package client.server;

import client.TestChannelInitializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.SocketAddress;
import org.fz.nettyx.endpoint.server.TcpServer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/23 12:40
 */
public class TestServer extends TcpServer {

    public TestServer(SocketAddress bindAddress) {
        super(bindAddress);
    }

    public TestServer(int bindPort) {
        super(bindPort);
    }

    @Override
    protected ChannelInitializer<NioServerSocketChannel> childChannelInitializer() {
        return new TestChannelInitializer<>();
    }

    public static void main(String[] args) {
        TestServer testServer = new TestServer(9888);
        testServer.bind().channel().closeFuture().addListener(cf -> {
            System.err.println("关闭了");
            testServer.shutdownGracefully();
        });

    }
}
