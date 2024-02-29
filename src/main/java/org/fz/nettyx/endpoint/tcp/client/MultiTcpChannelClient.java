package org.fz.nettyx.endpoint.tcp.client;

import cn.hutool.core.map.SafeConcurrentHashMap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelFutureAction;
import org.fz.nettyx.endpoint.NettyClient;
import org.fz.nettyx.listener.ActionableChannelFutureListener;
import org.fz.nettyx.util.ChannelStorage;
import org.fz.nettyx.util.Try;

import java.net.SocketAddress;
import java.util.Map;

import static org.fz.nettyx.action.ChannelFutureAction.NOTHING;

/**
 * The type Multi channel client. use channel key to retrieve and use channels
 *
 * @param <K> the channel channelKey type
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 16:58
 */
@Slf4j
@Getter
public abstract class MultiTcpChannelClient<K> extends NettyClient {

    private final AttributeKey<K> channelKey = AttributeKey.valueOf("$multi_tcp_channel_key$");
    protected final ChannelStorage<K> channelStorage = new ChannelStorage<>(16);
    protected final EventLoopGroup eventLoopGroup;
    private final Bootstrap bootstrap;
    private final Map<K, SocketAddress> addressMap;

    protected MultiTcpChannelClient(Map<K, SocketAddress> addressMap) {
        this.eventLoopGroup = new NioEventLoopGroup();
        this.bootstrap = newBootstrap();
        this.addressMap = new SafeConcurrentHashMap<>(addressMap);
    }

    public Channel getChannel(K key) {
        return this.channelStorage.get(key);
    }

    protected void connectAll() {

    }

    protected void connect(K key) {
        ChannelFutureListener listener = new ActionableChannelFutureListener()
                .whenDone(whenConnectDone(key))
                .whenCancel(whenConnectCancel(key))
                .whenSuccess(whenConnectSuccess(key))
                .whenFailure(whenConnectFailure(key));

        getBootstrap()
                .clone()
                .attr(channelKey, key)
                .handler(channelInitializer(key))
                .connect()
                .addListener(listener);
    }

    protected void storeChannel(K channelKey, ChannelFuture future) {
        storeChannel(channelKey, future.channel());
    }

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

    public void closeChannel(K key) {
        getChannel(key).close();
    }

    public void closeChannel(K key, ChannelPromise promise) {
        getChannel(key).close(promise);
    }

    public void closeChannelGracefully(K key) {
        if (gracefullyCloseable(getChannel(key))) this.closeChannel(key);
    }

    public void closeChannelGracefully(K key, ChannelPromise promise) {
        if (gracefullyCloseable(getChannel(key))) this.closeChannel(key, promise);
    }

    public ChannelPromise writeAndFlush(K channelKey, Object message) {
        Channel channel = channelStorage.get(channelKey);

        if (notActive(channel)) {
            log.debug("channel not in active status, message will be discard: {}", message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "channel: [" + channel + "] is not usable");
        }

        try {
            if (notWritable(channel)) {
                log.debug("channel [{}] is not writable, channel key [{}]", channel, channelKey);
                ReferenceCountUtil.safeRelease(message);
                return failurePromise(channel, "channel: [" + channel + "] is not writable");
            } else return (ChannelPromise) channel.writeAndFlush(message);
        }
        catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], remote " +
                                       "address is [" + channel.remoteAddress() + "]", exception);
        }
    }

    protected void clear() {
        channelStorage.clear();
    }

    protected K channelKey(ChannelHandlerContext ctx) {
        return ctx.channel().attr(channelKey).get();
    }

    protected K channelKey(ChannelFuture channelFuture) {
        return channelFuture.channel().attr(channelKey).get();
    }

    protected K channelKey(Channel channel) {
        return channel.attr(channelKey).get();
    }

    //***********************************           override start           *****************************************//

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


    protected Bootstrap newBootstrap() {
        return new Bootstrap()
                .group(getEventLoopGroup())
                .channel(NioSocketChannel.class);
    }

    protected abstract ChannelInitializer<? extends Channel> channelInitializer(K key);

    //************************************           override end           ******************************************//

}
