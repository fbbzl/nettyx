package org.fz.nettyx.util;

import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Used to store channels, using key-length pairs
 *
 * @param <K> the type parameter
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /4/29 10:19
 */
public class ChannelStorage<K> {

    private final Map<K, Channel> channelMap;

    /**
     * Instantiates a new Channel storage.
     */
    public ChannelStorage() {
        this.channelMap = new ConcurrentHashMap<>();
    }

    /**
     * Instantiates a new Channel storage.
     *
     * @param initialCapacity the initial capacity
     */
    public ChannelStorage(int initialCapacity) {
        this.channelMap = new ConcurrentHashMap<>(initialCapacity);
    }

    /**
     * Instantiates a new Channel storage.
     *
     * @param channelMap the channel map
     */
    public ChannelStorage(Map<K, Channel> channelMap) {
        this.channelMap = new ConcurrentHashMap<>(channelMap);
    }

    /**
     * Instantiates a new Channel storage.
     *
     * @param initialCapacity the initial capacity
     * @param loadFactor      the load factor
     */
    public ChannelStorage(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, 1);
    }

    /**
     * Instantiates a new Channel storage.
     *
     * @param initialCapacity  the initial capacity
     * @param loadFactor       the load factor
     * @param concurrencyLevel the concurrency level
     */
    public ChannelStorage(int initialCapacity, float loadFactor, int concurrencyLevel) {
        this.channelMap = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }

    /**
     * Store.
     *
     * @param key     the key
     * @param channel the channel
     */
    public void store(K key, Channel channel) {
        channelMap.put(key, channel);
    }

    /**
     * Get channel.
     *
     * @param key the key
     * @return the channel
     */
    public Channel get(K key) {
        return channelMap.get(key);
    }

    /**
     * Is all active boolean.
     *
     * @return the boolean
     */
    public boolean isAllActive() {
        return isAll(Channel::isActive);
    }

    /**
     * Is all writable boolean.
     *
     * @return the boolean
     */
    public boolean isAllWritable() {
        return isAll(Channel::isWritable);
    }

    /**
     * Is all registered boolean.
     *
     * @return the boolean
     */
    public boolean isAllRegistered() {
        return isAll(Channel::isRegistered);
    }

    /**
     * Is all open boolean.
     *
     * @return the boolean
     */
    public boolean isAllOpen() {
        return isAll(Channel::isOpen);
    }

    /**
     * Find all active list.
     *
     * @return the list
     */
    public List<Channel> findAllActive() {
        return findAll(Channel::isActive);
    }

    /**
     * Find all writable list.
     *
     * @return the list
     */
    public List<Channel> findAllWritable() {
        return findAll(Channel::isWritable);
    }

    /**
     * Find all registered list.
     *
     * @return the list
     */
    public List<Channel> findAllRegistered() {
        return findAll(Channel::isRegistered);
    }

    /**
     * Find all open list.
     *
     * @return the list
     */
    public List<Channel> findAllOpen() {
        return findAll(Channel::isOpen);
    }

    /**
     * Find all list.
     *
     * @param channelPredicate the channel predicate
     * @return the list
     */
    public List<Channel> findAll(Predicate<Channel> channelPredicate) {
        List<Channel> channels = new ArrayList<>(10);
        for (Channel channel : channelMap.values()) {
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
    public boolean isAll(Predicate<Channel> channelPredicate) {
        for (Channel channel : channelMap.values()) {
            if (channelPredicate.negate().test(channel)) {
                return false;
            }
        }
        return true;
    }

    /**
     * For each.
     *
     * @param action the action
     */
    public void forEach(BiConsumer<K, Channel> action) {
        channelMap.forEach(action);
    }

    /**
     * Remove channel.
     *
     * @param key the key
     * @return the channel
     */
    public Channel remove(K key) {
        return channelMap.remove(key);
    }

    /**
     * Clear.
     */
    public void clear() {
        channelMap.clear();
    }

    /**
     * Compute channel.
     *
     * @param key               the key
     * @param remappingFunction the remapping function
     * @return the channel
     */
    public Channel compute(K key, BiFunction<K, Channel, Channel> remappingFunction) {
        return channelMap.compute(key, remappingFunction);
    }

    @Override
    public String toString() {
        return this.channelMap.toString();
    }
}
