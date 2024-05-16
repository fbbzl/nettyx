package org.fz.nettyx.template.tcp.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * basic server abstraction
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/5/13 9:10
 */

@Slf4j
@Getter
public abstract class TcpServer {

    private final EventLoopGroup
            parentEventLoopGroup,
            childEventLoopGroup;

    private final ServerBootstrap serverBootstrap;

    protected TcpServer(int bindPort) {
        InetSocketAddress bindAddress = new InetSocketAddress(bindPort);
        this.childEventLoopGroup  = childEventLoopGroup();
        this.parentEventLoopGroup = parentEventLoopGroup();
        this.serverBootstrap      = newServerBootstrap(bindAddress);
    }

    protected EventLoopGroup parentEventLoopGroup() {
        return new NioEventLoopGroup();
    }

    protected EventLoopGroup childEventLoopGroup() {
        return new NioEventLoopGroup();
    }

    protected void doChannelConfig(ServerSocketChannel channel) {
        // default is nothing
    }

    public ChannelFuture bind() {
        return this.getServerBootstrap().clone().bind();
    }

    protected ServerBootstrap newServerBootstrap(SocketAddress bindAddress) {
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

    protected void shutdownGracefully() {
        childEventLoopGroup.shutdownGracefully();
        parentEventLoopGroup.shutdownGracefully();
    }

    protected void syncShutdownGracefully() throws InterruptedException {
        childEventLoopGroup.shutdownGracefully().sync();
        parentEventLoopGroup.shutdownGracefully().sync();
    }

}
