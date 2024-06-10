package template.rxtx;


import cn.hutool.core.lang.Console;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.channel.rxtx.RxtxChannel;
import org.fz.nettyx.channel.rxtx.RxtxChannelConfig;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.serial.rxtx.SingleRxtxChannellTemplate;
import template.TestChannelInitializer;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.fz.nettyx.action.ListenerAction.redo;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:31
 */
public class TestSingleRxtx extends SingleRxtxChannellTemplate {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

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
                .setDataBits(RxtxChannelConfig.DataBits.DATABITS_8)
                .setStopBits(RxtxChannelConfig.StopBits.STOPBITS_1)
                .setParityBit(RxtxChannelConfig.ParityBit.NONE)
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

                    RxtxChannelConfig config =(RxtxChannelConfig) cf.channel().config();
                    Console.log(config.getBaudRate());
                })
                .whenCancelled((l, cf) -> Console.log("cancel"))
                .whenFailure(redo(testSingleRxtx::connect, 2, SECONDS))
                .whenDone((l, cf) -> Console.log("done"));

        testSingleRxtx.connect().addListener(listener);
    }
}
