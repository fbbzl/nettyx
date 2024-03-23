package client.server;

import client.TestChannelInitializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.ServerSocketChannel;
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

    @Override
    protected ChannelInitializer<ServerSocketChannel> childChannelInitializer() {
        return new TestChannelInitializer<>();
    }

}
