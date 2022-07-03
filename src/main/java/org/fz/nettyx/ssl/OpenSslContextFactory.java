package org.fz.nettyx.ssl;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.File;
import javax.net.ssl.SSLException;
import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 3/8/2022 11:45 AM
 */

@UtilityClass
public class OpenSslContextFactory {

    public static SslContext getServerSslContext(OpenSsl openSsl) throws SSLException {
        return getServerSslContext(openSsl.cert(), openSsl.key(), openSsl.root());
    }

    public static SslContext getClientSslContext(OpenSsl openSsl) throws SSLException {
        return getClientSslContext(openSsl.cert(), openSsl.key(), openSsl.root());
    }

    public static SslContext getServerSslContext(String certChainPath, String keyPath, String rootPath) throws SSLException {
        File certChainFile = new File(certChainPath),
             keyFile       = new File(keyPath),
             rootFile      = new File(rootPath);

        return SslContextBuilder.forServer(certChainFile, keyFile).trustManager(rootFile).clientAuth(ClientAuth.REQUIRE).build();
    }

    public static SslContext getClientSslContext(String certChainPath, String keyPath, String rootPath) throws SSLException {
        File certChainFile = new File(certChainPath),
             keyFile       = new File(keyPath),
             rootFile      = new File(rootPath);

        return SslContextBuilder.forClient().keyManager(certChainFile, keyFile).trustManager(rootFile).build();
    }


    /**
     * @author fengbinbin
     * @version 1.0
     * @since 3/8/2022 12:59 PM
     */
    @Setter
    public static class OpenSsl {

        private static final int DEFAULT_HANDSHAKE_TIMEOUT_SECONDS = 5;

        private boolean enable;

        /**
         * certificate path
         */
        private String cert;

        /**
         * jks file
         */
        private String key;

        /**
         * root certificate path
         */
        private String root;

        private int handshakeTimeoutSeconds = DEFAULT_HANDSHAKE_TIMEOUT_SECONDS;

        public boolean enable() {
            return enable;
        }

        public String cert() {
            return cert;
        }

        public String key() {
            return key;
        }

        public String root() {
            return root;
        }

        public int handshakeTimeoutSeconds() {
            return handshakeTimeoutSeconds;
        }
    }
}
