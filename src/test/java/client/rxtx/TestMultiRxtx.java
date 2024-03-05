package client.rxtx;

import client.TestChannelInitializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.action.ChannelFutureAction;
import org.fz.nettyx.endpoint.client.rxtx.MultiRxtxChannelClient;
import org.fz.nettyx.endpoint.client.rxtx.support.XRxtxDeviceAddress;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 22:58
 */
public class TestMultiRxtx extends MultiRxtxChannelClient<String> {

    protected TestMultiRxtx(Map<String, XRxtxDeviceAddress> stringRxtxDeviceAddressMap) {
        super(stringRxtxDeviceAddressMap);
    }

    @Override
    protected ChannelInitializer<? extends Channel> channelInitializer() {
        return new TestChannelInitializer();
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
        Map<String, XRxtxDeviceAddress> map = new HashMap<>();

        map.put("5", new XRxtxDeviceAddress("COM5"));
        map.put("6", new XRxtxDeviceAddress("COM6"));

        TestMultiRxtx testMultiTcp = new TestMultiRxtx(map);
        testMultiTcp.connectAll();
    }
}
