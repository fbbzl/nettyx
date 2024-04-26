package client.rxtx;


import client.TestChannelInitializer;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.endpoint.client.rxtx.SingleRxtxChannelClient;
import org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannel;
import org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannelConfig.Databits;
import org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannelConfig.Paritybit;
import org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannelConfig.Stopbits;
import org.fz.nettyx.listener.ActionChannelFutureListener;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.fz.nettyx.listener.ActionChannelFutureListener.redo;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:31
 */
public class TestSingleRxtx extends SingleRxtxChannelClient {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public TestSingleRxtx(String commAddress) {
        super(commAddress);
    }

    @Override
    protected ChannelInitializer<RxtxChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    @Override
    protected void doChannelConfig(RxtxChannel channel) {
        channel.config()
               .setBaudRate(115200)
               .setDataBits(Databits.DATABITS_8)
               .setStopBits(Stopbits.STOPBITS_1)
               .setParityBit(Paritybit.NONE)
               .setDtr(false)
               .setRts(false);
    }

    public static void main(String[] args) {
        TestSingleRxtx testSingleRxtx = new TestSingleRxtx("COM3");
        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((l, cf) -> {
                    executor.scheduleAtFixedRate(() -> {
                        byte[] msg = new byte[300];
                        Arrays.fill(msg, (byte) 1);
                        testSingleRxtx.writeAndFlush(Unpooled.wrappedBuffer(msg));
                    }, 2, 30, TimeUnit.MILLISECONDS);

                    System.err.println(cf.channel().localAddress() + ": ok");
                })
                .whenCancel((l, cf) -> System.err.println("cancel"))
                .whenFailure(redo(testSingleRxtx::connect, 2, SECONDS))
                .whenDone((l, cf) -> System.err.println("done"));

        testSingleRxtx.connect().addListener(listener);
    }
}
