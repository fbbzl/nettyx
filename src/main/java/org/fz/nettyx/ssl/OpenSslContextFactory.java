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
 * you may use like the following code:
 *           this.sslContext = OpenSslContextFactory.getClientSslContext(openSsl);
*            SslHandler sslHandler = sslContext.newHandler(channel.alloc());
*            sslHandler.setHandshakeTimeout(openSsl.handshakeTimeoutSeconds(), TimeUnit.SECONDS);
*            GenericFutureListener<Promise<Channel>> handshakeListener =
*                future -> {
*                    if (future.isSuccess())  log.info("ssl handshake success, remote address is [{}]", remoteAddress);
*                    if (!future.isSuccess()) log.error("ssl handshake failure, remote address is [{}], exception is: [{}]", remoteAddress, future.cause().getMessage());
*                };
*            sslHandler.handshakeFuture().addListener(handshakeListener);
*            return sslHandler;
*        }
*         else return null;
 *
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

    public static SslContext getServerSslContext(String certChainPath, String keyPath, String keypass,String rootPath) throws SSLException {
        File certChainFile = new File(certChainPath),
            keyFile       = new File(keyPath),
            rootFile      = new File(rootPath);

        return SslContextBuilder.forServer(certChainFile, keyFile, keypass).trustManager(rootFile).clientAuth(ClientAuth.REQUIRE).build();
    }

    public static SslContext getServerSslContext(File certChainFile, File keyFile, File rootFile) throws SSLException {
        return SslContextBuilder.forServer(certChainFile, keyFile).trustManager(rootFile).clientAuth(ClientAuth.REQUIRE).build();
    }

    public static SslContext getServerSslContext(File certChainFile, File keyFile, String keypass, File rootFile) throws SSLException {
        return SslContextBuilder.forServer(certChainFile, keyFile, keypass).trustManager(rootFile).clientAuth(ClientAuth.REQUIRE).build();
    }

    public static SslContext getServerSslContext(Path certChainPath, Path keyPath, Path rootPath) throws SSLException {
        return SslContextBuilder.forServer(certChainPath.toFile(), keyPath.toFile()).trustManager(rootPath.toFile()).clientAuth(ClientAuth.REQUIRE).build();
    }

    public static SslContext getServerSslContext(Path certChainPath, Path keyPath, String keypass, Path rootPath) throws SSLException {
        return SslContextBuilder.forServer(certChainPath.toFile(), keyPath.toFile(), keypass).trustManager(rootPath.toFile()).clientAuth(ClientAuth.REQUIRE).build();
    }

    public static SslContext getServerSslContext(InputStream certChainStream, InputStream keyStream, InputStream rootStream) throws SSLException {
        return SslContextBuilder.forServer(certChainStream, keyStream).trustManager(rootStream).clientAuth(ClientAuth.REQUIRE).build();
    }

    public static SslContext getServerSslContext(InputStream certChainStream, InputStream keyStream, String keypass, InputStream rootStream) throws SSLException {
        return SslContextBuilder.forServer(certChainStream, keyStream, keypass).trustManager(rootStream).clientAuth(ClientAuth.REQUIRE).build();
    }

    public static SslContext getClientSslContext(String certChainPath, String keyPath, String rootPath) throws SSLException {
        File certChainFile = new File(certChainPath),
            keyFile       = new File(keyPath),
            rootFile      = new File(rootPath);

        return SslContextBuilder.forClient().keyManager(certChainFile, keyFile).trustManager(rootFile).build();
    }

    public static SslContext getClientSslContext(String certChainPath, String keyPath, String keypass,String rootPath) throws SSLException {
        File certChainFile = new File(certChainPath),
            keyFile       = new File(keyPath),
            rootFile      = new File(rootPath);

        return SslContextBuilder.forClient().keyManager(certChainFile, keyFile, keypass).trustManager(rootFile).build();
    }

    public static SslContext getClientSslContext(File certChainFile, File keyFile, File rootFile) throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainFile, keyFile).trustManager(rootFile).build();
    }

    public static SslContext getClientSslContext(File certChainFile, File keyFile, String keypass, File rootFile) throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainFile, keyFile, keypass).trustManager(rootFile).build();
    }

    public static SslContext getClientSslContext(Path certChainPath, Path keyPath, Path rootPath) throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainPath.toFile(), keyPath.toFile()).trustManager(rootPath.toFile()).build();
    }

    public static SslContext getClientSslContext(Path certChainPath, Path keyPath, String keypass, Path rootPath) throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainPath.toFile(), keyPath.toFile(), keypass).trustManager(rootPath.toFile()).build();
    }

    public static SslContext getClientSslContext(InputStream certChainStream, InputStream keyStream, InputStream rootStream) throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainStream, keyStream).trustManager(rootStream).build();
    }

    public static SslContext getClientSslContext(InputStream certChainStream, InputStream keyStream, String keypass, InputStream rootStream) throws SSLException {
        return SslContextBuilder.forClient().keyManager(certChainStream, keyStream, keypass).trustManager(rootStream).build();
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

        private String cert;

        private String key;

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
