package org.fz.nettyx.endpoint.client;

import static org.fz.nettyx.action.ChannelFutureAction.NOTHING;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.map.SafeConcurrentHashMap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelException;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ReflectiveChannelFactory;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import java.net.SocketAddress;
import java.util.Map;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelFutureAction;
import org.fz.nettyx.listener.ActionableChannelFutureListener;
import org.fz.nettyx.util.ChannelStorage;
import org.fz.nettyx.util.Throws;
import org.fz.nettyx.util.Try;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */

@Slf4j
@Getter
@SuppressWarnings({"unchecked", "unused"})
public abstract class AbstractMultiChannelClient<K, C extends Channel, F extends ChannelConfig> extends
                                                                                                Client<C> {

    private final ChannelStorage<K>     channelStorage = new ChannelStorage<>(16);
    private final Map<K, SocketAddress> addressMap;
    private final Map<K, Bootstrap>     bootstrapMap;

    protected <S extends SocketAddress> AbstractMultiChannelClient(Map<K, S> addressMap) {
        this.addressMap   = (Map<K, SocketAddress>) addressMap;
        this.bootstrapMap = new SafeConcurrentHashMap<>(MapUtil.map(addressMap, this::newBootstrap));
    }

    public void connectAll() {
        addressMap.keySet().forEach(this::connect);
    }

    public void connect(K key) {
        ChannelFutureListener listener = new ActionableChannelFutureListener()
            .whenDone(whenConnectDone(key))
            .whenCancel(whenConnectCancel(key))
            .whenSuccess(cf -> {
                storeChannel(cf);
                whenConnectSuccess(key).act(cf);
            })
            .whenFailure(whenConnectFailure(key));

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
        if (Client.gracefullyCloseable(getChannel(key))) { this.getChannel(key).close(); }
    }

    public void closeChannelGracefully(K key, ChannelPromise promise) {
        if (Client.gracefullyCloseable(getChannel(key))) { this.getChannel(key).close(promise); }
    }

    public ChannelPromise writeAndFlush(K channelKey, Object message) {
        Channel channel = channelStorage.get(channelKey);

        if (notActive(channel) || notWritable(channel)) {
            log.debug("channel not in usable status, channel key is [{}], message will be discard: {}", channelKey,
                      message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "channel: [" + channel + "] is not usable");
        }

        try {
            return (ChannelPromise) channel.writeAndFlush(message);
        }
        catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], address is " +
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

    protected void doChannelConfig(K targetChannelKey, F channelConfig) {
        // default is nothing
    }

    protected Bootstrap newBootstrap(K key, SocketAddress remoteAddress) {
        return new Bootstrap()
            .attr(getAttributeKey(), key)
            .remoteAddress(remoteAddress)
            .group(getEventLoopGroup())
            .channelFactory(() -> {
                C chl = new ReflectiveChannelFactory<>(getChannelClass()).newChannel();
                doChannelConfig(key, (F) chl.config());
                return chl;
            })
            .handler(channelInitializer());
    }

    protected ChannelFutureAction whenConnectDone(K key) {
        return NOTHING;
    }

    protected ChannelFutureAction whenConnectCancel(K key) {
        return NOTHING;
    }

    protected ChannelFutureAction whenConnectSuccess(K key) {
        return NOTHING;
    }

    protected ChannelFutureAction whenConnectFailure(K key) {
        return NOTHING;
    }

}
