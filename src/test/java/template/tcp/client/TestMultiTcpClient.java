package template.tcp.client;


import cn.hutool.core.lang.Console;
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
import static org.fz.nettyx.action.ListenerAction.redo;


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
        Map<String, InetSocketAddress> map        = new HashMap<>();
        InetSocketAddress              serverAddr = new InetSocketAddress(9888);
        for (int i = 0; i < 32; i++) {
            map.put("b" + i, serverAddr);
        }

        TestMultiTcpClient testMultiTcp = new TestMultiTcpClient(map);
        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((l, cf) -> {
                    executor.scheduleAtFixedRate(() -> {
                        byte[] msg = new byte[2048];
                        Arrays.fill(msg, (byte) 67);
                        cf.channel().writeAndFlush(Unpooled.wrappedBuffer(msg));
                    }, 2, 30, TimeUnit.MILLISECONDS);

                    Console.log(cf.channel().localAddress() + ": ok");
                })
                .whenCancelled((l, cf) -> Console.log("cancel"))
                .whenFailure(redo(cf -> testMultiTcp.connect(channelKey(cf)), 2, SECONDS, 2, (l, cf) -> {
                    String string = channelKey(cf).toString();
                    System.err.println(string);
                }))
                .whenDone((l, cf) -> Console.log("done"));

        testMultiTcp.connectAll().values().forEach(c -> c.addListener(listener));

    }
}
