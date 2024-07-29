package template.rxtx;

import cn.hutool.core.lang.Console;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.channel.SerialCommChannel;
import org.fz.nettyx.channel.rxtx.RxtxChannel;
import org.fz.nettyx.channel.rxtx.RxtxChannelConfig;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.serial.rxtx.MultiRxtxChannelTemplate;
import template.TestChannelInitializer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.fz.nettyx.action.ListenerAction.redo;


/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 22:58
 */
public class TestMultiRxtx extends MultiRxtxChannelTemplate<String> {

    protected TestMultiRxtx(Map<String, SerialCommChannel.SerialCommAddress> stringRxtxDeviceAddressMap) {
        super(stringRxtxDeviceAddressMap);
    }

    @Override
    protected void doChannelConfig(String targetChannelKey, RxtxChannelConfig channelConfig) {
        // if(targetChannelKey=="MES") {br=19200}
        channelConfig
                .setBaudRate(115200)
                .setDataBits(RxtxChannelConfig.DataBits.DATABITS_8)
                .setStopBits(RxtxChannelConfig.StopBits.STOPBITS_1)
                .setParityBit(RxtxChannelConfig.ParityBit.NONE)
                .setDtr(false)
                .setRts(false);
    }

    @Override
    protected ChannelInitializer<RxtxChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    public static void main(String[] args) {
        Map<String, SerialCommChannel.SerialCommAddress> map = new HashMap<>();

        map.put("5", new SerialCommChannel.SerialCommAddress("COM2"));
        map.put("6", new SerialCommChannel.SerialCommAddress("COM6"));

        TestMultiRxtx testMultiTcp = new TestMultiRxtx(map);
        ChannelFutureListener listener = new ActionChannelFutureListener()
                .whenSuccess((l, cf) -> {
                    byte[] msg = new byte[2048];
                    Arrays.fill(msg, (byte) 67);
                    cf.channel().writeAndFlush(Unpooled.wrappedBuffer(msg));

                    Console.log(cf.channel().localAddress() + ": ok");
                })
                .whenCancelled((l, cf) -> Console.log("cancel"))
                .whenFailure(redo(cf -> testMultiTcp.connect(channelKey(cf)), 2, SECONDS))
                .whenDone((l, cf) -> Console.log("done"));

        testMultiTcp.connectAll().values().forEach(c -> c.addListener(listener));
    }
}
