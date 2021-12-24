package org.fz.nettyx.client;


import static org.fz.nettyx.support.Logs.debug;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import java.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.support.ChannelStorage;

/**
 * The type Multi channel client.
 *
 * @param <K> the channel channelKey type
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 16:58
 */
@Slf4j
public abstract class MultiChannelClient<K> extends Client {

    private final AttributeKey<K> channelKey = AttributeKey.valueOf("$channel_key$");

    /**
     * Used to store different channels
     */
    protected final ChannelStorage<K> channelStorage = new ChannelStorage<>(16);

    /**
     * Connect.
     *
     * @param channelKey the channel key
     * @param address the address
     */
    protected abstract void connect(K channelKey, SocketAddress address);

    /**
     * must store channel after connect success!!
     *
     * @param key the channelKey
     * @param channel the channel
     */
    protected void storeChannel(K key, Channel channel) {
        channelStorage.store(key, channel);
        log.info("has stored channel [{}]", channel);
    }

    /**
     * Store according channel future
     *
     * @param cf the cf
     */
    protected void storeChannel(ChannelFuture cf) {
        this.storeChannel(channelKey(cf), cf.channel());
    }

    /**
     * Send.
     *
     * @param channelKey the channel channelKey
     * @param message the message
     */
    public void send(K channelKey, Object message) {
        try {
            Channel channel = channelStorage.get(channelKey);

            if (notReady(channel)) {
                debug(log, "connection has not been initialized, message will be discard: {}", message);
                return;
            }

            channel.writeAndFlush(message);
            debug(log, "has send message to : [{}]", channel.remoteAddress());
        } catch (Exception exception) {
            log.error("exception occurred while sending the message", exception);
        }
    }

    /**
     * Clear channelStorage
     */
    protected void clear() {
        channelStorage.clear();
    }

    /**
     * Cloned bootstrap
     *
     * @param key the channelKey
     * @return the bootstrap
     */
    protected Bootstrap newBootstrap(K key) {
        return super.newBootstrap().attr(channelKey, key);
    }

    /**
     * Channel key k.
     *
     * @param ctx the ctx
     * @return the k
     */
    protected K channelKey(ChannelHandlerContext ctx) {
        return ctx.channel().attr(channelKey).get();
    }

    /**
     * Channel key k.
     *
     * @param channelFuture the channel future
     * @return the k
     */
    protected K channelKey(ChannelFuture channelFuture) {
        return channelFuture.channel().attr(channelKey).get();
    }

    /**
     * Channel key k.
     *
     * @param channel the channel
     * @return the k
     */
    protected K channelKey(Channel channel) {
        return channel.attr(channelKey).get();
    }

}
