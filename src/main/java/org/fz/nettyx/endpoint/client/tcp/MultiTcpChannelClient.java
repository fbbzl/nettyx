package org.fz.nettyx.endpoint.client.tcp;

import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.MultiChannelClient;

import java.net.InetSocketAddress;
import java.util.Map;

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
public abstract class MultiTcpChannelClient<K> extends MultiChannelClient<K, NioSocketChannel, InetSocketAddress> {

    protected MultiTcpChannelClient(Map<K, InetSocketAddress> addressMap) {
        super(new NioEventLoopGroup(), addressMap);
    }

    @Override
    protected AttributeKey<K> getAttributeKey() {
        return AttributeKey.valueOf("$multi_tcp_channel_key$");
    }

}
