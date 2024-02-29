package org.fz.nettyx.endpoint.serial.rxtx;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFactory;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxChannelConfig;
import org.fz.nettyx.endpoint.Client;

import java.util.concurrent.*;

/**
 * rxtx abstract client
 *
 * @author fengbinbin
 * @since 2022-01-26 19:58
 */
@SuppressWarnings({"deprecation", "unchecked"})
public abstract class RxtxClient extends Client {

    /**
     * The Oio Event loop group.
     */
    protected final EventLoopGroup eventLoopGroup;

    private final Bootstrap bootstrap;

    private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(scheduledCorePoolSize());

    protected RxtxClient() {
        this.eventLoopGroup = new OioEventLoopGroup();

        this.bootstrap =
                new Bootstrap()
                        .group(eventLoopGroup)
                        .channelFactory(rxtxChannelFactory());
    }

    public ChannelFactory<RxtxChannel> rxtxChannelFactory() {
        return () -> {
            RxtxChannel rxtxChannel = new RxtxChannel();
            this.doRxtxConfig(rxtxChannel.config());
            return rxtxChannel;
        };
    }

    protected abstract void doRxtxConfig(RxtxChannelConfig rxtxChannel);

    @Override
    public EventLoopGroup getEventLoopGroup() {
        return eventLoopGroup;
    }

    @Override
    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    protected int scheduledCorePoolSize() {
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Schedule scheduled future.
     *
     * @param command the command
     * @param delay   the delay
     * @param unit    the unit
     *
     * @return the scheduled future
     */
    @Override
    public <T> ScheduledFuture<T> schedule(Runnable command, long delay, TimeUnit unit) {
        return (ScheduledFuture<T>) executorService.schedule(command, delay, unit);
    }

    /**
     * Schedule scheduled future.
     *
     * @param <V>      the type parameter
     * @param callable the callable
     * @param delay    the delay
     * @param unit     the unit
     *
     * @return the scheduled future
     */
    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return executorService.schedule(callable, delay, unit);
    }

    /**
     * Schedule at fixed rate scheduled future.
     *
     * @param command      the command
     * @param initialDelay the initial delay
     * @param period       the period
     * @param unit         the unit
     *
     * @return the scheduled future
     */
    @Override
    public <T> ScheduledFuture<T> scheduleAtFixedRate(
            Runnable command, long initialDelay, long period, TimeUnit unit) {
        return (ScheduledFuture<T>)
                executorService.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    /**
     * Schedule with fixed delay scheduled future.
     *
     * @param command      the command
     * @param initialDelay the initial delay
     * @param delay        the delay
     * @param unit         the unit
     *
     * @return the scheduled future
     */
    @Override
    public <T> ScheduledFuture<T> scheduleWithFixedDelay(
            Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return (ScheduledFuture<T>)
                executorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }
}
