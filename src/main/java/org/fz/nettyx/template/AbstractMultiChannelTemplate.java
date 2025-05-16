package org.fz.nettyx.template;

import cn.hutool.core.map.MapUtil;
import cn.hutool.core.map.SafeConcurrentHashMap;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.fz.erwin.exception.Throws;
import org.fz.erwin.lambda.Try;
import org.fz.nettyx.listener.ActionChannelFutureListener;
import org.fz.nettyx.util.ChannelState;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;

import static org.fz.nettyx.util.ChannelState.CHANNEL_STATE_KEY;

/**
 * multi-channel template
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024/3/1 14:44
 */

@Getter
@SuppressWarnings({ "unchecked", "unused" })
public abstract class AbstractMultiChannelTemplate<K, C extends Channel, F extends ChannelConfig> extends Template<C> {

    protected static final AttributeKey<?> MULTI_CHANNEL_KEY = AttributeKey.valueOf("__$multi_channel_key$");
    private static final   InternalLogger  log               = InternalLoggerFactory.getInstance(AbstractMultiChannelTemplate.class);

    private final ChannelStorage<K>           channelStorage = new ChannelStorage<>(16);
    private final Map<K, SocketAddress>       addressMap;
    private final ConcurrentMap<K, Bootstrap> bootstrapMap;

    protected <S extends SocketAddress> AbstractMultiChannelTemplate(Map<K, S> addressMap)
    {
        this.addressMap   = (Map<K, SocketAddress>) addressMap;
        this.bootstrapMap = new SafeConcurrentHashMap<>(MapUtil.map(addressMap, this::newBootstrap));
    }

    public Map<K, ChannelFuture> connectAll()
    {
        return MapUtil.map(addressMap, (k, v) -> this.connect(k));
    }

    public ChannelFuture connect(K key)
    {
        Bootstrap bootstrap = getBootstrapMap().get(key);
        Throws.ifNull(bootstrap, () -> "can not find config by key [" + key + "]");
        ChannelFuture channelFuture = bootstrap.clone().connect();
        channelFuture.addListener(new ActionChannelFutureListener().whenSuccess((l, cf) -> this.storeChannel(cf)));
        return channelFuture;
    }

    public Channel getChannel(K key)
    {
        return this.channelStorage.get(key);
    }

    protected void storeChannel(K channelKey, ChannelFuture future)
    {
        storeChannel(channelKey, future.channel());
    }

    protected void storeChannel(K key, Channel channel)
    {
        channelStorage.compute(key, Try.apply((k, old) -> {
            if (isActive(old)) {
                old.close().sync();
            }
            log.debug("has stored channel [{}]", channel);
            return channel;
        }));
    }

    protected void storeChannel(ChannelFuture cf)
    {
        this.storeChannel(channelKey(cf), cf.channel());
    }

    public void closeChannelGracefully(K key)
    {
        if (gracefullyCloseable(getChannel(key))) {
            this.getChannel(key).close();
        }
    }

    public void closeChannelGracefully(K key, ChannelPromise promise)
    {
        if (gracefullyCloseable(getChannel(key))) {
            this.getChannel(key).close(promise);
        }
    }

    public ChannelPromise write(K channelKey, Object message)
    {
        Channel channel = channelStorage.get(channelKey);

        if (notActive(channel) || notWritable(channel)) {
            log.debug("channel not in usable status, channel key is [{}], message will be discard: {}", channelKey,
                      message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "channel: [" + channel + "] is not usable");
        }

        try {
            return (ChannelPromise) channel.write(message);
        } catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], address is " +
                                       "[" + channel.remoteAddress() + "]", exception);
        }
    }

    public ChannelPromise writeAndFlush(K channelKey, Object message)
    {
        Channel channel = channelStorage.get(channelKey);

        if (notActive(channel) || notWritable(channel)) {
            log.debug("channel not in usable status, channel key is [{}], message will be discard: {}", channelKey,
                      message);
            ReferenceCountUtil.safeRelease(message);
            return failurePromise(channel, "channel: [" + channel + "] is not usable");
        }

        try {
            return (ChannelPromise) channel.writeAndFlush(message);
        } catch (Exception exception) {
            throw new ChannelException("exception occurred while sending the message [" + message + "], address is " +
                                       "[" + channel.remoteAddress() + "]", exception);
        }
    }

    protected void clear()
    {
        channelStorage.clear();
    }

    protected void doChannelConfig(K channelKey, F channelConfig)
    {
        // default is nothing
    }

    protected Bootstrap newBootstrap(K key, SocketAddress remoteAddress)
    {
        return new Bootstrap()
                .attr((AttributeKey<? super K>) MULTI_CHANNEL_KEY, key)
                .attr(CHANNEL_STATE_KEY, new ChannelState())
                .remoteAddress(remoteAddress)
                .group(getEventLoopGroup())
                .channelFactory(() -> {
                    C chl = new ReflectiveChannelFactory<>(getChannelClass()).newChannel();
                    doChannelConfig(key, (F) chl.config());
                    return chl;
                })
                .handler(channelInitializer());
    }

    public static <T> T channelKey(ChannelHandlerContext ctx)
    {
        return channelKey(ctx.channel());
    }

    public static <T> T channelKey(ChannelFuture cf)
    {
        return channelKey(cf.channel());
    }

    public static <T> T channelKey(Channel channel)
    {
        return (T) channel.attr(MULTI_CHANNEL_KEY).get();
    }

    /**
     * Used to store channels, using key-length pairs
     *
     * @param <K> the type parameter
     * @author fengbinbin
     * @version 1.0
     * @since 2021 /4/29 10:19
     */
    public static class ChannelStorage<K> {

        @Delegate
        private final Map<K, Channel> storage;

        /**
         * Instantiates a new Channel storage.
         */
        public ChannelStorage()
        {
            this.storage = new SafeConcurrentHashMap<>(8);
        }

        /**
         * Instantiates a new Channel storage.
         *
         * @param initialCapacity the initial capacity
         */
        public ChannelStorage(int initialCapacity)
        {
            this.storage = new SafeConcurrentHashMap<>(initialCapacity);
        }

        /**
         * Instantiates a new Channel storage.
         *
         * @param channelMap the channel map
         */
        public ChannelStorage(Map<K, Channel> channelMap)
        {
            this.storage = new SafeConcurrentHashMap<>(channelMap);
        }

        /**
         * Instantiates a new Channel storage.
         *
         * @param initialCapacity the initial capacity
         * @param loadFactor      the load factor
         */
        public ChannelStorage(int initialCapacity, float loadFactor)
        {
            this(initialCapacity, loadFactor, 1);
        }

        /**
         * Instantiates a new Channel storage.
         *
         * @param initialCapacity  the initial capacity
         * @param loadFactor       the load factor
         * @param concurrencyLevel the concurrency level
         */
        public ChannelStorage(
                int   initialCapacity,
                float loadFactor,
                int   concurrencyLevel)
        {
            this.storage = new SafeConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
        }

        /**
         * Is all active boolean.
         *
         * @return the boolean
         */
        public boolean isAllActive()
        {
            return isAll(Channel::isActive);
        }

        /**
         * Is all writable boolean.
         *
         * @return the boolean
         */
        public boolean isAllWritable()
        {
            return isAll(Channel::isWritable);
        }

        /**
         * Is all registered boolean.
         *
         * @return the boolean
         */
        public boolean isAllRegistered()
        {
            return isAll(Channel::isRegistered);
        }

        /**
         * Is all open boolean.
         *
         * @return the boolean
         */
        public boolean isAllOpen()
        {
            return isAll(Channel::isOpen);
        }

        /**
         * Find all active list.
         *
         * @return the list
         */
        public List<Channel> findAllActive()
        {
            return findAll(Channel::isActive);
        }

        /**
         * Find all writable list.
         *
         * @return the list
         */
        public List<Channel> findAllWritable()
        {
            return findAll(Channel::isWritable);
        }

        /**
         * Find all registered list.
         *
         * @return the list
         */
        public List<Channel> findAllRegistered()
        {
            return findAll(Channel::isRegistered);
        }

        /**
         * Find all open list.
         *
         * @return the list
         */
        public List<Channel> findAllOpen()
        {
            return findAll(Channel::isOpen);
        }

        /**
         * Find all list.
         *
         * @param channelPredicate the channel predicate
         * @return the list
         */
        public List<Channel> findAll(Predicate<Channel> channelPredicate)
        {
            List<Channel> channels = new ArrayList<>(10);
            for (Channel channel : storage.values()) {
                if (channelPredicate.test(channel)) channels.add(channel);
            }
            return channels;
        }

        /**
         * Is all boolean.
         *
         * @param channelPredicate the channel predicate
         * @return the boolean
         */
        public boolean isAll(Predicate<Channel> channelPredicate)
        {
            for (Channel channel : storage.values()) {
                if (channelPredicate.negate().test(channel)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String toString() {
            return this.storage.toString();
        }
    }
}
