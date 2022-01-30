package org.fz.nettyx.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * @author fengbinbin
 * @since 2022-01-26 20:54
 **/
public abstract class TcpClient extends Client {

    /**
     * The Event loop group.
     */
    protected final EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

    /**
     * The Proto bootstrap.
     */
    private final Bootstrap bootstrap = new Bootstrap().group(eventLoopGroup).channel(NioSocketChannel.class).handler(channelInitializer());

    /**
     * Channel initializer channel initializer.
     *
     * @return the channel initializer
     */
    public abstract ChannelInitializer<NioSocketChannel> channelInitializer();

    @Override
    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    @Override
    public Bootstrap getBootstrap() {
        return bootstrap;
    }
}
