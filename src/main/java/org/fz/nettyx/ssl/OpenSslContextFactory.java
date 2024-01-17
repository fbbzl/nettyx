package org.fz.nettyx.ssl;

import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import javax.net.ssl.SSLException;
import lombok.Setter;
import lombok.experimental.UtilityClass;

/**
 * For details about how to generate openssl files, see {@link ./support/openssl}.
 * you may use like the following code:
 * this.sslContext = OpenSslContextFactory.getClientSslContext(openSsl);
 * SslHandler sslHandler = sslContext.newHandler(channel.alloc());
 * sslHandler.setHandshakeTimeout(openSsl.handshakeTimeoutSeconds(), TimeUnit.SECONDS);
 * GenericFutureListener<Promise<Channel>> handshakeListener =
 * future -> {
 * if (future.isSuccess())  log.info("ssl handshake success, remote address is [{}]", remoteAddress);
 * if (!future.isSuccess()) log.error("ssl handshake failure, remote address is [{}], exception is: [{}]", remoteAddress, future.cause().getMessage());
 * };
 * sslHandler.handshakeFuture().addListener(handshakeListener);
 * return sslHandler;
 * }
 * else return null;
 *
 * @author fengbinbin
 * @version 1.0
 * @since 3 /8/2022 11:45 AM
 */
@UtilityClass
public class OpenSslContextFactory {

    /**
     * Gets server ssl context.
     *
     * @param openSslConfig the open ssl config
     * @return the server ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getServerSslContext(OpenSslConfig openSslConfig) throws SSLException {
        return getServerSslContext(openSslConfig.cert(), openSslConfig.key(), openSslConfig.root());
    }

    /**
     * Gets client ssl context.
     *
     * @param openSslConfig the open ssl config
     * @return the client ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getClientSslContext(OpenSslConfig openSslConfig) throws SSLException {
        return getClientSslContext(openSslConfig.cert(), openSslConfig.key(), openSslConfig.root());
    }

    /**
     * Gets server ssl context.
     *
     * @param certChainPath the cert chain path
     * @param keyPath       the key path
     * @param rootPath      the root path
     * @return the server ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getServerSslContext(String certChainPath, String keyPath, String rootPath) throws SSLException {
        File certChainFile = new File(certChainPath),
            keyFile       = new File(keyPath),
            rootFile      = new File(rootPath);

        return SslContextBuilder.forServer(certChainFile, keyFile).trustManager(rootFile).clientAuth(ClientAuth.REQUIRE).build();
    }

    /**
     * Gets server ssl context.
     *
     * @param certChainPath the cert chain path
     * @param keyPath       the key path
     * @param keypass       the keypass
     * @param rootPath      the root path
     * @return the server ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getServerSslContext(String certChainPath, String keyPath, String keypass,String rootPath) throws SSLException {
        File certChainFile = new File(certChainPath),
            keyFile       = new File(keyPath),
            rootFile      = new File(rootPath);

        return SslContextBuilder.forServer(certChainFile, keyFile, keypass).trustManager(rootFile).clientAuth(ClientAuth.REQUIRE).build();
    }

    /**
     * Gets server ssl context.
     *
     * @param certChainFile the cert chain file
     * @param keyFile       the key file
     * @param rootFile      the root file
     * @return the server ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getServerSslContext(File certChainFile, File keyFile, File rootFile) throws SSLException {
        return SslContextBuilder.forServer(certChainFile, keyFile).trustManager(rootFile).clientAuth(ClientAuth.REQUIRE).build();
    }

    /**
     * Gets server ssl context.
     *
     * @param certChainFile the cert chain file
     * @param keyFile       the key file
     * @param keypass       the keypass
     * @param rootFile      the root file
     * @return the server ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getServerSslContext(File certChainFile, File keyFile, String keypass, File rootFile) throws SSLException {
        return SslContextBuilder.forServer(certChainFile, keyFile, keypass).trustManager(rootFile).clientAuth(ClientAuth.REQUIRE).build();
    }

    /**
     * Gets server ssl context.
     *
     * @param certChainPath the cert chain path
     * @param keyPath       the key path
     * @param rootPath      the root path
     * @return the server ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getServerSslContext(Path certChainPath, Path keyPath, Path rootPath) throws SSLException {
        return SslContextBuilder.forServer(certChainPath.toFile(), keyPath.toFile()).trustManager(rootPath.toFile()).clientAuth(ClientAuth.REQUIRE).build();
    }

    /**
     * Gets server ssl context.
     *
     * @param certChainPath the cert chain path
     * @param keyPath       the key path
     * @param keypass       the keypass
     * @param rootPath      the root path
     * @return the server ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getServerSslContext(Path certChainPath, Path keyPath, String keypass, Path rootPath) throws SSLException {
        return SslContextBuilder.forServer(certChainPath.toFile(), keyPath.toFile(), keypass).trustManager(rootPath.toFile()).clientAuth(ClientAuth.REQUIRE).build();
    }

    /**
     * Gets server ssl context.
     *
     * @param certChainStream the cert chain stream
     * @param keyStream       the key stream
     * @param rootStream      the root stream
     * @return the server ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getServerSslContext(InputStream certChainStream, InputStream keyStream, InputStream rootStream) throws SSLException {
        return SslContextBuilder.forServer(certChainStream, keyStream).trustManager(rootStream).clientAuth(ClientAuth.REQUIRE).build();
    }

    /**
     * Gets server ssl context.
     *
     * @param certChainStream the cert chain stream
     * @param keyStream       the key stream
     * @param keypass         the keypass
     * @param rootStream      the root stream
     * @return the server ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getServerSslContext(InputStream certChainStream, InputStream keyStream, String keypass, InputStream rootStream) throws SSLException {
        return SslContextBuilder.forServer(certChainStream, keyStream, keypass).trustManager(rootStream).clientAuth(ClientAuth.REQUIRE).build();
    }

    /**
     * Gets client ssl context.
     *
     * @param certChainPath the cert chain path
     * @param keyPath       the key path
     * @param rootPath      the root path
     * @return the client ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getClientSslContext(String certChainPath, String keyPath, String rootPath) throws SSLException {
        File certChainFile = new File(certChainPath),
            keyFile       = new File(keyPath),
            rootFile      = new File(rootPath);

        return SslContextBuilder.forClient().keyManager(certChainFile, keyFile).trustManager(rootFile).build();
    }

    /**
     * Gets client ssl context.
     *
     * @param certChainPath the cert chain path
     * @param keyPath       the key path
     * @param keypass       the keypass
     * @param rootPath      the root path
     * @return the client ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getClientSslContext(String certChainPath, String keyPath, String keypass,String rootPath) throws SSLException {
        File certChainFile = new File(certChainPath),
            keyFile       = new File(keyPath),
            rootFile      = new File(rootPath);

        return SslContextBuilder.forClient().keyManager(certChainFile, keyFile, keypass).trustManager(rootFile).build();
    }

    /**
     * Gets client ssl context.
     *
     * @param certChainFile the cert chain file
     * @param keyFile       the key file
     * @param rootFile      the root file
     * @return the client ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getClientSslContext(File certChainFile, File keyFile, File rootFile) throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainFile, keyFile).trustManager(rootFile).build();
    }

    /**
     * Gets client ssl context.
     *
     * @param certChainFile the cert chain file
     * @param keyFile       the key file
     * @param keypass       the keypass
     * @param rootFile      the root file
     * @return the client ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getClientSslContext(File certChainFile, File keyFile, String keypass, File rootFile) throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainFile, keyFile, keypass).trustManager(rootFile).build();
    }

    /**
     * Gets client ssl context.
     *
     * @param certChainPath the cert chain path
     * @param keyPath       the key path
     * @param rootPath      the root path
     * @return the client ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getClientSslContext(Path certChainPath, Path keyPath, Path rootPath) throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainPath.toFile(), keyPath.toFile()).trustManager(rootPath.toFile()).build();
    }

    /**
     * Gets client ssl context.
     *
     * @param certChainPath the cert chain path
     * @param keyPath       the key path
     * @param keypass       the keypass
     * @param rootPath      the root path
     * @return the client ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getClientSslContext(Path certChainPath, Path keyPath, String keypass, Path rootPath) throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainPath.toFile(), keyPath.toFile(), keypass).trustManager(rootPath.toFile()).build();
    }

    /**
     * Gets client ssl context.
     *
     * @param certChainStream the cert chain stream
     * @param keyStream       the key stream
     * @param rootStream      the root stream
     * @return the client ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getClientSslContext(InputStream certChainStream, InputStream keyStream, InputStream rootStream) throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainStream, keyStream).trustManager(rootStream).build();
    }

    /**
     * Gets client ssl context.
     *
     * @param certChainStream the cert chain stream
     * @param keyStream       the key stream
     * @param keypass         the keypass
     * @param rootStream      the root stream
     * @return the client ssl context
     * @throws SSLException the ssl exception
     */
    public static SslContext getClientSslContext(InputStream certChainStream, InputStream keyStream, String keypass, InputStream rootStream) throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainStream, keyStream, keypass).trustManager(rootStream).build();
    }

    /**
     * The type Open ssl config.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 3 /8/2022 12:59 PM
     */
    @Setter
    public static class OpenSslConfig {

        private static final int DEFAULT_HANDSHAKE_TIMEOUT_SECONDS = 5;

        /**
         * if enable the openssl
         */
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

        /**
         * handshake timeout seconds
         */
        private int handshakeTimeoutSeconds = DEFAULT_HANDSHAKE_TIMEOUT_SECONDS;

        /**
         * Enable boolean.
         *
         * @return the boolean
         */
        public boolean enable() {
            return enable;
        }

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
