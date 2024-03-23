package client.jsc;

import client.TestChannelInitializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.fz.nettyx.endpoint.client.jsc.MultiJscChannelClient;
import org.fz.nettyx.endpoint.client.jsc.support.JscDeviceAddress;
import org.fz.nettyx.listener.ActionChannelFutureListener;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 22:58
 */
public class TestMultiJsc extends MultiJscChannelClient<String> {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public TestMultiJsc(Map<String, JscDeviceAddress> stringJscDeviceAddressMap) {
        super(stringJscDeviceAddressMap);
    }

    @Override
    protected ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }


    public static void main(String[] args) {
        Map<String, JscDeviceAddress> map = new HashMap<>();

        map.put("5", new JscDeviceAddress("COM5"));
        map.put("6", new JscDeviceAddress("COM6"));

        TestMultiJsc testMultiJsc = new TestMultiJsc(map);
        ChannelFutureListener listener = new ActionChannelFutureListener()
            .whenSuccess(cf -> {
                executor.scheduleAtFixedRate(() -> {
                    byte[] msg = new byte[300];
                    Arrays.fill(msg, (byte) 1);
                    cf.channel().writeAndFlush(Unpooled.wrappedBuffer(msg));
                }, 2, 30, TimeUnit.MILLISECONDS);

                System.err.println(cf.channel().localAddress() + ": ok");
            })
            .whenCancel(cf -> System.err.println("cancel"))
            .whenFailure(cf -> {
                System.err.println(cf.channel().localAddress() + ": fail, " + cf.cause());
                cf.channel().eventLoop().schedule(() -> testMultiJsc.connect(channelKey(cf)), 2, TimeUnit.SECONDS);
            })
            .whenDone(cf -> System.err.println("done"));

        testMultiJsc.connectAll().values().forEach(c -> c.addListener(listener)); ;
    }


}
