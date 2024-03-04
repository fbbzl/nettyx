package org.fz.nettyx.endpoint.client.rxtx;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.AbstractSingleChannelClient;
import org.fz.nettyx.endpoint.client.rxtx.support.NettyxRxtxChannel;
import org.fz.nettyx.endpoint.client.rxtx.support.NettyxRxtxDeviceAddress;

/**
 * single channel rxtx client
 *
 * @author fengbinbin
 * @since 2022-01-26 20:25
 **/

@Slf4j
@SuppressWarnings("deprecation")
public abstract class SingleRxtxChannelClient extends
                                              AbstractSingleChannelClient<NettyxRxtxChannel> {

    protected SingleRxtxChannelClient(NettyxRxtxDeviceAddress remoteAddress) {
        super(remoteAddress);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
