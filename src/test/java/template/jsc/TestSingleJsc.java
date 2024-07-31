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
import java.util.concurrent.TimeUnit;

import static org.fz.nettyx.action.ListenerAction.redo;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/28 14:49
 */

@Slf4j
public class TestSingleJsc extends SingleJscChannelTemplate {

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
                .setDataBits(JscChannelConfig.DataBits.DATA_BITS_8)
                .setStopBits(JscChannelConfig.StopBits.STOP_BITS_1)
                .setParityBit(JscChannelConfig.ParityBit.NO)
                .setDtr(false)
                .setRts(false);
    }

    public static void main(String[] args) {
        TestSingleJsc testSingleJsc = new TestSingleJsc("COM2");
        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((l, cf) -> {
                    byte[] msg = new byte[2048];
                    Arrays.fill(msg, (byte) 67);
                    testSingleJsc.writeAndFlush(Unpooled.wrappedBuffer(msg));
                })
                .whenCancelled((l, cf) -> Console.log("cancel"))
                .whenFailure(redo(testSingleJsc::connect, 2, TimeUnit.MILLISECONDS, 3, (l, c) -> {

                    c.cause().printStackTrace();
                }))
                .whenDone((l, cf) -> Console.log("done"));

        testSingleJsc.connect().addListener(listener);

        // send msg
        testSingleJsc.write("this is msg from 5 write");
        testSingleJsc.writeAndFlush("this is msg from 6 writeAndFlush");
    }
}
