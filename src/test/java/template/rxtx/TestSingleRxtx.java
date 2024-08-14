package template.rxtx;


import cn.hutool.core.lang.Console;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.channel.rxtx.RxtxChannel;
import org.fz.nettyx.channel.rxtx.RxtxChannelConfig;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.serial.rxtx.SingleRxtxChannelTemplate;
import template.TestChannelInitializer;

import java.util.concurrent.TimeUnit;

import static codec.UserCodec.TEST_USER;
import static org.fz.nettyx.action.ListenerAction.redo;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:31
 */
public class TestSingleRxtx extends SingleRxtxChannelTemplate {

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
                .setDataBits(RxtxChannelConfig.DataBits.DATA_BITS_8)
                .setStopBits(RxtxChannelConfig.StopBits.STOP_BITS_1)
                .setParityBit(RxtxChannelConfig.ParityBit.NO)
                .setDtr(false)
                .setRts(false);
    }

    public static void main(String[] args) {
        TestSingleRxtx testSingleRxtx = new TestSingleRxtx("COM5");
        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((l, cf) -> {
                    testSingleRxtx.writeAndFlush(TEST_USER);

                    RxtxChannelConfig config = (RxtxChannelConfig) cf.channel().config();
                    Console.log(config.getBaudRate());
                })
                .whenCancelled((l, cf) -> Console.log("cancel"))
                .whenFailure(redo(testSingleRxtx::connect, 2, TimeUnit.MILLISECONDS, 3, (l, c) -> System.err.println(c.cause())))
                .whenDone((l, cf) -> Console.log("done"));

        testSingleRxtx.connect().addListener(listener);
    }
}
