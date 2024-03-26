package client.rxtx;

import client.TestChannelInitializer;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.endpoint.client.rxtx.MultiRxtxChannelClient;
import org.fz.nettyx.endpoint.client.rxtx.support.XRxtxChannel;
import org.fz.nettyx.endpoint.client.rxtx.support.XRxtxDeviceAddress;

import java.util.HashMap;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 22:58
 */
public class TestMultiRxtx extends MultiRxtxChannelClient<String> {

    protected TestMultiRxtx(Map<String, XRxtxDeviceAddress> stringRxtxDeviceAddressMap) {
        super(stringRxtxDeviceAddressMap);
    }

    public static void main(String[] args) {
        Map<String, XRxtxDeviceAddress> map = new HashMap<>();

        map.put("5", new XRxtxDeviceAddress("COM5"));
        map.put("6", new XRxtxDeviceAddress("COM6"));

        TestMultiRxtx testMultiTcp = new TestMultiRxtx(map);
        testMultiTcp.connectAll();
    }

    @Override
    protected ChannelInitializer<XRxtxChannel> channelInitializer() {
        return new TestChannelInitializer<>();
    }
}
