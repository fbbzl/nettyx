package template.bluetooth.demo;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 21:20
 */

import cn.hutool.core.lang.Console;
import cn.hutool.core.util.StrUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.fz.nettyx.channel.bluetooth.BtDeviceAddress;
import org.fz.nettyx.channel.bluetooth.client.BtChannel;
import org.fz.nettyx.channel.bluetooth.finder.BtFinder;
import template.DebugChannelListener;

import javax.bluetooth.RemoteDevice;
import java.io.IOException;
import java.util.List;

public class BluetoothChannelTest {

    public static void main(String[] args) throws IOException {
        Bootstrap      bootstrap   = new Bootstrap();
        EventLoopGroup workerGroup = new OioEventLoopGroup();

        List<RemoteDevice> devices = new BtFinder.DeviceFinder().getDevices();
        RemoteDevice       remoteDevice = devices.get(0);
        System.err.println(remoteDevice);
        Console.log(remoteDevice.getFriendlyName(false));

        String       url = StrUtil.format("btspp://{}:1;authenticate=false;encrypt=false;master=false", remoteDevice.getBluetoothAddress());
        bootstrap.group(workerGroup).channel(BtChannel.class).remoteAddress(new BtDeviceAddress(url))
                 .handler(new ChannelInitializer<BtChannel>() {

                     @Override
                     public void initChannel(BtChannel ch) {
                         ch.pipeline().addLast(new StringDecoder()).addLast(new StringEncoder());
                     }
                 });

        bootstrap.connect().addListener(new DebugChannelListener());
    }

}