package org.fz.nettyx.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.net.SocketAddress;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/5/13 9:10
 */
@Slf4j
@SuppressWarnings("unchecked")
public abstract class Server {

    private final EventLoopGroup
        parentEventLoopGroup,
        childEventLoopGroup;

    private final ServerBootstrap serverBootstrap;

    protected Server() {
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

    public EventLoopGroup getParentEventLoopGroup() {
        return this.parentEventLoopGroup;
    }

    public EventLoopGroup getChildEventLoopGroup() {
        return this.childEventLoopGroup;
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
