package org.fz.nettyx.endpoint.tcp.client;


import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.NettyClient;

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
public abstract class SingleTcpChannelClient extends NettyClient {

    private final EventLoopGroup eventLoopGroup;

    protected SingleTcpChannelClient() {
        this.eventLoopGroup = new NioEventLoopGroup();
    }

    /**
     * The Channel.
     */
    protected Channel channel;

    /**
     * Store channel.
     *
     * @param cf the cf
     */
    protected void storeChannel(ChannelFuture cf) {
        storeChannel(cf.channel());
    }

    /**
     * Store channel.
     *
     * @param channel the channel
     */
    @SneakyThrows
    protected void storeChannel(Channel channel) {
        if (isActive(this.channel)) {
            this.channel.close().sync();
        }
        this.channel = channel;
    }

    /**
     * Close channel.
     */
    public void closeChannel() {
        this.channel.close();
    }

    /**
     * Close channel.
     *
     * @param promise the promise
     */
    public void closeChannel(ChannelPromise promise) {
        this.channel.close(promise);
    }

    /**
     * Close channel gracefully.
     */
    public void closeChannelGracefully() {
        if (gracefullyCloseable(channel)) this.closeChannel();
    }

    /**
     * Close channel gracefully.
     *
     * @param promise the promise
     */
    public void closeChannelGracefully(ChannelPromise promise) {
        if (gracefullyCloseable(channel)) this.closeChannel(promise);
    }

    public abstract void connect(SocketAddress remoteAddress);

    /**
     * Send channel promise.
     *
     * @param message the message
     *
     * @return the channel promise
     */
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
}
