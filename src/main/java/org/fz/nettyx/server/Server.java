package org.fz.nettyx.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2021/11/25 15:53
 */

@Slf4j
public abstract class Server {

    private EventLoopGroup
        parentGroup = new NioEventLoopGroup(1),
        childGroup = new NioEventLoopGroup();
    private ServerBootstrap serverBootstrap = new ServerBootstrap().group(parentGroup, childGroup).channel(NioServerSocketChannel.class);

}
