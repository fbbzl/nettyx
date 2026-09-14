package org.fz.nettyx.template.tcp.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

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
}
