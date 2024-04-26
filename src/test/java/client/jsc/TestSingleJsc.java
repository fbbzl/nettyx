package client.jsc;


import client.TestChannelInitializer;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.jsc.SingleJscChannelClient;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannel;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannelConfig;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannelConfig.ParityBit;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannelConfig.StopBits;
import org.fz.nettyx.listener.ActionChannelFutureListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;
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

    @Override
    protected ChannelInitializer<JscChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    @Override
    protected void doChannelConfig(JscChannelConfig channelConfig) {
        channelConfig
                .setBaudRate(115200)
                .setDataBits(8)
                .setStopBits(StopBits.ONE_STOP_BIT)
                .setParityBit(ParityBit.NO_PARITY)
                .setDtr(false)
                .setRts(false);
    }

    public static void main(String[] args) {
        TestSingleJsc testSingleJsc = new TestSingleJsc("COM2");
        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((l, cf) -> {
                    JscChannelConfig config =(JscChannelConfig) cf.channel().config();
                    System.err.println(config.getBaudRate());
                })
                .whenCancel((l, cf) -> System.err.println("cancel"))
                .whenFailure(redo(testSingleJsc::connect, 2, SECONDS))
                .whenDone((l, cf) -> System.err.println("done"));

        testSingleJsc.connect().addListener(listener);

    }
}
