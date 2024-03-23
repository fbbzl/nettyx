package client.server;

import client.TestChannelInitializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.ServerSocketChannel;
import java.net.SocketAddress;
import java.util.Arrays;
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
    protected ChannelInitializer<ServerSocketChannel> childChannelInitializer() {
        return new TestChannelInitializer<>();
    }

    static byte[] msg = new byte[300];

    {
        Arrays.fill(msg, (byte) 1);
    }

    public static void main(String[] args) {
        TestServer testServer = new TestServer(9888);
        testServer.bind().channel().closeFuture().addListener(cf -> {
            System.err.println("关闭了");
            testServer.shutdownGracefully();
        });

    }
}
