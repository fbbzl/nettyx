package client.tcp;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import org.fz.nettyx.endpoint.client.tcp.MultiTcpChannelClient;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 14:58
 */
public class TestMultiTcp extends MultiTcpChannelClient<String> {

    protected TestMultiTcp(Map<String, InetSocketAddress> stringInetSocketAddressMap) {
        super(stringInetSocketAddressMap);
    }

    @Override
    protected ChannelInitializer<? extends Channel> channelInitializer() {
        return null;
    }

    public static void main(String[] args) {
        Map<Object, Object> map = new HashMap<>();

        map.put("a", new InetSocketAddress("127.0.0.1", 9081));
        map.put("b", new InetSocketAddress("127.0.0.1", 9082));

        
    }
}
