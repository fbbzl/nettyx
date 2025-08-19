package template.tcp.server;

import cn.hutool.core.lang.Console;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.template.tcp.server.ServerTemplate;
import template.TestChannelInitializer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/23 12:40
 */
public class TestServer extends ServerTemplate {

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
        bindFuture.addListener(cf -> Console.log("binding state:" + cf.isSuccess()));
        bindFuture.channel().closeFuture().addListener(cf -> {
            Console.log("关闭了");
            testServer.shutdownGracefully();
        });

    }
}
