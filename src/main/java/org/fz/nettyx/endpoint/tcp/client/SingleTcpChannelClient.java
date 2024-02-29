package org.fz.nettyx.endpoint.tcp.client;


import cn.hutool.core.lang.Console;
import cn.hutool.core.util.TypeUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelFutureAction;
import org.fz.nettyx.endpoint.NettyClient;
import org.fz.nettyx.listener.ActionableChannelFutureListener;

import java.net.SocketAddress;

/**
 * Single channel client
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 15:22
 */
@Slf4j
@Getter
@SuppressWarnings("unchecked")
public abstract class SingleTcpChannelClient<C extends Channel> extends NettyClient {

    private final SocketAddress remoteAddress;
    private final Bootstrap bootstrap;

    private final EventLoopGroup eventLoopGroup;

    protected SingleTcpChannelClient(SocketAddress remoteAddress) {
        this.eventLoopGroup = new NioEventLoopGroup();
        this.remoteAddress = remoteAddress;
        this.bootstrap = new Bootstrap()
                .group(getEventLoopGroup())
                .channel((Class<? extends Channel>) TypeUtil.getTypeArgument(this.getClass(), 0))
                .handler(channelInitializer());
    }

    protected Channel channel;

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

    public void closeChannel() {
        this.channel.close();
    }


    public void closeChannel(ChannelPromise promise) {
        this.channel.close(promise);
    }


    public void closeChannelGracefully() {
        if (gracefullyCloseable(channel)) this.closeChannel();
    }

    public void closeChannelGracefully(ChannelPromise promise) {
        if (gracefullyCloseable(channel)) this.closeChannel(promise);
    }

    public void connect() {
        final SocketAddress address = getRemoteAddress();
        Console.log("connecting address [" + address.toString() + "]");
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

    protected abstract ChannelInitializer<C> channelInitializer();

    public ChannelPromise writeAndFlush(Object message) {
        if (this.notActive(channel)) {
            log.debug("channel not in active status, message will be discard: {}", message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "channel: [" + channel + "] is not usable");
        }

        try {
            if (notWritable(channel)) {
                log.debug("channel [{}] is not writable", channel);
                ReferenceCountUtil.safeRelease(message);
                return failurePromise(channel, "channel: [" + channel + "] is not writable");
            } else return (ChannelPromise) channel.writeAndFlush(message);
        }
        catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], remote " +
                                       "address is [" + channel.remoteAddress() + "]", exception);
        }
    }

    //***************************************** event method start ***************************************************//

    protected ChannelFutureAction whenConnectDone() {
        return ctx -> {
        };
    }

    protected ChannelFutureAction whenConnectCancel() {
        return ctx -> {
        };
    }

    protected ChannelFutureAction whenConnectSuccess() {
        return ctx -> {
        };
    }

    protected ChannelFutureAction whenConnectFailure() {
        return ctx -> {
        };
    }


}
