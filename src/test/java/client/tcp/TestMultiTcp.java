package client.tcp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.endpoint.client.tcp.MultiTcpChannelClient;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import static client.tcp.TestSingleTcp.CHANNEL_INITIALIZER;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 14:58
 */
public class TestMultiTcp extends MultiTcpChannelClient<String> {

    protected TestMultiTcp(Map<String, InetSocketAddress> inetSocketAddressMap) {
        super(inetSocketAddressMap);
    }

    @Override
    protected ChannelInitializer<NioSocketChannel> channelInitializer() {
        return CHANNEL_INITIALIZER;
    }

    public static void main(String[] args) {
        Map<String, InetSocketAddress> map = new HashMap<>();

        map.put("a", new InetSocketAddress("127.0.0.1", 9081));
        map.put("b", new InetSocketAddress("127.0.0.1", 9082));

        TestMultiTcp testMultiTcp = new TestMultiTcp(map);
        testMultiTcp.connectAll();
    }
}
