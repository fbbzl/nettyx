package org.fz.nettyx.endpoint.tcp.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.endpoint.Client;

/**
 * basic tcp client
 * @author fengbinbin
 * @since 2022-01-26 20:54
 **/
public abstract class TcpClient extends Client {

    protected final EventLoopGroup eventLoopGroup;

    private final Bootstrap bootstrap;

    protected TcpClient() {
        this.eventLoopGroup = new NioEventLoopGroup();

        this.bootstrap = new Bootstrap().group(eventLoopGroup).channel(NioSocketChannel.class);
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
