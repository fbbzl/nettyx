package client.jsc;


import client.TestChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.jsc.SingleJscChannelClient;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannel;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannelConfig.ParityBit;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannelConfig.StopBits;
import org.fz.nettyx.listener.ActionChannelFutureListener;

import java.net.SocketAddress;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.netty.channel.rxtx.RxtxChannelOption.DTR;
import static io.netty.channel.rxtx.RxtxChannelOption.RTS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.fz.nettyx.endpoint.client.jsc.support.JscChannelOption.*;
import static org.fz.nettyx.listener.ActionChannelFutureListener.redo;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/28 14:49
 */

@Slf4j
public class TestSingleJsc extends SingleJscChannelClient {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public TestSingleJsc(String commAddress) {
        super(commAddress);
    }

    public static void main(String[] args) {
        TestSingleJsc testSingleJsc = new TestSingleJsc("COM2");
        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((l, cf) -> {
                    executor.scheduleAtFixedRate(() -> {
                        byte[] msg = new byte[300];
                        Arrays.fill(msg, (byte) 1);
                        testSingleJsc.writeAndFlush(Unpooled.wrappedBuffer(msg));
                    }, 2, 30, TimeUnit.MILLISECONDS);

                    System.err.println(cf.channel().localAddress() + ": ok");
                })
                .whenCancel((l, cf) -> System.err.println("cancel"))
                .whenFailure(redo(testSingleJsc::connect, 2, SECONDS))
                .whenDone((l, cf) -> System.err.println("done"));

        testSingleJsc.connect().addListener(listener);
    }

    @Override
    protected ChannelInitializer<JscChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    @Override
    protected Bootstrap newBootstrap(SocketAddress jscDeviceAddress) {
        return super.newBootstrap(jscDeviceAddress)
                    .option(BAUD_RATE, 115200)
                    .option(DATA_BITS, 8)
                    .option(STOP_BITS, StopBits.ONE_STOP_BIT)
                    .option(PARITY_BIT, ParityBit.NO_PARITY)
                    .option(DTR, false)
                    .option(RTS, false);
    }

}
