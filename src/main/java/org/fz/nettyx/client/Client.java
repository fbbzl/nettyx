package org.fz.nettyx.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * The generic NioTcpClient.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 16:49
 */
@Slf4j
@SuppressWarnings("unchecked")
public abstract class Client {

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

    /**
     * Event loop group event loop group.
     *
     * @return the event loop group
     */
    public EventLoopGroup eventLoopGroup() {
        return this.eventLoopGroup;
    }

    protected Bootstrap newBootstrap() {
        return bootstrap.clone();
    }

    /**
     * Available boolean.
     *
     * @param channel the channel
     * @return the boolean
     */
    public boolean ready(Channel channel) {
        return channel != null && channel.isActive();
    }

    /**
     * Unavailable boolean
     *
     * @param channel the channel
     * @return the boolean
     */
    public boolean notReady(Channel channel) {
        return !ready(channel);
    }

    /**
     * Shutdown gracefully.
     */
    protected void shutdownGracefully() {
        eventLoopGroup.shutdownGracefully();
    }

    public static boolean preCloseGracefully(Channel channel) {
        return
            channel != null
                &&
                !channel.isActive()
                &&
                !channel.isOpen()
                &&
                !channel.isWritable();
    }

    /**
     * Schedule scheduled future.
     *
     * @param command the command
     * @param delay the delay
     * @param unit the unit
     * @return the scheduled future
     */
    public <T> ScheduledFuture<T> schedule(Runnable command, long delay, TimeUnit unit) {
        return (ScheduledFuture<T>) eventLoopGroup().schedule(command, delay, unit);
    }

    /**
     * Schedule scheduled future.
     *
     * @param <V> the type parameter
     * @param callable the callable
     * @param delay the delay
     * @param unit the unit
     * @return the scheduled future
     */
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return eventLoopGroup().schedule(callable, delay, unit);
    }

    /**
     * Schedule at fixed rate scheduled future.
     *
     * @param command the command
     * @param initialDelay the initial delay
     * @param period the period
     * @param unit the unit
     * @return the scheduled future
     */
    public <T> ScheduledFuture<T> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return (ScheduledFuture<T>) eventLoopGroup().scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    /**
     * Schedule with fixed delay scheduled future.
     *
     * @param command the command
     * @param initialDelay the initial delay
     * @param delay the delay
     * @param unit the unit
     * @return the scheduled future
     */
    public <T> ScheduledFuture<T> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return (ScheduledFuture<T>) eventLoopGroup().scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
}
