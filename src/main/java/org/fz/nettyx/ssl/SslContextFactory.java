package org.fz.nettyx.ssl;


import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.UtilityClass;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

import static lombok.AccessLevel.PACKAGE;

/**
 * SSL context factory, provide one-way/two-way
 * you may use like the following code:
 * private SslHandler newSslHandler(SocketAddress remoteAddress) {
 * if (ssl.enable()) {
 * SSLEngine sslEngine = SslContextFactory.TWOWAY.getServerContext(ssl.path(), ssl.pwd()).createSSLEngine();
 * sslEngine.setUseClientMode(true);
 * <p>
 * SslHandler sslHandler = new SslHandler(sslEngine);
 * sslHandler.setHandshakeTimeout(ssl.handshakeTimeoutSeconds(), TimeUnit.SECONDS);
 * <p>
 * GenericFutureListener<Promise<Channel>> handshakeListener =
 * future -> {
 * if (future.isSuccess())  log.info("ssl handshake success, remote address is [{}]", remoteAddress);
 * if (!future.isSuccess()) log.error("ssl handshake failure, remote address is [{}]", remoteAddress, future.cause());
 * };
 * <p>
 * sslHandler.handshakeFuture().addListener(handshakeListener);
 * return sslHandler;
 * }
 * else return null;
 * }
 *
 * @author fengbinbin
 * @version 1.0
 * @see SslContextFactory#ONEWAY SslContextFactory#ONEWAY
 * @see SslContextFactory#TWOWAY SslContextFactory#TWOWAY
 * @see <a href="https://github.com/ZHI-XINHUA/myNetty/tree/master/src/zxh/netty/ssl">refer</a>
 * @since 2022 /1/21 14:24
 */
@UtilityClass
public class SslContextFactory {

    /**
     * The constant ONEWAY.
     */
    public static final OneWay ONEWAY;
    /**
     * The constant TWOWAY.
     */
    public static final TwoWay TWOWAY;

    static {
        synchronized (SslContextFactory.class) {
            ONEWAY = new OneWay("TLS");
            TWOWAY = new TwoWay("TLS");
        }
    }

    /**
     * The type One way.
     */
    @RequiredArgsConstructor(access = PACKAGE)
    public static class OneWay {

        private final String protocol;

        /**
         * Gets server context.
         *
         * @param path the path
         * @param pwd  the pwd
         * @return the server context
         */
        public SSLContext getServerContext(String path, String pwd) {
            // here will be no string constant pool security issues
            return getServerContext(path, pwd.toCharArray());
        }

        /**
         * Gets server context.
         *
         * @param path the key file path
         * @param pwd  the pwd witch be used to generate the keystore
         * @return the server context
         */
        public SSLContext getServerContext(String path, char[] pwd) {
            try (InputStream in = Files.newInputStream(Paths.get(path))) {

                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, pwd);

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, pwd);

                SSLContext serverSslContext = SSLContext.getInstance(protocol);
                serverSslContext.init(keyManagerFactory.getKeyManagers(), null, null);

                return serverSslContext;
            } catch (Exception serverSslCtxInitException) {
                throw new SecurityException("init server ssl context error, path is [" + path + "]", serverSslCtxInitException);
            }
        }

        /**
         * Gets client context.
         *
         * @param path the path
         * @param pwd  the pwd
         * @return the client context
         */
        public SSLContext getClientContext(String path, String pwd) {
            // here will be no string constant pool security issues
            return getClientContext(path, pwd.toCharArray());
        }

        /**
         * Gets client context.
         *
         * @param path the key file path
         * @param pwd  the pwd witch be used to generate the keystore
         * @return the client context
         */
        public SSLContext getClientContext(String path, char[] pwd) {
            try (InputStream in = Files.newInputStream(Paths.get(path))) {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, pwd);

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);

                SSLContext clientSslContext = SSLContext.getInstance(protocol);
                clientSslContext.init(null, trustManagerFactory.getTrustManagers(), null);

                return clientSslContext;
            } catch (Exception clientSslCtxInitException) {
                throw new SecurityException("init client ssl context error, path is [" + path + "]", clientSslCtxInitException);
            }
        }
    }

    /**
     * The type Two way.
     */
    @RequiredArgsConstructor(access = PACKAGE)
    public static class TwoWay {

        private final String protocol;

        /**
         * Gets server context.
         *
         * @param path the path
         * @param pwd  the pwd
         * @return the server context
         */
        public SSLContext getServerContext(String path, String pwd) {
            return getContext(path, pwd.toCharArray(), path, pwd.toCharArray());
        }

        /**
         * Gets client context.
         *
         * @param path the path
         * @param pwd  the pwd
         * @return the client context
         */
        public SSLContext getClientContext(String path, String pwd) {
            return getContext(path, pwd.toCharArray(), path, pwd.toCharArray());
        }

        /**
         * Gets server context.
         *
         * @param path      the path
         * @param pwd       the pwd
         * @param trustPath the trust path
         * @param trustPwd  the trust pwd
         * @return the server context
         */
        public SSLContext getServerContext(String path, String pwd, String trustPath, String trustPwd) {
            try {
                return getContext(path, pwd.toCharArray(), trustPath, trustPwd.toCharArray());
            } catch (Exception serverSslContextException) {
                throw new SecurityException("init server ssl context failed", serverSslContextException);
            }
        }

        /**
         * Gets client context.
         *
         * @param path      the path
         * @param pwd       the pwd
         * @param trustPath the trust path
         * @param trustPwd  the trust pwd
         * @return the client context
         */
        public SSLContext getClientContext(String path, String pwd, String trustPath, String trustPwd) {
            try {
                return getContext(path, pwd.toCharArray(), trustPath, trustPwd.toCharArray());
            } catch (Exception clientSslContextException) {
                throw new SecurityException("init client ssl context failed", clientSslContextException);
            }
        }

        /**
         * Gets context.
         *
         * @param path the path
         * @param pwd  the pwd
         * @return the context
         */
        SSLContext getContext(String path, String pwd) {
            return getContext(path, pwd.toCharArray(), path, pwd.toCharArray());
        }

        /**
         * Gets context.
         *
         * @param path the path
         * @param pwd  the pwd
         * @return the context
         */
        SSLContext getContext(String path, char[] pwd) {
            return getContext(path, pwd, path, pwd);
        }

        /**
         * two-way type server/client context is the same
         *
         * @param path      the path
         * @param pwd       the pwd
         * @param trustPath the trust path
         * @param trustPwd  the trust pwd
         * @return the context
         */
        public SSLContext getContext(String path, char[] pwd, String trustPath, char[] trustPwd) {
            try (
                    InputStream in = Files.newInputStream(Paths.get(path));
                    InputStream trustIn = Files.newInputStream(Paths.get(trustPath))) {

                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, pwd);

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, pwd);

                KeyStore trustKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                trustKeyStore.load(trustIn, trustPwd);

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(trustKeyStore);

                SSLContext sslContext = SSLContext.getInstance(protocol);
                sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

                return sslContext;
            } catch (Exception sslContextInitException) {
                throw new SecurityException(sslContextInitException);
            }
        }
    }

    /**
     * The type Ssl.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2022 /1/21 16:09
     */
    @Setter
    public static class Ssl {

        private static final int DEFAULT_HANDSHAKE_TIMEOUT_SECONDS = 5;

        /**
         * if enable ssl
         */
        private boolean enable;

        /**
         * key store path
         */
        private String path;

        /**
         * key store password
         */
        private String password;

        /**
         * trust path
         */
        private Trust trust;

        /**
         * Handshake Timeout Seconds
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
         * Path string.
         *
         * @return the string
         */
        public String path() {
            return path;
        }

        /**
         * Pwd string.
         *
         * @return the string
         */
        public String pwd() {
            return password;
        }

        /**
         * Trust trust.
         *
         * @return the trust
         */
        public Trust trust() {
            return trust;
        }

        /**
         * Handshake timeout seconds int.
         *
         * @return the int
         */
        public int handshakeTimeoutSeconds() {
            return handshakeTimeoutSeconds;
        }

        /**
         * The type Trust.
         */
        @Setter
        public static class Trust {

            /**
             * key file path
             */
            private String path;

            /**
             * key password
             */
            private String password;

            /**
             * Path string.
             *
             * @return the string
             */
            public String path() {
                return path;
            }

            /**
             * Pwd string.
             *
             * @return the string
             */
            public String pwd() {
                return password;
            }

        }
    }

}
