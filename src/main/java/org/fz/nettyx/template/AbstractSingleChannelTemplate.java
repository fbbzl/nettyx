package org.fz.nettyx.template;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.Data;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.listener.ActionChannelFutureListener;

import java.net.SocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */

@Slf4j
@Getter
@SuppressWarnings({ "unchecked", "unused" })
public abstract class AbstractSingleChannelTemplate<C extends Channel, F extends ChannelConfig> extends Template<C> {

    protected static final ThreadLocal<ChannelState> channelState = ThreadLocal.withInitial(ChannelState::new);
    private final          SocketAddress             remoteAddress;
    private final          Bootstrap                 bootstrap;
    private                Channel                   channel;

    protected AbstractSingleChannelTemplate(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        this.bootstrap     = newBootstrap(remoteAddress);
    }

    public ChannelFuture connect() {
        ChannelFuture channelFuture = this.getBootstrap().clone().connect();
        channelFuture.addListener(new ActionChannelFutureListener().whenSuccess((l, cf) -> this.storeChannel(cf)));
        // add channel state listener
        addStateListener(channelFuture);
        return channelFuture;
    }

    protected void storeChannel(ChannelFuture cf) {
        storeChannel(cf.channel());
    }

    @SneakyThrows(InterruptedException.class)
    protected void storeChannel(Channel channel) {
        if (isActive(this.channel)) {
            this.channel.close().sync();
        }
        this.channel = channel;
    }

    public void closeChannelGracefully() {
        if (gracefullyCloseable(channel)) {
            this.getChannel().close();
        }
    }

    public void closeChannelGracefully(ChannelPromise promise) {
        if (gracefullyCloseable(channel)) {
            this.getChannel().close(promise);
        }
    }

    public ChannelPromise write(Object message) {
        if (this.notActive(channel) || notWritable(channel)) {
            log.debug("channel not in usable status, message will be discard: {}", message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "channel: [" + channel + "] is not usable");
        }

        try {
            return (ChannelPromise) channel.write(message);
        } catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], address is ["
                                       + channel.remoteAddress() + "]", exception);
        }
    }

    public ChannelPromise writeAndFlush(Object message) {
        if (this.notActive(channel) || notWritable(channel)) {
            log.debug("channel not in usable status, message will be discard: {}", message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "channel: [" + channel + "] is not usable");
        }

        try {
            return (ChannelPromise) channel.writeAndFlush(message);
        } catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], address is ["
                                       + channel.remoteAddress() + "]", exception);
        }
    }

    Bootstrap newBootstrap(SocketAddress remoteAddress) {
        return new Bootstrap()
                .remoteAddress(remoteAddress)
                .group(getEventLoopGroup())
                .channelFactory(() -> {
                    C chl = new ReflectiveChannelFactory<>(getChannelClass()).newChannel();
                    doChannelConfig((F) chl.config());
                    return chl;
                })
                .handler(channelInitializer());
    }

    private void addStateListener(ChannelFuture channelFuture) {
        ActionChannelFutureListener stateListener = new ActionChannelFutureListener();
        stateListener
                .whenSuccess((l, cf) -> channelState.get().)
                .whenFailure((l, cf) -> {})
                .whenDone((l, cf) -> {})
                .whenCancel((l, cf) -> {});

        // TODO close release
        channelFuture.addListener(stateListener);
    }

    protected void doChannelConfig(F channelConfig) {
        // default is do nothing
    }

    /**
     * to save connect state history
     *
     * @author fengbinbin
     * @since 2021 -12-29 18:46
     */
    @Data
    public static class ChannelState {

        /**
         * total number of connections
         */
        private int connectTimes;
        /**
         * the number of successful connections
         */
        private int connectSuccessTimes;
        /**
         * the number of connection failures
         */
        private int connectFailureTimes;
        /**
         * the number of times the connection was completed
         */
        private int connectDoneTimes;
        /**
         * the number of times the connection was canceled
         */
        private int connectCancelTimes;

        static void increase(ChannelFuture channelFuture) {
            ChannelState currentState = channelState.get();

            if (channelFuture.isSuccess())   currentState.setConnectSuccessTimes(currentState.getConnectSuccessTimes() + 1);
            else                             currentState.setConnectFailureTimes(currentState.getConnectFailureTimes() + 1);

            if (channelFuture.isDone())      currentState.setConnectDoneTimes(currentState.getConnectDoneTimes() + 1);
            if (channelFuture.isCancelled()) currentState.setConnectCancelTimes(currentState.getConnectCancelTimes() + 1);
        }

    }
}
