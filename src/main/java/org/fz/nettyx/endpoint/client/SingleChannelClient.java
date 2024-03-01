package org.fz.nettyx.endpoint.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.listener.ActionableChannelFutureListener;

import java.net.SocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */

@Slf4j
@Getter
public abstract class SingleChannelClient<C extends Channel, A extends SocketAddress> extends NettyClient<C> {
    private final A         remoteAddress;
    private       Channel   channel;
    private final Bootstrap bootstrap;

    protected SingleChannelClient(EventLoopGroup eventLoopGroup, A remoteAddress) {
        super(eventLoopGroup);
        this.remoteAddress = remoteAddress;
        this.bootstrap     = newBootstrap(remoteAddress);
    }

    public void connect() {
        ChannelFutureListener listener = new ActionableChannelFutureListener()
                .whenDone(whenConnectDone())
                .whenCancel(whenConnectCancel())
                .whenSuccess(whenConnectSuccess())
                .whenFailure(whenConnectFailure());

        this.getBootstrap()
            .clone()
            .connect()
            .addListener(listener);
    }

    protected void storeChannel(ChannelFuture cf) {
        storeChannel(cf.channel());
    }

    @SneakyThrows
    protected void storeChannel(Channel channel) {
        if (isActive(this.channel)) {
            this.channel.close().sync();
        }
        this.channel = channel;
    }

    public void closeChannelGracefully() {
        if (gracefullyCloseable(channel)) { this.getChannel().close(); }
    }

    public void closeChannelGracefully(ChannelPromise promise) {
        if (gracefullyCloseable(channel)) { this.getChannel().close(promise); }
    }

    public ChannelPromise writeAndFlush(Object message) {
        if (this.notActive(channel)) {
            log.debug("comm channel not in active status, message will be discard: {}", message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "comm channel: [" + channel + "] is not usable");
        }

        try {
            if (notWritable(channel)) {
                log.debug("comm channel [{}] is not writable", channel);
                ReferenceCountUtil.safeRelease(message);
                return failurePromise(channel, "comm channel: [" + channel + "] is not writable");
            } else { return (ChannelPromise) channel.writeAndFlush(message); }
        }
        catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], comm-port is ["
                                       + channel.remoteAddress() + "]", exception);
        }
    }

    protected Bootstrap newBootstrap(SocketAddress remoteAddress) {
        return new Bootstrap()
                .remoteAddress(remoteAddress)
                .group(getEventLoopGroup())
                .channel(getChannelClass())
                .handler(channelInitializer());
    }

    protected abstract ChannelInitializer<? extends Channel> channelInitializer();

}
