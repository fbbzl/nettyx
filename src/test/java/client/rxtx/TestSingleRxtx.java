package client.rxtx;


import client.TestChannelInitializer;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.endpoint.client.rxtx.SingleRxtxChannelClient;
import org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannel;
import org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannelConfig;
import org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannelConfig.DataBits;
import org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannelConfig.ParityBit;
import org.fz.nettyx.endpoint.client.rxtx.support.RxtxChannelConfig.StopBits;
import org.fz.nettyx.listener.ActionChannelFutureListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.fz.nettyx.listener.ActionChannelFutureListener.redo;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:31
 */
public class TestSingleRxtx extends SingleRxtxChannelClient {

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
                .setDataBits(DataBits.DATABITS_8)
                .setStopBits(StopBits.STOPBITS_1)
                .setParityBit(ParityBit.NONE)
                .setDtr(false)
                .setRts(false);
    }

    public static void main(String[] args) {
        TestSingleRxtx testSingleRxtx = new TestSingleRxtx("COM3");
        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((l, cf) -> {
                    RxtxChannelConfig config =(RxtxChannelConfig) cf.channel().config();
                    System.err.println(config.getBaudRate());
                })
                .whenCancel((l, cf) -> System.err.println("cancel"))
                .whenFailure(redo(testSingleRxtx::connect, 2, SECONDS))
                .whenDone((l, cf) -> System.err.println("done"));

        testSingleRxtx.connect().addListener(listener);
    }
}
