package org.jumpmind.pos.server.config;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.bouncycastle.crypto.prng.EntropySource;
import org.bouncycastle.crypto.prng.EntropySourceProvider;

/*
 * This provider yields a non-blocking entropy source based on
 * {@code /dev/urandom} (UN*Xes).
 *
 * <p>This exists only as a workaround for slow startup times due to low
 * entropy; it is actually "less secure" than the BC default provider (which is
 * implemented to ensure enough entropy is gathered even in containerized
 * applications).
 * </p>
 *
 * <p>Always prefer the following in order:
 * <ol>
 * <li>Increase the entropy available to the container by running an entropy-
 * gathering daemon (EGD) on the host.</li>
 * <li>Set the <em>org.bouncycastle.drbg.gather_pause_secs</em> system property
 * to a lower value (the default is 5). This may only be reasonable for
 * containerized applications.</li>
 * <li><strong>Finally,</strong> if all else fails and the entropy gathering
 * wait time is still unacceptable, then set the
 * <em>org.bouncycastle.drbg.entropysource</em> system property to this
 * provider's FQCN.</li>
 * </ol>
 * </p>
 */
public class NonBlockingEntropySourceProvider implements EntropySourceProvider {
    @Override
    public EntropySource get(final int bitsRequired) {
        final int byteLength = (bitsRequired + 7) / 8;
        return new EntropySource() {
            @Override
            public int entropySize() {
                return byteLength * 8;
            }

            @Override
            public boolean isPredictionResistant() {
                return true;
            }

            @Override
            public byte[] getEntropy() {
                byte[] entropy = new byte[byteLength];
                /*
                 * use fresh instance to ensure that seeding takes place
                 * regularly (could also use a SecureRandom member and regular
                 * calls to #setSeed, but keep this simple until it needs to be
                 * otherwise)
                 */
                try {
                    SecureRandom.getInstance("NativePRNGNonBlocking").nextBytes(entropy);
                } catch (NoSuchAlgorithmException ex) {
                    throw new UnsupportedOperationException(ex);
                }
                return entropy;
            }
        };
    }
}
