package org.fz.nettyx.template.tcp.client;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.template.AbstractMultiChannelEndpoint;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * The type Multi channel client. use channel key to retrieve and use channels
 *
 * @param <K> the channel channelKey type
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 16:58
 */
@Slf4j
public abstract class MultiTcpChannelClient<K> extends
                                               AbstractMultiChannelEndpoint<K, NioSocketChannel, SocketChannelConfig> {

    protected MultiTcpChannelClient(Map<K, InetSocketAddress> addressMap) {
        super(addressMap);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new NioEventLoopGroup();
    }

}
