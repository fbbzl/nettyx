package org.fz.nettyx.endpoint.client.jsc;


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

    public SingleJscChannelClient(String commAddress) {
        super(new JscDeviceAddress(commAddress));
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new OioEventLoopGroup();
    }

}
