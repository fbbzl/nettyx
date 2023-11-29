package org.fz.nettyx.endpoint.client.tcp;


import io.netty.channel.Channel;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.Client;

import java.net.SocketAddress;

/**
 * Single channel client
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 15:22
 */
@Slf4j
public abstract class SingleTcpChannelClient extends TcpClient {

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
        if (active(this.channel)) {
            this.channel.close().sync();
        }
        this.channel = channel;
    }

    /**
     * Gets channel.
     *
     * @return the channel
     */
    public Channel getChannel() {
        return this.channel;
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
        if (Client.gracefullyCloseable(channel)) this.closeChannel();
    }

    /**
     * Close channel gracefully.
     *
     * @param promise the promise
     */
    public void closeChannelGracefully(ChannelPromise promise) {
        if (Client.gracefullyCloseable(channel)) this.closeChannel(promise);
    }

    /**
     * Connect.
     *
     * @param address the address
     * @return the channel future
     * @throws Exception the exception
     */
    public abstract ChannelFuture connect(SocketAddress address) throws Exception;

    /**
     * Send channel promise.
     *
     * @param message the message
     * @return the channel promise
     */
    public ChannelPromise send(Object message) {
        if (this.inActive(channel)) {
            log.debug("channel not in active status, message will be discard: {}", message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "channel: [" + channel + "] is not usable");
        }

        try {
            if (unWritable(channel)) {
                log.debug("channel [{}] is not writable", channel);
                ReferenceCountUtil.safeRelease(message);
                return failurePromise(channel, "channel: [" + channel + "] is not writable");
            } else return (ChannelPromise) channel.writeAndFlush(message);
        } catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], remote address is [" + channel.remoteAddress() + "]", exception);
        }
    }
}