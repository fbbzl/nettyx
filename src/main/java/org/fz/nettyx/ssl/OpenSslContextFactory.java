package org.fz.nettyx.ssl;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import javax.net.ssl.SSLException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * use to get OpenSslContext
 *
 * @author fengbinbin
 * @version 1.0
 * @since 3 /8/2022 11:45 AM
 */

@Data
@RequiredArgsConstructor
public class OpenSslContextFactory {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(OpenSslContextFactory.class);
    private final        OpenSslConfig  openSslConfig;

    public SslContext getServerSslContext() throws SSLException {
        return getServerSslContext(Paths.get(openSslConfig.cert()), Paths.get(openSslConfig.key()),
                                   Paths.get(openSslConfig.root()));
    }

    public SslContext getClientSslContext() throws SSLException {
        return getClientSslContext(Paths.get(openSslConfig.cert()), Paths.get(openSslConfig.key()),
                                   Paths.get(openSslConfig.root()));
    }

    //****************************************************************************************************************//

    protected SslContext getServerSslContext(Path certChainPath, Path keyPath, Path rootPath) throws SSLException {
        return SslContextBuilder.forServer(certChainPath.toFile(), keyPath.toFile()).trustManager(rootPath.toFile())
                                .clientAuth(ClientAuth.REQUIRE).build();
    }

    protected SslContext getServerSslContext(Path certChainPath, Path keyPath, String keypass, Path rootPath)
            throws SSLException {
        return SslContextBuilder.forServer(certChainPath.toFile(), keyPath.toFile(), keypass)
                                .trustManager(rootPath.toFile()).clientAuth(ClientAuth.REQUIRE).build();
    }

    //****************************************************************************************************************//

    protected SslContext getClientSslContext(Path certChainPath, Path keyPath, Path rootPath) throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainPath.toFile(), keyPath.toFile())
                                .trustManager(rootPath.toFile()).build();
    }

    protected SslContext getClientSslContext(Path certChainPath, Path keyPath, String keypass, Path rootPath)
            throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainPath.toFile(), keyPath.toFile(), keypass)
                                .trustManager(rootPath.toFile()).build();
    }

    @Data
    public static class OpenSslConfig {

        private static final int DEFAULT_HANDSHAKE_TIMEOUT_SECONDS = 5;

        /**
         * certificate path
         */
        private String cert;

        /**
         * jks file
         */
        private String key;

        /**
         * jks file password
         */
        private String keyPass;

        /**
         * root certificate path
         */
        private String root;

        /**
         * handshake timeout seconds
         */
        private int handshakeTimeoutSeconds = DEFAULT_HANDSHAKE_TIMEOUT_SECONDS;

        /**
         * Cert string.
         *
         * @return the string
         */
        public String cert() {
            return cert;
        }

        /**
         * Key string.
         *
         * @return the string
         */
        public String key() {
            return key;
        }

        /**
         * Root string.
         *
         * @return the string
         */
        public String root() {
            return root;
        }

        /**
         * Handshake timeout seconds int.
         *
         * @return the int
         */
        public int handshakeTimeoutSeconds() {
            return handshakeTimeoutSeconds;
        }
    }

}
