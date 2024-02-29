package org.fz.nettyx.endpoint;

import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The generic NioTcpClient.
 * you may use like the following code:
 *
 *     public void connect(SocketAddress address) {
 *         log.info("connecting to [{}]...", address);
 *
 *         ChannelFutureListener connectListener = new ActionableChannelFutureListener()
 *             .whenSuccess(connectSuccessAction(address))
 *             .whenFailure(connectFailureAction(address));
 *
 *         super.cloneBootstrap()
 *             .handler(channelInitializer())
 *             .connect(address)
 *             .addListeners(connectListener);
 *     }
 *
 *     private ChannelInitializer<NioSocketChannel> channelInitializer() {
 *         InboundAdvice inboundAdvice = new InboundAdvice()
 *             .whenChannelActive(channelActiveAction())
 *             .whenChannelInactive(channelInactiveAction())
 *             .whenChannelRead(channelReadAction())
 *             .whenReadIdle(net.readIdleSeconds(), readIdleAction());
 *
 *         return new AdvisableChannelInitializer<NioSocketChannel>(inboundAdvice) {
 *             @Override
 *             protected void addHandlers(NioSocketChannel channel) {
 *                 channel.pipeline()
 *                     // in  out
 *                     // ▼   ▲  remove start and end flag
 *                     .addLast(new StartEndFlagFrameCodec(BytesKit.le.fromByteValue((byte) 0x7e)))
 *                     // ▼   ▲  deal control character and recover application data
 *                     .addLast(new BitResetRecoverCodec())
 *                     // ▼   ▲  do check sum
 *                     .addLast(new CheckSumCodec())
 *                     // ▼   ▲  object serialization
 *                     .addLast(new DatexRecordCodec())
 *                     // ▼   ●  do dispatch
 *                     .addLast(messageDispatcher);
 *             }
 *         };
 *     }
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 16:49
 */
@Slf4j
@SuppressWarnings("unchecked")
public abstract class NettyClient {

    public abstract EventLoopGroup getEventLoopGroup();

    protected boolean isRegistered(Channel channel) { return channel != null && channel.isRegistered(); }
    protected boolean isOpen(Channel channel)       { return channel != null && channel.isOpen();       }
    protected boolean isActive(Channel channel)     { return channel != null && channel.isActive();     }
    protected boolean isWritable(Channel channel)   { return channel != null && channel.isWritable();   }

    protected boolean notRegistered(Channel channel) { return !isRegistered(channel); }
    protected boolean notOpen(Channel channel)       { return !isOpen(channel);       }
    protected boolean notActive(Channel channel)     { return !isActive(channel);     }
    protected boolean notWritable(Channel channel)   { return !isWritable(channel);   }

    protected ChannelPromise failurePromise(Channel channel) {
        return failurePromise(channel, "channel failure promise occur, channel: [" + channel + "]");
    }

    protected ChannelPromise failurePromise(Channel channel, String message) {
        return channel == null ? null : new DefaultChannelPromise(channel).setFailure(new ChannelException(message));
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

    protected abstract <C extends Channel> ChannelInitializer<C> getChannelInitializer();

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
