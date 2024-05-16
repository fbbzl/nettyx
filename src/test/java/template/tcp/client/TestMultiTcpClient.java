package template.tcp.client;


import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.tcp.client.MultiTcpChannelClientTemplate;
import template.TestChannelInitializer;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.fz.nettyx.listener.ActionChannelFutureListener.redo;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 14:58
 */
public class TestMultiTcpClient extends MultiTcpChannelClientTemplate<String> {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    protected TestMultiTcpClient(Map<String, InetSocketAddress> inetSocketAddressMap) {
        super(inetSocketAddressMap);
    }

    @Override
    protected ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    public static void main(String[] args) {
        Map<String, InetSocketAddress> map = new HashMap<>();

        map.put("a", new InetSocketAddress(9888));
        map.put("b", new InetSocketAddress(9888));

        TestMultiTcpClient testMultiTcp = new TestMultiTcpClient(map);
        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((l, cf) -> {
                    executor.scheduleAtFixedRate(() -> {
                        byte[] msg = new byte[300];
                        Arrays.fill(msg, (byte) 1);
                        cf.channel().writeAndFlush(Unpooled.wrappedBuffer(msg));
                    }, 2, 30, TimeUnit.MILLISECONDS);

                    System.err.println(cf.channel().localAddress() + ": ok");
                })
                .whenCancel((l, cf) -> System.err.println("cancel"))
                .whenFailure(redo(cf -> testMultiTcp.connect(channelKey(cf)), 2, SECONDS))
                .whenDone((l, cf) -> System.err.println("done"));

        testMultiTcp.connectAll().values().forEach(c -> c.addListener(listener));

    }
}
