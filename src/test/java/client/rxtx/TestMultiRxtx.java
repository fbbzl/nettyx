package client.rxtx;

import client.TestChannelInitializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.endpoint.client.rxtx.MultiRxtxChannelClient;
import org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannel;
import org.fz.nettyx.listener.ActionChannelFutureListener;

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
 * @since 2024/3/1 22:58
 */
public class TestMultiRxtx extends MultiRxtxChannelClient<String> {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    protected TestMultiRxtx(Map<String, RxtxDeviceAddress> stringRxtxDeviceAddressMap) {
        super(stringRxtxDeviceAddressMap);
    }

    @Override
    protected ChannelInitializer<RxtxChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    public static void main(String[] args) {
        Map<String, RxtxDeviceAddress> map = new HashMap<>();

        map.put("5", new RxtxDeviceAddress("COM5"));
        map.put("6", new RxtxDeviceAddress("COM6"));

        TestMultiRxtx testMultiTcp = new TestMultiRxtx(map);
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
