package org.fz.nettyx.template.tcp.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;

/**
 * Checks TCP server binding, event loop shutdown, and cleanup after configuration failure.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-09-14
 */
public class ServerTemplateCoverageTest {

    @Test
    public void bindsAndShutsDownUsingConfiguredEventLoops() throws Exception {
        TestServer server = new TestServer(0);
        try {
            assertTrue(server.parentEventLoopGroup() != null);
            assertTrue(server.childEventLoopGroup() != null);
            ChannelFuture bound = server.bind().sync();
            assertTrue(bound.isSuccess());
            assertTrue(bound.channel().isOpen());
            bound.channel().close().sync();
            server.syncShutdownGracefully();
        }
        finally {
            server.shutdownGracefully();
        }
    }

    @Test
    public void failedServerChannelConfigurationClosesCreatedChannel() throws Exception
    {
        FailingServer server = new FailingServer();
        try {
            ChannelFuture bind = server.bind().await();
            assertFalse(bind.isSuccess());
            assertFalse(server.created.isOpen());
        }
        finally {
            server.shutdownGracefully();
        }
    }

    /**
     * Supplies an inert child channel initializer and exposes shutdown for server template tests.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-09-14
     */
    private static final class TestServer extends ServerTemplate {
        private TestServer(int port) {
            super(port);
        }

        @Override
        protected ChannelInitializer<NioSocketChannel> childChannelInitializer() {
            return new ChannelInitializer<>() {
                @Override
                protected void initChannel(NioSocketChannel channel) {
                }
            };
        }

        public void syncShutdownGracefully() throws InterruptedException {
            super.syncShutdownGracefully();
        }

        public void shutdownGracefully() {
            super.shutdownGracefully();
        }
    }

    /**
     * Throws during server channel configuration so tests can verify channel cleanup.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-14
     */
    private static final class FailingServer extends ServerTemplate
    {
        private volatile ServerSocketChannel created;

        private FailingServer()
        {
            super(0);
        }

        @Override protected EventLoopGroup parentEventLoopGroup() { return new NioEventLoopGroup(1); }
        @Override protected EventLoopGroup childEventLoopGroup() { return new NioEventLoopGroup(1); }
        @Override protected ChannelInitializer<NioSocketChannel> childChannelInitializer() {
            return new ChannelInitializer<>() { @Override protected void initChannel(NioSocketChannel channel) { } };
        }
        @Override protected void doChannelConfig(ServerSocketChannel channel) {
            created = channel;
            throw new IllegalStateException("config failed");
        }
        public void shutdownGracefully() { super.shutdownGracefully(); }
    }
}
