package client.jsc;


import static io.netty.channel.rxtx.RxtxChannelOption.DTR;
import static io.netty.channel.rxtx.RxtxChannelOption.RTS;
import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.BAUD_RATE;
import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.DATA_BITS;
import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.PARITY_BIT;
import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.STOP_BITS;

import client.TestChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.jsc.SingleJscChannelClient;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannel;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannelConfig.ParityBit;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannelConfig.StopBits;
import org.fz.nettyx.listener.ActionableChannelFutureListener;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/28 14:49
 */

@Slf4j
public class TestSingleJsc extends SingleJscChannelClient {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    @Override
    protected ChannelInitializer<JscChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }


    @Override
    protected Bootstrap newBootstrap() {
        return super.newBootstrap()
                    .option(BAUD_RATE, 115200)
                    .option(DATA_BITS, 8)
                    .option(STOP_BITS, StopBits.ONE_STOP_BIT)
                    .option(PARITY_BIT, ParityBit.NO_PARITY)
                    .option(DTR, false)
                    .option(RTS, false);
    }

    public static void main(String[] args) {
        TestSingleJsc testSingleJsc = new TestSingleJsc();
        ChannelFutureListener listener = new ActionableChannelFutureListener()
            .whenSuccess(cf -> {
                executor.scheduleAtFixedRate(() -> {
                    byte[] msg = new byte[300];
                    Arrays.fill(msg, (byte) 1);
                    testSingleJsc.writeAndFlush(Unpooled.wrappedBuffer(msg));
                }, 2, 30, TimeUnit.MILLISECONDS);

                System.err.println(cf.channel().localAddress() + ": ok");
            })
            .whenCancel(cf -> System.err.println("cancel"))
            .whenFailure(cf -> {
                System.err.println(cf.channel().localAddress() + ": fail, " + cf.cause());
                cf.channel().eventLoop()
                  .schedule(() -> testSingleJsc.connect(cf.channel().remoteAddress()), 2, TimeUnit.SECONDS);
            })
            .whenDone(cf -> System.err.println("done"));

        testSingleJsc.connect("COM2").addListener(listener);
    }

}
