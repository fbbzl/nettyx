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

    protected static final ThreadLocal<ConnectionState> connectState = ThreadLocal.withInitial(ConnectionState::new);
    private final          SocketAddress                remoteAddress;
    private final          Bootstrap                    bootstrap;
    private                Channel                      channel;

    protected AbstractSingleChannelTemplate(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        this.bootstrap     = newBootstrap(remoteAddress);
    }

    public ChannelFuture connect() {
        ChannelFuture channelFuture = this.getBootstrap().clone().connect();
        channelFuture.addListeners(
                new ActionChannelFutureListener().whenSuccess((l, cf) -> this.storeChannel(cf)),
                (ChannelFutureListener) (cf -> ConnectionState.doIncrease(connectState.get(), cf))
                                  );

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

    public ConnectionState getConnectState() {
        return connectState.get();
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
    public static class ConnectionState {

        /**
         * total number of connection times
         */
        private long connectTimes;
        /**
         * the number of successful connection times
         */
        private long connectSuccessTimes;
        /**
         * the number of connection failure times
         */
        private long connectFailureTimes;
        /**
         * the number of times the connection was done
         */
        private long connectDoneTimes;
        /**
         * the number of times the connection was canceled
         */
        private long connectCancelTimes;

       public static void doIncrease(ConnectionState state, ChannelFuture cf) {
            state.setConnectTimes(state.getConnectTimes() + 1);

            if (cf.isSuccess())   state.setConnectSuccessTimes(state.getConnectSuccessTimes() + 1);
            if(!cf.isSuccess())   state.setConnectFailureTimes(state.getConnectFailureTimes() + 1);
            if (cf.isDone())      state.setConnectDoneTimes(state.getConnectDoneTimes() + 1);
            if (cf.isCancelled()) state.setConnectCancelTimes(state.getConnectCancelTimes() + 1);
        }

        public static void reset(ConnectionState state) {
            state.setConnectTimes(0);
            state.setConnectSuccessTimes(0);
            state.setConnectFailureTimes(0);
            state.setConnectDoneTimes(0);
            state.setConnectCancelTimes(0);
        }

    }
}
