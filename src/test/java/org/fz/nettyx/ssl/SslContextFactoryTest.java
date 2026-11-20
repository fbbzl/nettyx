package org.fz.nettyx.ssl;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import javax.net.ssl.SSLContext;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Checks TLS configuration defaults and error wrapping for missing key or trust stores.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-09-14
 */
public class SslContextFactoryTest
{
    @Rule
    public TemporaryFolder stores = new TemporaryFolder();

    @Test
    public void initializesOneWayContextsFromReadableStores() throws Exception
    {
        String store = writeEmptyStore("one-way.p12", "store-password");

        assertTlsContext(SslContextFactory.ONEWAY.getServerContext(store, "store-password"));
        assertTlsContext(SslContextFactory.ONEWAY.getClientContext(store, "store-password"));
    }

    @Test
    public void initializesTwoWayContextsWithSeparateKeyAndTrustStores() throws Exception
    {
        String keys = writeEmptyStore("keys.p12", "key-password");
        String trust = writeEmptyStore("trust.p12", "trust-password");

        assertTlsContext(SslContextFactory.TWOWAY.getServerContext(keys, "key-password"));
        assertTlsContext(SslContextFactory.TWOWAY.getClientContext(keys, "key-password"));
        assertTlsContext(SslContextFactory.TWOWAY.getServerContext(keys, "key-password", trust, "trust-password"));
        assertTlsContext(SslContextFactory.TWOWAY.getClientContext(keys, "key-password", trust, "trust-password"));
        assertThrows(SecurityException.class,
                     () -> SslContextFactory.TWOWAY.getClientContext(keys, "key-password", trust, "wrong-password"));
    }

    private String writeEmptyStore(String fileName, String password) throws Exception
    {
        Path path = stores.newFile(fileName).toPath();
        char[] chars = password.toCharArray();
        KeyStore store = KeyStore.getInstance(KeyStore.getDefaultType());
        store.load(null, chars);
        try (OutputStream output = Files.newOutputStream(path)) {
            store.store(output, chars);
        }
        return path.toString();
    }

    private static void assertTlsContext(SSLContext context)
    {
        assertEquals("TLS", context.getProtocol());
        assertTrue(context.createSSLEngine().getEnabledProtocols().length > 0);
    }

    @Test
    public void oneWayFactoryWrapsMissingKeyStoreErrors()
    {
        assertThrows(SecurityException.class,
                     () -> SslContextFactory.ONEWAY.getServerContext("missing-keystore", "pwd"));
        assertThrows(SecurityException.class,
                     () -> SslContextFactory.ONEWAY.getClientContext("missing-keystore", "pwd"));
        assertThrows(SecurityException.class,
                     () -> SslContextFactory.ONEWAY.getServerContext("missing-keystore", "pwd".toCharArray()));
        assertThrows(SecurityException.class,
                     () -> SslContextFactory.ONEWAY.getClientContext("missing-keystore", "pwd".toCharArray()));
    }

    @Test
    public void twoWayFactoryWrapsMissingKeyAndTrustStores()
    {
        assertThrows(SecurityException.class,
                     () -> SslContextFactory.TWOWAY.getContext("missing-keystore", "pwd"));
        assertThrows(SecurityException.class,
                     () -> SslContextFactory.TWOWAY.getContext("missing-keystore", "pwd".toCharArray()));
        assertThrows(SecurityException.class,
                     () -> SslContextFactory.TWOWAY.getContext("missing-keystore", "pwd".toCharArray(), "missing-trust", "pwd".toCharArray()));
        assertThrows(SecurityException.class,
                     () -> SslContextFactory.TWOWAY.getServerContext("missing-keystore", "pwd", "missing-trust", "pwd"));
        assertThrows(SecurityException.class,
                     () -> SslContextFactory.TWOWAY.getClientContext("missing-keystore", "pwd", "missing-trust", "pwd"));
    }

    @Test
    public void sslAndTrustConfigurationExposeDefaultsAndSetters()
    {
        SslContextFactory.Ssl ssl = new SslContextFactory.Ssl();
        assertFalse(ssl.enable());
        assertEquals(5, ssl.handshakeTimeoutSeconds());
        ssl.setEnable(true);
        ssl.setPath("keystore");
        ssl.setPassword("password");
        ssl.setHandshakeTimeoutSeconds(12);
        SslContextFactory.Ssl.Trust trust = new SslContextFactory.Ssl.Trust();
        trust.setPath("truststore");
        trust.setPassword("trust-password");
        ssl.setTrust(trust);
        assertTrue(ssl.enable());
        assertEquals("keystore", ssl.path());
        assertEquals("password", ssl.pwd());
        assertEquals(12, ssl.handshakeTimeoutSeconds());
        assertNotNull(ssl.trust());
        assertEquals("truststore", trust.path());
        assertEquals("trust-password", trust.pwd());
    }
}
