package org.fz.nettyx.template.websocket.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.fz.nettyx.template.tcp.server.ServerTemplate;

import static io.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;

/**
 * @author fengbinbin
 * @version 1.0.0
 * @since 2022-10-23 9:57
 */

public abstract class WebSocketServerTemplate extends ServerTemplate {

    protected WebSocketServerTemplate(int bindPort) {
        super(bindPort);
    }

    @Override
    protected ChannelInitializer<? extends Channel> childChannelInitializer() {
        return new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel socketChannel) {
                socketChannel.pipeline()
                             .addLast(defaultWebsocketServerHandlers())
                             .addLast(childChannelHandlers());
            }
        };
    }

    /**
     * common websocket-server handlers
     *
     * @return websocket handlers
     */
    protected ChannelHandler[] defaultWebsocketServerHandlers() {
        return new ChannelHandler[] {
                new HttpServerCodec(),
                new ChunkedWriteHandler(),
                new HttpObjectAggregator(8192),
                new WebSocketServerProtocolHandler("/ws", WEBSOCKET, true, 65536 * 10)
        };
    }

    protected abstract ChannelHandler[] childChannelHandlers();
}
