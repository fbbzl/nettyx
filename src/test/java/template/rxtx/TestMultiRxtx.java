package template.rxtx;

import cn.hutool.core.lang.Console;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.channel.serial.SerialCommChannel;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannel;
import org.fz.nettyx.channel.serial.rxtx.RxtxChannelConfig;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.serial.rxtx.MultiRxtxChannelTemplate;
import template.TestChannelInitializer;

import java.util.HashMap;
import java.util.Map;

import static codec.UserCodec.TEST_USER;
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
    protected void doChannelConfig(String channelKey, RxtxChannelConfig channelConfig) {
        // if(targetChannelKey=="MES") {br=19200}
        channelConfig
                .setBaudRate(115200)
                .setDataBits(RxtxChannelConfig.DataBits.DATA_BITS_8)
                .setStopBits(RxtxChannelConfig.StopBits.STOP_BITS_1)
                .setParityBit(RxtxChannelConfig.ParityBit.NO)
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
                    cf.channel().writeAndFlush(TEST_USER);

                    Console.log(cf.channel().localAddress() + ": ok");
                })
                .whenCancelled((l, cf) -> Console.log("cancel"))
                .whenFailure(redo(cf -> testMultiTcp.connect(channelKey(cf)), 2, SECONDS))
                .whenDone((l, cf) -> Console.log("done"));

        testMultiTcp.connectAll().values().forEach(c -> c.addListener(listener));
    }
}
