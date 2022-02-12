package org.fz.nettyx.client.rxtx;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxChannelConfig;
import org.fz.nettyx.client.Client;

/**
 * @author fengbinbin
 * @since 2022-01-26 19:58
 **/
public abstract class RxtxClient extends Client {

    /**
     * The Oio Event loop group.
     */
    protected final EventLoopGroup eventLoopGroup = new OioEventLoopGroup();

    private final Bootstrap bootstrap = new Bootstrap()
        .group(eventLoopGroup).channelFactory(() -> {
            RxtxChannel rxtxChannel = new RxtxChannel();
            this.rxtxConfig(rxtxChannel.config());
            return rxtxChannel;
        });

    protected abstract void rxtxConfig(RxtxChannelConfig rxtxChannel);

    @Override
    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    @Override
    public Bootstrap getBootstrap() {
        return bootstrap;
    }

}
