package org.jumpmind.pos.server.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

/**
 * Configuration settings for the TLS-secured port(s).
 *
 * <p>Setting names correspond roughly to getters/setters found in
 * {@link org.eclipse.jetty.util.ssl.SslContextFactory}
 * (i.e. the objects that {@link SecuredAutoConfiguration} configures).</p>
 */
@Slf4j
public class SecuredConfiguration implements InitializingBean {

    @Getter
    @Setter
    int port;

    @Getter
    @Setter
    Set<Integer> secondaryPorts = Collections.emptySet();

    @Getter
    @Setter
    String[] excludeProtocols;

    @Getter
    @Setter
    String[] includeProtocols;

    @Getter
    @Setter
    String[] excludeCipherSuites;

    @Getter
    @Setter
    String[] includeCipherSuites;

    /* aligned with Jetty default */
    @Getter
    @Setter
    boolean useCipherSuitesOrder = true;

    @Getter
    @Setter
    String certAlias;

    @Getter
    @Setter
    String keyStorePath;

    @Getter
    @Setter
    String keyStoreType;

    @Getter
    @Setter
    String keyStorePasswordEnvName;

    /**
     * Whether or not to generate a self-signed certificate if
     * {@link #getKeyStorePasswordEnvName()}  does not exist.
     */
    @Getter
    @Setter
    boolean keyStoreCreateIfNotExists;

    @Getter
    @Setter
    boolean wantClientAuth;

    @Getter
    @Setter
    boolean needClientAuth;

    @Getter
    @Setter
    String trustStorePath;

    @Getter
    @Setter
    String trustStoreType;

    @Getter
    @Setter
    String trustStorePasswordEnvName;

    /**
     * If not defined, the default Java Security Provider will be used for loading the keystore. Specify "BC" to use
     * BouncyCastle.
     */
    @Getter
    @Setter
    String keyStoreSecurityProvider;

    @Override
    public void afterPropertiesSet() {
        log.info("Using {}", this);
    }

    @Override
    public String toString() {
        return String.format(
                "%s{port=%d, secondaryPorts=%s, excludeProtocols=%s, includeProtocols=%s, excludeCipherSuites=%s, includeCipherSuites=%s, useCipherSuitesOrder=%s, keyStorePath=%s, keyStoreType=%s, certAlias=%s, trustStorePath=%s, trustStoreType=%s, wantClientAuth=%s, needClientAuth=%s}",
                getClass().getSimpleName(), port, secondaryPorts, Arrays.toString(excludeProtocols), Arrays.toString(includeProtocols), Arrays.toString(excludeCipherSuites), Arrays.toString(includeCipherSuites), useCipherSuitesOrder, keyStorePath, keyStoreType, certAlias, trustStorePath, trustStoreType, wantClientAuth, needClientAuth);
    }
}
