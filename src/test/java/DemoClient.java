import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.SocketAddress;
import org.fz.nettyx.client.SingleChannelClient;

/**
 * @author fengbinbin
 * @since 2022-02-08 20:32
 **/
public class DemoClient extends SingleChannelClient {

    @Override
    public void connect(SocketAddress address) {
        super.newBootstrap().handler(channelInitializer()).connect(address);
    }

    @Override
    public ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {

            }
        };
    }

    public static void main(String[] args) {

    }
}
