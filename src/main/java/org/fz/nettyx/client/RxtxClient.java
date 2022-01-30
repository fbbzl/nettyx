package org.fz.nettyx.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxChannelConfig;

/**
 * @author fengbinbin
 * @since 2022-01-26 19:58
 **/
abstract class RxtxClient extends Client {

    /**
     * The Oio Event loop group.
     */
    protected final EventLoopGroup eventLoopGroup = new OioEventLoopGroup();

    private final Bootstrap bootstrap = new Bootstrap()
        .group(eventLoopGroup).channelFactory(() -> {
            RxtxChannel rxtxChannel = new RxtxChannel();
            this.rxtxConfig(rxtxChannel.config());
            return rxtxChannel;
        }).handler(channelInitializer());

    protected abstract void rxtxConfig(RxtxChannelConfig rxtxChannel);

    public abstract ChannelInitializer<RxtxChannel> channelInitializer();

    @Override
    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    @Override
    public Bootstrap getBootstrap() {
        return bootstrap;
    }
}
