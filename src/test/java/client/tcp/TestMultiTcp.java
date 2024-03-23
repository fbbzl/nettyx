package client.tcp;


import client.TestChannelInitializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.fz.nettyx.endpoint.client.tcp.MultiTcpChannelClient;
import org.fz.nettyx.listener.ActionChannelFutureListener;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 14:58
 */
public class TestMultiTcp extends MultiTcpChannelClient<String> {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    protected TestMultiTcp(Map<String, InetSocketAddress> inetSocketAddressMap) {
        super(inetSocketAddressMap);
    }

    @Override
    protected ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    @Override
    protected void doChannelConfig(String key, SocketChannelConfig channelConfig) {
        super.doChannelConfig(key, channelConfig);
    }

    public static void main(String[] args) {
        Map<String, InetSocketAddress> map = new HashMap<>();

        map.put("a", new InetSocketAddress("127.0.0.1", 9081));
        map.put("b", new InetSocketAddress("127.0.0.1", 9081));

        TestMultiTcp testMultiTcp = new TestMultiTcp(map);
        ChannelFutureListener listener = new ActionChannelFutureListener()
            .whenSuccess(cf -> {
                executor.scheduleAtFixedRate(() -> {
                    byte[] msg = new byte[300];
                    Arrays.fill(msg, (byte) 1);
                    testSingleRxtx.writeAndFlush(Unpooled.wrappedBuffer(msg));
                }, 2, 30, TimeUnit.MILLISECONDS);

                System.err.println(cf.channel().localAddress() + ": ok");
            })
            .whenCancel(cf -> System.err.println("cancel"))
            .whenFailure(cf -> {
                System.err.println(cf.channel().localAddress() + ": fail, " + cf.cause());
                cf.channel().eventLoop()
                  .schedule(testSingleRxtx::connect, 2, TimeUnit.SECONDS);
            })
            .whenDone(cf -> System.err.println("done"));

        Map<String, ChannelFuture> stringChannelFutureMap = testMultiTcp.connectAll();

    }
}
