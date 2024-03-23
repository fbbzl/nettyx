package org.fz.nettyx.endpoint.client.jsc;


import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.AbstractSingleChannelClient;
import org.fz.nettyx.endpoint.client.jsc.support.JscChannel;
import org.fz.nettyx.endpoint.client.jsc.support.JscDeviceAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/29 10:09
 */

@Slf4j
@SuppressWarnings("deprecation")
public abstract class SingleJscChannelClient extends AbstractSingleChannelClient<JscChannel> {

    public ChannelFuture connect(String commAddress) {
        return super.connect(new JscDeviceAddress(commAddress));
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
