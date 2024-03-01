package client.rxtx;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import org.fz.nettyx.endpoint.client.rxtx.SingleRxtxChannelClient;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:31
 */
public class TestRxtx extends SingleRxtxChannelClient {

    protected TestRxtx(RxtxDeviceAddress remoteAddress) {
        super(remoteAddress);
    }

    @Override
    protected ChannelInitializer<? extends Channel> channelInitializer() {
        return null;
    }

    public static void main(String[] args) {


    }
}
