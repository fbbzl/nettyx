package org.fz.nettyx.ssl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class SslContextFactoryTest
{
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
