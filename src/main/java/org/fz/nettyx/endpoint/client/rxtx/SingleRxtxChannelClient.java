package org.fz.nettyx.endpoint.client.rxtx;

import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.AbstractSingleChannelClient;
import org.fz.nettyx.endpoint.client.rxtx.support.XRxtxChannel;
import org.fz.nettyx.endpoint.client.rxtx.support.XRxtxDeviceAddress;

/**
 * single channel rxtx client
 *
 * @author fengbinbin
 * @since 2022-01-26 20:25
 **/

@Slf4j
@SuppressWarnings("deprecation")
public abstract class SingleRxtxChannelClient extends AbstractSingleChannelClient<XRxtxChannel> {

    public ChannelFuture connect(String commAddress) {
        return super.connect(new XRxtxDeviceAddress(commAddress));
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
