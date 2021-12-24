package org.fz.nettyx;

import io.netty.channel.Channel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

/**
 * Used to store channels, using key-value pairs
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/4/29 10:19
 */

public class ChannelStorage<K> {

    private final Map<K, Channel> channelMap;

    public ChannelStorage(int initialCapacity) {
        channelMap = new ConcurrentHashMap<>(initialCapacity);
    }

    public void store(K key, Channel channel) {
        channelMap.put(key, channel);
    }

    public Channel get(K key) {
        return channelMap.get(key);
    }

    public boolean isAllActive() {
        for (Channel channel : channelMap.values()) {
            if (!channel.isActive()) {
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
