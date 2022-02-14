package org.fz.nettyx.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
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

    public abstract EventLoopGroup getEventLoopGroup();

    public abstract Bootstrap getBootstrap();

    protected Bootstrap newBootstrap() {
        return getBootstrap().clone();
    }

    public boolean active(Channel channel) {
        return channel != null && channel.isActive();
    }

    public boolean inActive(Channel channel) {
        return !active(channel);
    }

    protected void shutdownGracefully() {
        getEventLoopGroup().shutdownGracefully();
    }

    public static boolean gracefullyCloseable(Channel channel) {
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
        return (ScheduledFuture<T>) getEventLoopGroup().schedule(command, delay, unit);
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
        return getEventLoopGroup().schedule(callable, delay, unit);
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
        return (ScheduledFuture<T>) getEventLoopGroup().scheduleAtFixedRate(command, initialDelay, period, unit);
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
        return (ScheduledFuture<T>) getEventLoopGroup().scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
}
