package template.jsc;

import cn.hutool.core.lang.Console;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.channel.serial.SerialCommChannel;
import org.fz.nettyx.channel.serial.jsc.JscChannel;
import org.fz.nettyx.channel.serial.jsc.JscChannelConfig;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.template.serial.jsc.MultiJscChannelTemplate;
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
public class TestMultiJsc extends MultiJscChannelTemplate<String> {

	public TestMultiJsc(Map<String, SerialCommChannel.SerialCommAddress> stringJscDeviceAddressMap) {
		super(stringJscDeviceAddressMap);
	}

	@Override
	protected ChannelInitializer<JscChannel> channelInitializer() {
		return new TestChannelInitializer<>();
	}

	@Override
	protected void doChannelConfig(String channelKey, JscChannelConfig channelConfig) {
		// if(targetChannelKey=="MES") {br=19200}
		channelConfig
			.setBaudRate(115200)
			.setDataBits(JscChannelConfig.DataBits.DATA_BITS_8)
			.setStopBits(JscChannelConfig.StopBits.STOP_BITS_1)
			.setParityBit(JscChannelConfig.ParityBit.NO)
			.setDtr(false)
			.setRts(false);
	}

	public static void main(String[] args) {
		Map<String, SerialCommChannel.SerialCommAddress> map = new HashMap<>();

		map.put("5", new SerialCommChannel.SerialCommAddress("COM5"));
		map.put("6", new SerialCommChannel.SerialCommAddress("COM7"));

		TestMultiJsc testMultiJsc = new TestMultiJsc(map);
		ChannelFutureListener listener = new ActionChannelFutureListener()
			.whenSuccess((l, cf) -> {
				cf.channel().writeAndFlush(TEST_USER);

				Console.log(cf.channel().localAddress() + ": ok");
			})
			.whenCancelled((l, cf) -> Console.log("cancel"))
			.whenFailure(redo(cf -> testMultiJsc.connect(channelKey(cf)), 2, SECONDS))
			.whenDone((l, cf) -> Console.log("done"));

		testMultiJsc.connectAll().values().forEach(c -> c.addListener(listener));

		// send msg
		testMultiJsc.write("5", "this is msg from 5 write");
		testMultiJsc.writeAndFlush("6", "this is msg from 6 writeAndFlush");
	}

}
