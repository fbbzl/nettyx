package org.fz.nettyx.template.rxtx;


import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.fz.nettyx.channel.serial.SerialDataBits;
import org.fz.nettyx.channel.serial.SerialParityBit;
import org.fz.nettyx.channel.serial.SerialStopBits;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannel;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannelConfig;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.TestChannelInitializer;
import org.fz.nettyx.template.serial.rxtx.SingleRxtxChannelTemplate;

import java.util.concurrent.TimeUnit;

import static org.fz.nettyx.action.ListenerAction.redo;
import static org.fz.nettyx.codec.UserCodec.TEST_USER;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:31
 */
public class TestSingleRxtx extends SingleRxtxChannelTemplate {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(TestSingleRxtx.class);

    public TestSingleRxtx(String commAddress) {
        super(commAddress);
    }

    @Override
    protected ChannelInitializer<RxtxChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    @Override
    protected void doChannelConfig(RxtxChannelConfig channelConfig) {
        channelConfig
                .setBaudRate(115200)
                .setDataBits(SerialDataBits.DATA_BITS_8)
                .setStopBits(SerialStopBits.STOP_BITS_1)
                .setParityBit(SerialParityBit.NO)
                .setDtr(false)
                .setRts(false);
    }

    public static void main(String[] args) {
        TestSingleRxtx testSingleRxtx = new TestSingleRxtx("COM1");
        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((l, cf) -> {
                    testSingleRxtx.writeAndFlush(TEST_USER);

                    RxtxChannelConfig config = (RxtxChannelConfig) cf.channel().config();
                    log.info("baudRate: {}", config.getBaudRate());
                })
                .whenCancelled((l, cf) -> log.info("cancel"))
                .whenFailure(redo(testSingleRxtx::connect, 2, TimeUnit.MILLISECONDS, 3, (l, c) -> System.err.println(c.cause())))
                .whenDone((l, cf) -> log.info("done"));

        testSingleRxtx.connect().addListener(listener);
    }
}
