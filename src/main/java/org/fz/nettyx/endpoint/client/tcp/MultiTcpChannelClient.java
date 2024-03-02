package org.fz.nettyx.endpoint.client.tcp;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import java.net.InetSocketAddress;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.MultiChannelClient;

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
public abstract class MultiTcpChannelClient<K> extends
                                               MultiChannelClient<K, NioSocketChannel, SocketChannelConfig> {

    protected MultiTcpChannelClient(Map<K, InetSocketAddress> addressMap) {
        super(addressMap);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new NioEventLoopGroup();
    }

    @Override
    protected AttributeKey<K> getAttributeKey() {
        return AttributeKey.valueOf("$multi_tcp_channel_key$");
    }

}
