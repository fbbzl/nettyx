package org.fz.nettyx.endpoint.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * basic server abstraction
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
        this.childEventLoopGroup = childEventLoopGroup();
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

    public <T> ScheduledFuture<T> schedule(Runnable command, long delay, TimeUnit unit) {
        return (ScheduledFuture<T>) getParentEventLoopGroup().schedule(command, delay, unit);
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return getParentEventLoopGroup().schedule(callable, delay, unit);
    }

    public <T> ScheduledFuture<T> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return (ScheduledFuture<T>) getParentEventLoopGroup().scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public <T> ScheduledFuture<T> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return (ScheduledFuture<T>) getParentEventLoopGroup().scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

}
