package client.rxtx;


import static io.netty.channel.rxtx.RxtxChannelOption.BAUD_RATE;
import static io.netty.channel.rxtx.RxtxChannelOption.DATA_BITS;
import static io.netty.channel.rxtx.RxtxChannelOption.DTR;
import static io.netty.channel.rxtx.RxtxChannelOption.PARITY_BIT;
import static io.netty.channel.rxtx.RxtxChannelOption.RTS;
import static io.netty.channel.rxtx.RxtxChannelOption.STOP_BITS;

import client.TestChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.rxtx.RxtxChannelConfig.Databits;
import io.netty.channel.rxtx.RxtxChannelConfig.Paritybit;
import io.netty.channel.rxtx.RxtxChannelConfig.Stopbits;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.fz.nettyx.endpoint.client.rxtx.SingleRxtxChannelClient;
import org.fz.nettyx.endpoint.client.rxtx.support.XRxtxChannel;
import org.fz.nettyx.listener.ActionableChannelFutureListener;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:31
 */
public class TestSingleRxtx extends SingleRxtxChannelClient {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected ChannelInitializer<XRxtxChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    @Override
    protected Bootstrap newBootstrap() {
        return super.newBootstrap()
                    .option(BAUD_RATE, 115200)
                    .option(DATA_BITS, Databits.DATABITS_8)
                    .option(STOP_BITS, Stopbits.STOPBITS_1)
                    .option(PARITY_BIT, Paritybit.NONE)
                    .option(DTR, false)
                    .option(RTS, false);
    }

    public static void main(String[] args) {
        TestSingleRxtx testSingleRxtx = new TestSingleRxtx();
        ChannelFutureListener listener = new ActionableChannelFutureListener()
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
                  .schedule(() -> testSingleRxtx.connect(cf.channel().remoteAddress()), 2, TimeUnit.SECONDS);
            })
            .whenDone(cf -> System.err.println("done"));
        testSingleRxtx.connect("COM3").addListener(listener);
    }
}
