package org.fz.nettyx.endpoint.client.rxtx;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.SingleChannelClient;

/**
 * single channel rxtx client
 *
 * @author fengbinbin
 * @since 2022-01-26 20:25
 **/

@Slf4j
@SuppressWarnings("deprecation")
public abstract class SingleRxtxChannelClient extends
                                              SingleChannelClient<RxtxChannel> {

    protected SingleRxtxChannelClient(RxtxDeviceAddress remoteAddress) {
        super(remoteAddress);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
