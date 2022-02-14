package org.fz.nettyx.ssl;


import static lombok.AccessLevel.PACKAGE;

import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.experimental.UtilityClass;

/**
 * SSL context factory, provide one-way/two-way
 * @author fengbinbin
 * @version 1.0
 * @see SslContextFactory#ONEWAY
 * @see SslContextFactory#TWOWAY
 * @see <a href="https://github.com/ZHI-XINHUA/myNetty/tree/master/src/zxh/netty/ssl">refer</a>
 * @since 2022/1/21 14:24
 */
@UtilityClass
public class SslContextFactory {

    public static final OneWay ONEWAY;
    public static final TwoWay TWOWAY;

    static {
        ONEWAY = new OneWay("TLS");
        TWOWAY = new TwoWay("TLS");
    }

    @RequiredArgsConstructor(access = PACKAGE)
    public static class OneWay {

        private final String protocol;

        public SSLContext getServerContext(String path, String pwd) {
            // here will be no string constant pool security issues
            return getServerContext(path, pwd.toCharArray());
        }

        /**
         * @param path the key file path
         * @param pwd the pwd witch be used to generate the keystore
         */
        public SSLContext getServerContext(String path, char[] pwd) {
            try (InputStream in = new FileInputStream(path)) {

                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, pwd);

                KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
                keyManagerFactory.init(keyStore, pwd);

                SSLContext serverSslContext = SSLContext.getInstance(protocol);
                serverSslContext.init(keyManagerFactory.getKeyManagers(), null, null);

                return serverSslContext;
            } catch (Exception serverSslCtxInitException) {
                throw new RuntimeException("init server ssl context error, path is [" + path + "]", serverSslCtxInitException);
            }
        }

        public SSLContext getClientContext(String path, String pwd) {
            // here will be no string constant pool security issues
            return getClientContext(path, pwd.toCharArray());
        }

        /**
         * @param path the key file path
         * @param pwd the pwd witch be used to generate the keystore
         */
        public SSLContext getClientContext(String path, char[] pwd) {
            try (InputStream in = new FileInputStream(path)) {
                KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                keyStore.load(in, pwd);

                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);

                SSLContext clientSslContext = SSLContext.getInstance(protocol);
                clientSslContext.init(null, trustManagerFactory.getTrustManagers(), null);

                return clientSslContext;
            } catch (Exception clientSslCtxInitException) {
                throw new RuntimeException("init client ssl context error, path is [" + path + "]", clientSslCtxInitException);
            }
        }
    }

    @RequiredArgsConstructor(access = PACKAGE)
    public static class TwoWay {

        private final String protocol;

        public SSLContext getServerContext(String path, String pwd) {
            return getContext(path, pwd.toCharArray(), path, pwd.toCharArray());
        }

        public SSLContext getClientContext(String path, String pwd) {
            return getContext(path, pwd.toCharArray(), path, pwd.toCharArray());
        }

        public SSLContext getServerContext(String path, String pwd, String trustPath, String trustPwd) {
            try {
                return getContext(path, pwd.toCharArray(), trustPath, trustPwd.toCharArray());
            } catch (Exception serverSslContextException) {
                throw new RuntimeException("init server ssl context failed", serverSslContextException);
            }
        }

        public SSLContext getClientContext(String path, String pwd, String trustPath, String trustPwd) {
            try {
                return getContext(path, pwd.toCharArray(), trustPath, trustPwd.toCharArray());
            } catch (Exception clientSslContextException) {
                throw new RuntimeException("init client ssl context failed", clientSslContextException);
            }
        }

        SSLContext getContext(String path, String pwd) {
            return getContext(path, pwd.toCharArray(), path, pwd.toCharArray());
        }

        SSLContext getContext(String path, char[] pwd) {
            return getContext(path, pwd, path, pwd);
        }

        /**
         * two-way type server/client context is the same
         */
        public SSLContext getContext(String path, char[] pwd, String trustPath, char[] trustPwd) {
            try (
                InputStream in      = new FileInputStream(path);
                InputStream trustIn = new FileInputStream(trustPath)) {

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
                throw new RuntimeException(sslContextInitException);
            }
        }
    }

}
