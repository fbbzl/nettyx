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

@Slf4j
@SuppressWarnings("unchecked")
public abstract class Server {

    private final EventLoopGroup
        parentEventLoopGroup = new NioEventLoopGroup(),
        childEventLoopGroup  = new NioEventLoopGroup();

    private final ServerBootstrap serverBootstrap =
        new ServerBootstrap()
            .group(parentEventLoopGroup, childEventLoopGroup)
            .channel(NioServerSocketChannel.class);

    public EventLoopGroup parentEventLoopGroup() {
        return this.parentEventLoopGroup;
    }

    public EventLoopGroup childEventLoopGroup() {
        return this.childEventLoopGroup;
    }

    protected ServerBootstrap newServerBootstrap() {
        return serverBootstrap.clone();
    }

    public abstract ChannelFuture bind(SocketAddress socketAddress) throws Exception;

    protected void shutdownGracefully() {
        childEventLoopGroup.shutdownGracefully();
        parentEventLoopGroup.shutdownGracefully();
    }

    protected void syncShutdownGracefully() throws InterruptedException {
        childEventLoopGroup.shutdownGracefully().sync();
        parentEventLoopGroup.shutdownGracefully().sync();
    }

    public <T> ScheduledFuture<T> schedule(Runnable command, long delay, TimeUnit unit) {
        return (ScheduledFuture<T>) parentEventLoopGroup().schedule(command, delay, unit);
    }

    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return parentEventLoopGroup().schedule(callable, delay, unit);
    }

    public <T> ScheduledFuture<T> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return (ScheduledFuture<T>) parentEventLoopGroup().scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    public <T> ScheduledFuture<T> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return (ScheduledFuture<T>) parentEventLoopGroup().scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

}
