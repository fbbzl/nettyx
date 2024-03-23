package org.fz.nettyx.endpoint.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.SocketAddress;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * basic server abstraction
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/5/13 9:10
 */

@Slf4j
@Getter
@SuppressWarnings("unchecked")
public abstract class TcpServer {

    private final EventLoopGroup
        parentEventLoopGroup,
        childEventLoopGroup;

    private final ServerBootstrap serverBootstrap;

    protected TcpServer() {
        this.childEventLoopGroup  = childEventLoopGroup();
        this.parentEventLoopGroup = parentEventLoopGroup();

        this.serverBootstrap =
            new ServerBootstrap()
                .group(parentEventLoopGroup, childEventLoopGroup)
                .channel(NioServerSocketChannel.class);
    }

    protected EventLoopGroup parentEventLoopGroup() {
        return new NioEventLoopGroup();
    }

    protected EventLoopGroup childEventLoopGroup() {
        return new NioEventLoopGroup();
    }

    protected ServerBootstrap newServerBootstrap() {
        return serverBootstrap.clone();
    }

    public abstract ChannelFuture bind(SocketAddress socketAddress);

    protected void shutdownGracefully() {
        childEventLoopGroup.shutdownGracefully();
        parentEventLoopGroup.shutdownGracefully();
    }

    protected void syncShutdownGracefully() throws InterruptedException {
        childEventLoopGroup.shutdownGracefully().sync();
        parentEventLoopGroup.shutdownGracefully().sync();
    }

}
