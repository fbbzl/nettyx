package org.fz.nettyx.endpoint.client.tcp;


import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import java.net.InetSocketAddress;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.endpoint.client.AbstractSingleChannelClient;

/**
 * Single channel client
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/6 15:22
 */
@Slf4j
public abstract class SingleTcpChannelClient extends AbstractSingleChannelClient<NioSocketChannel> {

    protected SingleTcpChannelClient(InetSocketAddress address) {
        super(address);
    }

    protected SingleTcpChannelClient(String hostname, int port) {
        super(new InetSocketAddress(hostname, port));
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new NioEventLoopGroup();
    }
}
