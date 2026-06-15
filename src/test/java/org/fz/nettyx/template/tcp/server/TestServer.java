package org.fz.nettyx.template.tcp.server;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.fz.nettyx.template.TestChannelInitializer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/23 12:40
 */
public class TestServer extends ServerTemplate {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(TestServer.class);

    public TestServer(int bindPort) {
        super(bindPort);
    }

    @Override
    protected ChannelInitializer<NioSocketChannel> childChannelInitializer() {
        return new TestChannelInitializer<>();
    }

    public static void main(String[] args) {
        TestServer    testServer = new TestServer(9888);
        ChannelFuture bindFuture = testServer.bind();
        bindFuture.addListener(cf -> log.info("binding state:" + cf.isSuccess()));
        bindFuture.channel().closeFuture().addListener(cf -> {
            log.info("关闭了");
            testServer.shutdownGracefully();
        });

    }
}
