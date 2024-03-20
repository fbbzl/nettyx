package client.jsc;

import client.TestChannelInitializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.action.ChannelFutureAction;
import org.fz.nettyx.endpoint.client.jsc.MultiJscChannelClient;
import org.fz.nettyx.endpoint.client.jsc.support.JscDeviceAddress;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 22:58
 */
public class TestMultiJsc extends MultiJscChannelClient<String> {

    public TestMultiJsc(Map<String, JscDeviceAddress> stringJscDeviceAddressMap) {
        super(stringJscDeviceAddressMap);
    }

    @Override
    protected ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }

    @Override
    protected ChannelFutureAction whenConnectFailure(String key) {
        return cf -> {
            // handle by assigned key value
            if (key.equals("5")) {
                return;
            }
            System.err.println(key + ": fail, " + cf.cause());
            cf.channel().eventLoop().schedule(() -> connect(key), 2, TimeUnit.SECONDS);
        };
    }

    public static void main(String[] args) {
        Map<String, JscDeviceAddress> map = new HashMap<>();

        map.put("5", new JscDeviceAddress("COM5"));
        map.put("6", new JscDeviceAddress("COM6"));

        TestMultiJsc testMultiJsc = new TestMultiJsc(map);
        testMultiJsc.connectAll();
    }


}
