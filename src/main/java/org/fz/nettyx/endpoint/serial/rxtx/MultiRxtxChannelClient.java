package org.fz.nettyx.endpoint.serial.rxtx;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.rxtx.RxtxDeviceAddress;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.NettyClient;
import org.fz.nettyx.util.ChannelStorage;


/**
 * A multi-channel client that uses channel-key to retrieve and manipulate the specified channel
 * @author fengbinbin
 * @since 2022-01-26 20:26
 **/

@SuppressWarnings("deprecation")
@Slf4j
public abstract class MultiRxtxChannelClient<K> extends RxtxClient {

    private final AttributeKey<K> channelKey = AttributeKey.valueOf("$rxtx_channel_key$");

    /**
     * Used to store different channels
     */
    protected final ChannelStorage<K> channelStorage = new ChannelStorage<>(16);

    public Channel getChannel(K key) {
        return this.channelStorage.get(key);
    }

    /**
     * Connect.
     *
     * @param channelKey the channel key
     * @param address    the address
     */
    protected abstract ChannelFuture connect(K channelKey, RxtxDeviceAddress address) throws Exception;

    protected void storeChannel(K channelKey, ChannelFuture future) {
        storeChannel(channelKey, future.channel());
    }

    /**
     * must store channel after connect success!!
     *
     * @param key     the channelKey
     * @param channel the channeliopuj
     */
    @SneakyThrows
    protected void storeChannel(K key, Channel channel) {
        Channel oldChannel = channelStorage.get(key);
        if (isActive(oldChannel)) {
            oldChannel.close().sync();
            channelStorage.remove(key);
        }
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

    public void closeChannel(K key) {
        getChannel(key).close();
    }

    public void closeChannel(K key, ChannelPromise promise) {
        getChannel(key).close(promise);
    }

    public void closeChannelGracefully(K key) {
        if (NettyClient.gracefullyCloseable(getChannel(key))) this.closeChannel(key);
    }

    public void closeChannelGracefully(K key, ChannelPromise promise) {
        if (NettyClient.gracefullyCloseable(getChannel(key))) this.closeChannel(key, promise);
    }

    /**
     * Send.
     *
     * @param channelKey the channel channelKey
     * @param message    the message
     */
    public ChannelPromise send(K channelKey, Object message) {
        Channel channel = channelStorage.get(channelKey);

        if (notActive(channel)) {
            log.debug("comm channel not in active status, message will be discard: {}", message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "comm channel: [" + channel + "] is not usable");
        }

        try {
            if (notWritable(channel)) {
                log.debug("comm channel [{}] is not writable, channel key [{}]", channel, channelKey);
                ReferenceCountUtil.safeRelease(message);
                return failurePromise(channel, "comm channel: [" + channel + "] is not writable");
            } else return (ChannelPromise) channel.writeAndFlush(message);
        } catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], comm-port is [" + channel.remoteAddress() + "]", exception);
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
    protected Bootstrap cloneBootstrap(K key) {
        return super.cloneBootstrap().attr(channelKey, key);
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
