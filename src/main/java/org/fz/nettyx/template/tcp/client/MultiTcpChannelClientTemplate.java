package org.fz.nettyx.template.tcp.client;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.template.AbstractMultiChannelTemplate;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * multi tcp channel template
 *
 * @param <K> the channel channelKey type
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 16:58
 */
public abstract class MultiTcpChannelClientTemplate<K> extends
                                                       AbstractMultiChannelTemplate<K, NioSocketChannel, SocketChannelConfig> {

    protected MultiTcpChannelClientTemplate(Map<K, InetSocketAddress> addressMap) {
        super(addressMap);
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new NioEventLoopGroup();
    }

}
