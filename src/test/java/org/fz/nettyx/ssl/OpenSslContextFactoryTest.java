package org.fz.nettyx.ssl;

import io.netty.handler.ssl.SslContext;
import org.fz.nettyx.ssl.OpenSslContextFactory.OpenSslConfig;
import org.junit.Test;

import javax.net.ssl.SSLException;
import java.nio.file.Path;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class OpenSslContextFactoryTest {

    @Test
    public void encryptedPrivateKeyUsesPasswordOverloads() throws Exception
    {
        OpenSslConfig config = config();
        config.setKeyPass("secret");
        TrackingFactory factory = new TrackingFactory(config);

        factory.getServerSslContext();
        factory.getClientSslContext();

        assertTrue(factory.serverPasswordOverload);
        assertTrue(factory.clientPasswordOverload);
    }

    @Test
    public void unencryptedPrivateKeyUsesPasswordlessOverloads() throws Exception
    {
        TrackingFactory factory = new TrackingFactory(config());

        factory.getServerSslContext();
        factory.getClientSslContext();

        assertFalse(factory.serverPasswordOverload);
        assertFalse(factory.clientPasswordOverload);
    }

    @Test
    public void configAccessorsAndEqualityFollowLombokContract()
    {
        OpenSslConfig config = config();
        config.setKeyPass("secret");
        config.setHandshakeTimeoutSeconds(9);
        assertEquals("cert.pem", config.cert());
        assertEquals("key.pem", config.key());
        assertEquals("secret", config.keyPass());
        assertEquals("root.pem", config.root());
        assertEquals(9, config.handshakeTimeoutSeconds());
        assertEquals(config, config);
        assertEquals(config.hashCode(), config.hashCode());
        assertTrue(config.toString().contains("cert.pem"));
    }

    private static OpenSslConfig config()
    {
        OpenSslConfig config = new OpenSslConfig();
        config.setCert("cert.pem");
        config.setKey("key.pem");
        config.setRoot("root.pem");
        return config;
    }

    private static final class TrackingFactory extends OpenSslContextFactory {
        private boolean serverPasswordOverload;
        private boolean clientPasswordOverload;

        private TrackingFactory(OpenSslConfig config)
        {
            super(config);
        }

        @Override
        protected SslContext getServerSslContext(Path cert, Path key, Path root)
        {
            serverPasswordOverload = false;
            return null;
        }

        @Override
        protected SslContext getServerSslContext(Path cert, Path key, String keyPass, Path root)
        {
            serverPasswordOverload = true;
            return null;
        }

        @Override
        protected SslContext getClientSslContext(Path cert, Path key, Path root)
        {
            clientPasswordOverload = false;
            return null;
        }

        @Override
        protected SslContext getClientSslContext(Path cert, Path key, String keyPass, Path root) throws SSLException
        {
            clientPasswordOverload = true;
            return null;
        }
    }
}
