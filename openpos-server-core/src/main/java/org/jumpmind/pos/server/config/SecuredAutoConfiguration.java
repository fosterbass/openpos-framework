package org.jumpmind.pos.server.config;


import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.x509.*;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.security.auth.x500.X500Principal;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(name = "secured.enabled", havingValue = "true", matchIfMissing = true)
public class SecuredAutoConfiguration {
    private static final String DEFAULT_LABEL = "commerce-server";

    private static final int DEFAULT_EC_KEYSZ = 256;

    private static final String DEFAULT_EC_SIGALG = "SHA256withECDSA";

    private static final int DEFAULT_RSA_KEYSZ = 2048;

    private static final String DEFAULT_RSA_SIGALG = "SHA256withRSA";

    private static final String DEFAULT_DN = "O=JumpMind, OU=Commerce, CN=commerce-server";

    private static final int DEFAULT_DAYS = 3650/* = 10 (ten) years */;

    private static final String DEFAULT_SAN = "DNS:localhost4.localdomain4,DNS:localhost6.localdomain6,DNS:localhost.localdomain,DNS:localhost4,DNS:localhost6,DNS:localhost,IP:127.0.0.1,IP:0:0:0:0:0:0:0:1";

    private static final String DEFAULT_SECURITY_DIR_PERMS = "rwx------";

    private static final String DEFAULT_KEY_STORE_PERMS = "rw-------";

    private static final String DEFAULT_UNSAFE_KEY_STORE_PASSWORD = "changeit";

    static {
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
            log.debug("installed \"{}\" Security Provider", BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    private static char[] readPassword(final String envName) {
        if (System.getenv(envName) == null) {
            log.warn("{} is not defined! Using default (unsafe) key store password.", envName);
            return DEFAULT_UNSAFE_KEY_STORE_PASSWORD.toCharArray();
        }
        return System.getenv(envName).toCharArray();
    }

    private static KeyPair generateEcKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        generator.initialize(DEFAULT_EC_KEYSZ);
        return generator.generateKeyPair();
    }

    private static KeyPair generateRsaKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(DEFAULT_RSA_KEYSZ);
        return generator.generateKeyPair();
    }

    private static X509Certificate generateV3SelfSignedCertificate(final KeyPair keyPair, final String signatureAlgorithm)
            throws NoSuchAlgorithmException, CertIOException, OperatorCreationException, CertificateException {
        X500Principal self = new X500Principal(DEFAULT_DN);

        long now = System.currentTimeMillis();
        Date notBefore = new Date(now);
        Date notAfter = new Date(now + TimeUnit.DAYS.toMillis(DEFAULT_DAYS));

        String[] names = DEFAULT_SAN.split(",");
        GeneralName[] generalNames = Arrays.stream(names).map(SecuredAutoConfiguration::generalName).toArray(GeneralName[]::new);
        GeneralNames subjAltNames = new GeneralNames(generalNames);

        BigInteger serial = randomSerial();

        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(self, serial, notBefore, notAfter, self, keyPair.getPublic());

        JcaX509ExtensionUtils extUtils = new JcaX509ExtensionUtils();

        certBuilder.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature | KeyUsage.keyEncipherment));
        certBuilder.addExtension(Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_serverAuth));
        certBuilder.addExtension(Extension.basicConstraints, false, new BasicConstraints(false));
        certBuilder.addExtension(Extension.subjectAlternativeName, false, subjAltNames);
        certBuilder.addExtension(Extension.subjectKeyIdentifier, false, extUtils.createSubjectKeyIdentifier(keyPair.getPublic()));
        certBuilder.addExtension(Extension.authorityKeyIdentifier, false, extUtils.createAuthorityKeyIdentifier(keyPair.getPublic()));

        X509CertificateHolder certHolder = certBuilder.build(new JcaContentSignerBuilder(signatureAlgorithm).build(keyPair.getPrivate()));

        return new JcaX509CertificateConverter().getCertificate(certHolder);
    }

    private static BigInteger randomSerial() {
        return new BigInteger((20 * 8) - 1, new Random());
    }

    private static GeneralName generalName(String s) {
        if (s.toUpperCase().startsWith("DNS:")) {
            return new GeneralName(GeneralName.dNSName, s.substring(4));
        } else if (s.toUpperCase().startsWith("IP:")) {
            return new GeneralName(GeneralName.iPAddress, s.substring(3));
        } else {
            throw new IllegalArgumentException(String.format("can't process \"%s\" in SAN", s));
        }
    }

    private static Path ensuredPath(final Path path) throws IOException {
        Path ensure = path.toAbsolutePath().normalize();
        Path fileName = ensure.getFileName();
        Path parent = ensure.getParent();

        parent.toFile().mkdirs();
        /* should now succeed whether dirs were created or not: */
        parent = parent.toRealPath();

        if ("security".equals(parent.getFileName().toString())) {
            try {
                Files.setPosixFilePermissions(parent, PosixFilePermissions.fromString(DEFAULT_SECURITY_DIR_PERMS));
            } catch (UnsupportedOperationException ex) {
                log.warn("unable to set least perms on {} (non-POSIX-compliant file system)", parent);
            }
        }

        /* parent should now exist and be writable */
        return parent.resolve(fileName);
    }

    @Bean
    @ConfigurationProperties(prefix = "secured")
    SecuredConfiguration commerceServerSecuredConfiguration() {
        return new SecuredConfiguration();
    }

    @Bean
    KeyStore commerceServerKeyStore(
            @Qualifier("commerceServerSecuredConfiguration") final SecuredConfiguration config)
            throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, OperatorCreationException, NoSuchProviderException {
        log.debug("initializing commerce server key store from {}", config);
        try {
            KeyStore keyStore = KeyStore.getInstance(config.getKeyStoreType(), BouncyCastleProvider.PROVIDER_NAME);
            Path keyStorePath = Paths.get(config.getKeyStorePath());
            if (keyStorePath.toFile().exists()) {
                log.info("loading key store from {}", keyStorePath);
                try (InputStream is = Files.newInputStream(keyStorePath, StandardOpenOption.READ)) {
                    keyStore.load(is, readPassword(config.getKeyStorePasswordEnvName()));
                }
            } else if (config.isKeyStoreCreateIfNotExists()) {
                keyStorePath = ensuredPath(keyStorePath);
                log.info("auto-creating key store at {}", keyStorePath);

                keyStore.load(null, null);

                KeyPair ecKeyPair = generateEcKeyPair();
                X509Certificate ecCert = generateV3SelfSignedCertificate(ecKeyPair, DEFAULT_EC_SIGALG);

                KeyPair rsaKeyPair = generateRsaKeyPair();
                X509Certificate rsaCert = generateV3SelfSignedCertificate(rsaKeyPair, DEFAULT_RSA_SIGALG);

                keyStore.setKeyEntry(DEFAULT_LABEL + "-ec", ecKeyPair.getPrivate(), null, new Certificate[]{ecCert});
                keyStore.setKeyEntry(DEFAULT_LABEL + "-rsa", rsaKeyPair.getPrivate(), null, new Certificate[]{rsaCert});
                try (OutputStream out = Files.newOutputStream(keyStorePath, StandardOpenOption.CREATE_NEW)) {
                    keyStore.store(out, readPassword(config.getKeyStorePasswordEnvName()));
                } catch (Exception ex) {
                    /* do not allow a "corrupted" key store to be written */
                    Files.deleteIfExists(keyStorePath);
                    log.error("failed to create key store ({})", ex.toString());
                    throw ex;
                }
                try {
                    Files.setPosixFilePermissions(keyStorePath, PosixFilePermissions.fromString(DEFAULT_KEY_STORE_PERMS));
                } catch (UnsupportedOperationException ex) {
                    log.warn("unable to set least perms on {} (non-POSIX-compliant file system)", keyStorePath);
                }
            } else {
                throw new FileNotFoundException(keyStorePath.toString());
            }
            return keyStore;
        } finally {
            Security.removeProvider(BouncyCastleProvider.PROVIDER_NAME);
        }
    }

    @Bean
    SslContextFactory commerceServerSslContextFactory(
            @Qualifier("commerceServerSecuredConfiguration") final SecuredConfiguration config,
            @Qualifier("commerceServerKeyStore") final KeyStore keyStore) {
        SslContextFactory sslContextFactory = new SslContextFactory.Server();

        if (config.getExcludeProtocols() != null) {
            sslContextFactory.setExcludeProtocols(config.getExcludeProtocols());
        }
        if (config.getIncludeProtocols() != null) {
            sslContextFactory.setIncludeProtocols(config.getIncludeProtocols());
        }
        if (config.getExcludeCipherSuites() != null) {
            sslContextFactory.setExcludeCipherSuites(config.getExcludeCipherSuites());
        }
        if (config.getIncludeCipherSuites() != null) {
            sslContextFactory.setIncludeCipherSuites(config.getIncludeCipherSuites());
            sslContextFactory.setUseCipherSuitesOrder(config.isUseCipherSuitesOrder());
        }

        sslContextFactory.setCertAlias(config.getCertAlias());
        sslContextFactory.setKeyStore(keyStore);
        sslContextFactory.setKeyStorePath(config.getKeyStorePath());
        sslContextFactory.setKeyStoreType(config.getKeyStoreType());
        sslContextFactory.setKeyStorePassword(new String(readPassword(config.getKeyStorePasswordEnvName())));

        sslContextFactory.setWantClientAuth(config.isWantClientAuth());
        sslContextFactory.setNeedClientAuth(config.isNeedClientAuth());
        if (config.getTrustStorePath() != null) {
            sslContextFactory.setTrustStorePath(config.getTrustStorePath());
            sslContextFactory.setTrustStoreType(config.getTrustStoreType());
            sslContextFactory.setTrustStorePassword(new String(readPassword(config.getTrustStorePasswordEnvName())));
        }

        return sslContextFactory;
    }
}
