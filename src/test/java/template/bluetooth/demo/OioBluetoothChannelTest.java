package template.bluetooth.demo;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/5/30 21:20
 */

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.junit.Test;

public class OioBluetoothChannelTest {

    @Test
    public void testConnect() {

        Bootstrap      bootstrap   = new Bootstrap();
        EventLoopGroup workerGroup = new OioEventLoopGroup();

        bootstrap.group(workerGroup).channel(OioBluetoothChannel.class).remoteAddress(new BluetoothDeviceAddress("btspp://000878439229:1;authenticate=false;encrypt=false;master=false"))
                 .handler(new ChannelInitializer<OioBluetoothChannel>() {

                     @Override
                     public void initChannel(OioBluetoothChannel ch) {
                         ch.pipeline().addLast(new StringDecoder()).addLast(new StringEncoder());
                     }
                 });

        bootstrap.connect().syncUninterruptibly();
    }
}