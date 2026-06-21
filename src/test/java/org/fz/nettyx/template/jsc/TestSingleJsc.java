package org.fz.nettyx.template.jsc;


import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.fz.nettyx.channel.serial.SerialDataBits;
import org.fz.nettyx.channel.serial.SerialParityBit;
import org.fz.nettyx.channel.serial.SerialStopBits;
import org.fz.nettyx.channel.serial.jsc.JscChannel;
import org.fz.nettyx.channel.serial.jsc.JscChannelConfig;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.TestChannelInitializer;
import org.fz.nettyx.template.serial.jsc.SingleJscChannelTemplate;

import java.util.concurrent.TimeUnit;

import static org.fz.nettyx.action.ListenerAction.redo;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/28 14:49
 */

public class TestSingleJsc extends SingleJscChannelTemplate {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(TestSingleJsc.class);

    public TestSingleJsc(String commAddress) {
        super(commAddress);
    }

    @Override
    protected ChannelInitializer<JscChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    @Override
    protected void doChannelConfig(JscChannelConfig channelConfig) {
        channelConfig
                .setBaudRate(115200)
                .setDataBits(SerialDataBits.DATA_BITS_8)
                .setStopBits(SerialStopBits.STOP_BITS_1)
                .setParityBit(SerialParityBit.NO)
                .setDtr(false)
                .setRts(false);
    }

    public static void main(String[] args) {
        TestSingleJsc testSingleJsc = new TestSingleJsc("COM2");
        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((l, cf) -> {

                })
                .whenCancelled((l, cf) -> log.info("cancel"))
                .whenFailure(redo(testSingleJsc::connect, 2, TimeUnit.MILLISECONDS, 3, (l, c) -> {

                    c.cause().printStackTrace();
                }))
                .whenDone((l, cf) -> log.info("done"));

        testSingleJsc.connect().addListener(listener);

        // send msg
        testSingleJsc.write("this is msg from 5 write");
        testSingleJsc.writeAndFlush("this is msg from 6 writeAndFlush");
    }
}
