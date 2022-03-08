package org.fz.nettyx;

import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Used to store channels, using key-value pairs
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/4/29 10:19
 */

public class ChannelStorage<K> {

    private final Map<K, Channel> channelMap;

    public ChannelStorage() {
        this.channelMap = new ConcurrentHashMap<>();
    }

    public ChannelStorage(int initialCapacity) {
        this.channelMap = new ConcurrentHashMap<>(initialCapacity);
    }

    public ChannelStorage(Map<K, Channel> channelMap) {
        this.channelMap = new ConcurrentHashMap<>(channelMap);
    }

    public ChannelStorage(int initialCapacity, float loadFactor) {
        this(initialCapacity, loadFactor, 1);
    }

    public ChannelStorage(int initialCapacity, float loadFactor, int concurrencyLevel) {
        this.channelMap = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
    }

    public void store(K key, Channel channel) {
        channelMap.put(key, channel);
    }

    public Channel get(K key) { return channelMap.get(key); }

    public boolean        isAllActive()       { return isAll(Channel::isActive);       }
    public boolean        isAllWritable()     { return isAll(Channel::isWritable);     }
    public boolean        isAllRegistered()   { return isAll(Channel::isRegistered);   }
    public boolean        isAllOpen()         { return isAll(Channel::isOpen);         }

    public List<Channel>  findAllActive()     { return findAll(Channel::isActive);     }
    public List<Channel>  findAllWritable()   { return findAll(Channel::isWritable);   }
    public List<Channel>  findAllRegistered() { return findAll(Channel::isRegistered); }
    public List<Channel>  findAllOpen()       { return findAll(Channel::isOpen);       }

    public List<Channel> findAll(Predicate<Channel> channelPredicate) {
        List<Channel> channels = new ArrayList<>(10);
        for (Channel channel : channelMap.values()) {
            if (channelPredicate.test(channel)) channels.add(channel);
        }
        return channels;
    }

    public boolean isAll(Predicate<Channel> channelPredicate) {
        for (Channel channel : channelMap.values()) {
            if (channelPredicate.negate().test(channel)) {
                return false;
            }
        }
        return true;
    }

    public void forEach(BiConsumer<K, Channel> action) {
        channelMap.forEach(action);
    }

    public Channel remove(K key) {
        return channelMap.remove(key);
    }

    public void clear() {
        channelMap.clear();
    }

    public Channel compute(K key, BiFunction<K, Channel, Channel> remappingFunction) {
        return channelMap.compute(key, remappingFunction);
    }

    @Override
    public String toString() {
        return this.channelMap.toString();
    }
}
