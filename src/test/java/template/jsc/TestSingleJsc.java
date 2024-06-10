package template.jsc;


import cn.hutool.core.lang.Console;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.channel.jsc.JscChannel;
import org.fz.nettyx.channel.jsc.JscChannelConfig;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.serial.jsc.SingleJscChannelTemplate;
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
 * @since 2024/2/28 14:49
 */

@Slf4j
public class TestSingleJsc extends SingleJscChannelTemplate {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

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
                .setDataBits(JscChannelConfig.DataBits.DATABITS_8)
                .setStopBits(JscChannelConfig.StopBits.ONE_STOP_BIT)
                .setParityBit(JscChannelConfig.ParityBit.NO_PARITY)
                .setDtr(false)
                .setRts(false);
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

                    JscChannelConfig config = (JscChannelConfig) cf.channel().config();
                    Console.log(config.getBaudRate());
                })
                .whenCancelled((l, cf) -> Console.log("cancel"))
                .whenFailure(redo(testSingleJsc::connect, 2, SECONDS))
                .whenDone((l, cf) -> Console.log("done"));

        testSingleJsc.connect().addListener(listener);

        // send msg
        testSingleJsc.write("this is msg from 5 write");
        testSingleJsc.writeAndFlush("this is msg from 6 writeAndFlush");
    }
}
