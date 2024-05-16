package template.jsc;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.channel.SerialCommChannel;
import org.fz.nettyx.channel.jsc.JscChannel;
import org.fz.nettyx.channel.jsc.JscChannelConfig;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.serial.jsc.MultiJscChannelTemplate;
import template.TestChannelInitializer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.fz.nettyx.listener.ActionChannelFutureListener.redo;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 22:58
 */
public class TestMultiJsc extends MultiJscChannelTemplate<String> {

    static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public TestMultiJsc(Map<String, SerialCommChannel.SerialCommAddress> stringJscDeviceAddressMap) {
        super(stringJscDeviceAddressMap);
    }

    @Override
    protected ChannelInitializer<JscChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }


    @Override
    protected void doChannelConfig(String targetChannelKey, JscChannelConfig channelConfig) {
        // if(targetChannelKey=="MES") {br=19200}
        channelConfig
               .setBaudRate(115200)
               .setDataBits(JscChannelConfig.DataBits.DATABITS_8)
               .setStopBits(JscChannelConfig.StopBits.ONE_STOP_BIT)
               .setParityBit(JscChannelConfig.ParityBit.NO_PARITY)
               .setDtr(false)
               .setRts(false);
    }

    public static void main(String[] args) {
        Map<String, SerialCommChannel.SerialCommAddress> map = new HashMap<>();

        map.put("5", new SerialCommChannel.SerialCommAddress("COM5"));
        map.put("6", new SerialCommChannel.SerialCommAddress("COM6"));

        TestMultiJsc testMultiJsc = new TestMultiJsc(map);
        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((l, cf) -> {
                    executor.scheduleAtFixedRate(() -> {
                        byte[] msg = new byte[300];
                        Arrays.fill(msg, (byte) 1);
                        cf.channel().writeAndFlush(Unpooled.wrappedBuffer(msg));
                    }, 2, 30, TimeUnit.MILLISECONDS);

                    System.err.println(cf.channel().localAddress() + ": ok");
                })
                .whenCancel((l, cf) -> System.err.println("cancel"))
                .whenFailure(redo(cf -> testMultiJsc.connect(channelKey(cf)), 2, SECONDS))
                .whenDone((l, cf) -> System.err.println("done"));

        testMultiJsc.connectAll().values().forEach(c -> c.addListener(listener));

        // send msg
        testMultiJsc.write("5", "this is msg from 5 write");
        testMultiJsc.writeAndFlush("6", "this is msg from 6 writeAndFlush");
    }

}
