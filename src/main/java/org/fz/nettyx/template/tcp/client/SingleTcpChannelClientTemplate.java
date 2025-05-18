package org.fz.nettyx.template.tcp.client;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.fz.nettyx.template.AbstractSingleChannelTemplate;

import java.net.InetSocketAddress;

/**
 * single tcp channel template
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 15:22
 */
public abstract class SingleTcpChannelClientTemplate extends AbstractSingleChannelTemplate<NioSocketChannel, SocketChannelConfig> {

    protected SingleTcpChannelClientTemplate(InetSocketAddress address)
    {
        super(address);
    }

    protected SingleTcpChannelClientTemplate(String hostname, int port)
    {
        super(new InetSocketAddress(hostname, port));
    }

    @Override
    protected EventLoopGroup newEventLoopGroup()
    {
        return new NioEventLoopGroup();
    }
}
