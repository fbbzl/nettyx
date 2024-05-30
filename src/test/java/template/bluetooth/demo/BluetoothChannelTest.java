package template.bluetooth.demo;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 21:20
 */

import cn.hutool.core.util.StrUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.fz.nettyx.util.BtFinder;
import org.junit.Test;

import javax.bluetooth.RemoteDevice;
import java.util.List;

public class BluetoothChannelTest {

    @Test
    public void testConnect() {
        Bootstrap      bootstrap   = new Bootstrap();
        EventLoopGroup workerGroup = new OioEventLoopGroup();

        List<RemoteDevice> devices = new BtFinder.DeviceFinder().getDevices();
        String       url = StrUtil.format("btspp://{}:1;authenticate=false;encrypt=false;master=false", devices.get(0).getBluetoothAddress());
        bootstrap.group(workerGroup).channel(OioBluetoothChannel.class).remoteAddress(new BluetoothDeviceAddress(url))
                 .handler(new ChannelInitializer<OioBluetoothChannel>() {

                     @Override
                     public void initChannel(OioBluetoothChannel ch) {
                         ch.pipeline().addLast(new StringDecoder()).addLast(new StringEncoder());
                     }
                 });

        bootstrap.connect().syncUninterruptibly();

    }
}