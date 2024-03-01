package client.tcp;



import client.TestChannelInitializer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.fz.nettyx.action.ChannelFutureAction;
import org.fz.nettyx.endpoint.client.tcp.MultiTcpChannelClient;

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
        return new TestChannelInitializer<>();
    }

    @Override
    protected ChannelFutureAction whenConnectSuccess(String key) {
        return cf -> {
            System.err.println(key +": ok");
        };
    }

    @Override
    protected void doChannelConfig(String key, SocketChannelConfig channelConfig) {
        super.doChannelConfig(key, channelConfig);
    }

    @Override
    protected ChannelFutureAction whenConnectFailure(String key) {
        return cf -> {
            System.err.println(key +": fail, " + cf.cause());
            cf.channel().eventLoop().schedule(() -> connect(key), 2, TimeUnit.SECONDS);
        };
    }

    public static void main(String[] args) {
        Map<String, InetSocketAddress> map = new HashMap<>();

        map.put("a", new InetSocketAddress("127.0.0.1", 9081));
        map.put("b", new InetSocketAddress("127.0.0.1", 9082));

        TestMultiTcp testMultiTcp = new TestMultiTcp(map);
        testMultiTcp.connectAll();
    }
}
