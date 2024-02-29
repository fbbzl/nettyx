package org.fz.nettyx.endpoint.client;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.map.SafeConcurrentHashMap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.listener.ActionableChannelFutureListener;
import org.fz.nettyx.util.ChannelStorage;
import org.fz.nettyx.util.Throws;
import org.fz.nettyx.util.Try;

import java.net.SocketAddress;
import java.util.Map;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */

@Slf4j
@Getter
public abstract class MultiChannelClient<K, C extends Channel, A extends SocketAddress> extends NettyClient<C> {

    protected final ChannelStorage<K> channelStorage = new ChannelStorage<>(16);
    private final   Map<K, A>         addressMap;
    private final   Map<K, Bootstrap> bootstrapMap;

    protected MultiChannelClient(EventLoopGroup eventLoopGroup, Map<K, A> addressMap) {
        super(eventLoopGroup);
        this.addressMap   = addressMap;
        this.bootstrapMap = new SafeConcurrentHashMap<>(MapUtil.map(addressMap, this::newBootstrap));
    }

    public void connectAll() {
        addressMap.keySet().forEach(this::connect);
    }

    public void connect(K key) {
        ChannelFutureListener listener = new ActionableChannelFutureListener()
                .whenDone(whenConnectDone())
                .whenCancel(whenConnectCancel())
                .whenSuccess(whenConnectSuccess())
                .whenFailure(whenConnectFailure());

        Bootstrap bootstrap = getBootstrapMap().get(key);
        Throws.ifNull(bootstrap, "can not find config by key [" + key + "]");
        bootstrap.clone()
                 .connect()
                 .addListener(listener);
    }

    public Channel getChannel(K key) {
        return this.channelStorage.get(key);
    }

    protected void storeChannel(K channelKey, ChannelFuture future) {
        storeChannel(channelKey, future.channel());
    }

    @SneakyThrows
    protected void storeChannel(K key, Channel channel) {
        channelStorage.compute(key, Try.apply((k, old) -> {
            if (isActive(old)) {
                old.close().sync();
            }

            log.info("has stored channel [{}]", channel);
            return channel;
        }));
    }

    protected void storeChannel(ChannelFuture cf) {
        this.storeChannel(channelKey(cf), cf.channel());
    }

    public void closeChannelGracefully(K key) {
        if (NettyClient.gracefullyCloseable(getChannel(key))) this.getChannel(key).close();
    }

    public void closeChannelGracefully(K key, ChannelPromise promise) {
        if (NettyClient.gracefullyCloseable(getChannel(key))) this.getChannel(key).close(promise);
    }

    public ChannelPromise writeAndFlush(K channelKey, Object message) {
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
        }
        catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], comm-port is " +
                                       "[" + channel.remoteAddress() + "]", exception);
        }
    }

    protected void clear() {
        channelStorage.clear();
    }

    protected K channelKey(ChannelHandlerContext ctx) {
        return ctx.channel().attr(getAttributeKey()).get();
    }

    protected K channelKey(ChannelFuture channelFuture) {
        return channelFuture.channel().attr(getAttributeKey()).get();
    }

    protected K channelKey(Channel channel) {
        return channel.attr(getAttributeKey()).get();
    }

    protected AttributeKey<K> getAttributeKey() {
        return AttributeKey.valueOf("$multi_channel_key$");
    }

    protected Bootstrap newBootstrap(K key, SocketAddress remoteAddress) {
        return new Bootstrap()
                .attr(getAttributeKey(), key)
                .remoteAddress(remoteAddress)
                .group(getEventLoopGroup())
                .channel(channelClass)
                .handler(channelInitializer());
    }

}
