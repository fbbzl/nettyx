package client.server;

import client.TestChannelInitializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.ServerSocketChannel;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.fz.nettyx.endpoint.server.TcpServer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/23 12:40
 */
public class TestServer extends TcpServer {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

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

    public static void main(String[] args) {
        TestServer testServer = new TestServer(9999);
        Channel    channel    = testServer.bind().channel();
        channel.closeFuture().addListener(cf -> testServer.shutdownGracefully());
        System.err.println(channel);
        executor.scheduleAtFixedRate(() -> {
            byte[] msg = new byte[300];
            Arrays.fill(msg, (byte) 1);
            channel.writeAndFlush(Unpooled.wrappedBuffer(msg));
        }, 2, 30, TimeUnit.MILLISECONDS);
    }
}
