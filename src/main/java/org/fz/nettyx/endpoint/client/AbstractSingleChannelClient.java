package org.fz.nettyx.endpoint.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.util.ReferenceCountUtil;
import java.net.SocketAddress;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.listener.ActionChannelFutureListener;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */

@Slf4j
@Getter
public abstract class AbstractSingleChannelClient<C extends Channel> extends Client<C> {

    private final SocketAddress remoteAddress;
    private final Bootstrap     bootstrap;
    private       Channel       channel;

    protected AbstractSingleChannelClient(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
        this.bootstrap     = newBootstrap(remoteAddress);
    }

    public ChannelFuture connect() {
        ChannelFuture channelFuture = this.getBootstrap().clone().connect();
        channelFuture.addListener(new ActionChannelFutureListener().whenSuccess(this::storeChannel));
        return channelFuture;
    }

    protected void storeChannel(ChannelFuture cf) {
        storeChannel(cf.channel());
    }

    @SneakyThrows({InterruptedException.class})
    protected void storeChannel(Channel channel) {
        if (isActive(this.channel)) { this.channel.close().sync(); }
        this.channel = channel;
    }

    public void closeChannelGracefully() {
        if (gracefullyCloseable(channel)) { this.getChannel().close(); }
    }

    public void closeChannelGracefully(ChannelPromise promise) {
        if (gracefullyCloseable(channel)) { this.getChannel().close(promise); }
    }

    public ChannelPromise writeAndFlush(Object message) {
        if (this.notActive(channel) || notWritable(channel)) {
            log.debug("channel not in usable status, message will be discard: {}", message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "channel: [" + channel + "] is not usable");
        }

        try {
            return (ChannelPromise) channel.writeAndFlush(message);
        }
        catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], address is ["
                                       + channel.remoteAddress() + "]", exception);
        }
    }

    protected Bootstrap newBootstrap(SocketAddress remoteAddress) {
        return new Bootstrap()
            .remoteAddress(remoteAddress)
            .group(getEventLoopGroup())
            .channelFactory(() -> {
                C chl = new ReflectiveChannelFactory<>(getChannelClass()).newChannel();
                doChannelConfig(chl);
                return chl;
            })
            .handler(channelInitializer());
    }

    protected void doChannelConfig(C channel) {
        // default is nothing
    }

}
