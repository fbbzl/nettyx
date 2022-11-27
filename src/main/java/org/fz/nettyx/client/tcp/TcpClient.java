package org.fz.nettyx.client.tcp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.client.Client;

/**
 * @author fengbinbin
 * @since 2022-01-26 20:54
 **/
public abstract class TcpClient extends Client {

    protected final EventLoopGroup eventLoopGroup;

    private final Bootstrap bootstrap;

    protected TcpClient() {
        this.eventLoopGroup = eventLoopGroup();

        this.bootstrap = new Bootstrap().group(eventLoopGroup).channel(NioSocketChannel.class);
    }

    public EventLoopGroup eventLoopGroup() {
        return new NioEventLoopGroup();
    }

    @Override
    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    @Override
    public Bootstrap getBootstrap() {
        return bootstrap;
    }
}
