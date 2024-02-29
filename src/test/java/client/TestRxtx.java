package client;

import io.netty.channel.ChannelFuture;
import io.netty.channel.rxtx.RxtxChannelConfig;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import org.fz.nettyx.endpoint.serial.rxtx.SingleRxtxChannelClient;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:31
 */
public class TestRxtx {


    public static void main(String[] args) {
        SingleRxtxChannelClient singleRxtxChannelClient = new SingleRxtxChannelClient() {
            @Override
            public ChannelFuture connect(RxtxDeviceAddress address) throws Exception {
                return null;
            }

            @Override
            protected void doRxtxConfig(RxtxChannelConfig rxtxChannel) {

            }
        };

    }
}
