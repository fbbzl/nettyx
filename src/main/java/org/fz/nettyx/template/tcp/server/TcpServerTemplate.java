package org.fz.nettyx.template.tcp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.Getter;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * basic server template
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/5/13 9:10
 */

@Getter
public abstract class TcpServerTemplate {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(TcpServerTemplate.class);
    private final        EventLoopGroup parentEventLoopGroup, childEventLoopGroup;

    private final ServerBootstrap serverBootstrap;

    protected TcpServerTemplate(int bindPort)
    {
        InetSocketAddress bindAddress = new InetSocketAddress(bindPort);
        this.childEventLoopGroup  = childEventLoopGroup();
        this.parentEventLoopGroup = parentEventLoopGroup();
        this.serverBootstrap      = newServerBootstrap(bindAddress);
    }

    protected EventLoopGroup parentEventLoopGroup()
    {
        return new NioEventLoopGroup();
    }

    protected EventLoopGroup childEventLoopGroup()
    {
        return new NioEventLoopGroup();
    }

    protected void doChannelConfig(ServerSocketChannel channel) {
        // default is nothing
    }

    public ChannelFuture bind()
    {
        ChannelFuture bindFuture = this.getServerBootstrap().clone().bind();
        log.debug("already finish bind, bind future is [{}]", bindFuture);
        return bindFuture;
    }

    protected ServerBootstrap newServerBootstrap(SocketAddress bindAddress)
    {
        return new ServerBootstrap()
                .group(parentEventLoopGroup, childEventLoopGroup)
                .localAddress(bindAddress)
                .channelFactory(() -> {
                    NioServerSocketChannel serverSocketChannel = new NioServerSocketChannel();
                    doChannelConfig(serverSocketChannel);
                    return serverSocketChannel;
                })
                .childHandler(childChannelInitializer());
    }

    protected abstract ChannelInitializer<? extends Channel> childChannelInitializer();

    protected void shutdownGracefully()
    {
        childEventLoopGroup.shutdownGracefully();
        parentEventLoopGroup.shutdownGracefully();
    }

    protected void syncShutdownGracefully() throws InterruptedException
    {
        childEventLoopGroup.shutdownGracefully().sync();
        parentEventLoopGroup.shutdownGracefully().sync();
        log.debug("has already successfully shutdown, child-event-loop-group is [{}], parent-event-loop-group is [{}]", childEventLoopGroup, parentEventLoopGroup);
    }

}
