package org.fz.nettyx.template.tcp.client;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.Getter;
import org.fz.nettyx.template.AbstractSingleChannelTemplate;

import java.net.InetSocketAddress;

/**
 * single tcp channel template
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 15:22
 */
@Getter
public abstract class SingleChannelClientTemplate extends AbstractSingleChannelTemplate<NioSocketChannel,
        SocketChannelConfig> {

    private final InetSocketAddress remoteAddress;

    protected SingleChannelClientTemplate(String hostname, int port) {
        this(new InetSocketAddress(hostname, port));
    }

    protected SingleChannelClientTemplate(InetSocketAddress address) {
        super(address);
        this.remoteAddress = address;
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new NioEventLoopGroup();
    }
}
