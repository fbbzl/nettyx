package org.fz.nettyx.template.tcp.client;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.template.AbstractSingleChannelTemplate;

import java.net.InetSocketAddress;

/**
 * Single channel client
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 15:22
 */
@Slf4j
public abstract class SingleTcpChannellClient extends AbstractSingleChannelTemplate<NioSocketChannel, SocketChannelConfig> {

    protected SingleTcpChannellClient(InetSocketAddress address) {
        super(address);
    }

    protected SingleTcpChannellClient(String hostname, int port) {
        super(new InetSocketAddress(hostname, port));
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new NioEventLoopGroup();
    }
}
