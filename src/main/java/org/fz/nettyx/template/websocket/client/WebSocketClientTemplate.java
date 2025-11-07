package org.fz.nettyx.template.websocket.client;

import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannelConfig;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.template.AbstractSingleChannelTemplate;

import java.net.InetSocketAddress;
import java.net.URI;

/**
 * @author fengbinbin
 * @version 1.0.0
 * @since 2022-10-23 9:57
 */
public abstract class WebSocketClientTemplate extends AbstractSingleChannelTemplate<NioSocketChannel, SocketChannelConfig> {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(WebSocketClientTemplate.class);

    public static final AttributeKey<WebSocketClientHandshaker> HAND_SHAKER = AttributeKey.valueOf("$_handshake_$");

    protected URI uri;

    public WebSocketClientTemplate(URI uri) {
        super(new InetSocketAddress(uri.getHost(), uri.getPort() < 0 ? 80 : uri.getPort()));
        this.uri = uri;
    }

    @Override
    protected final ChannelInitializer<NioSocketChannel> channelInitializer() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(NioSocketChannel chl) {
                chl.pipeline()
                   .addLast(defaultWebsocketClientHandlers(chl))
                   .addLast(channelHandlers(chl));
            }
        };
    }

    /**
     * common websocket handlers
     *
     * @return websocket handlers
     */
    protected ChannelHandler[] defaultWebsocketClientHandlers(NioSocketChannel chl) {
        WebSocketClientHandshaker webSocketClientHandshaker =
                WebSocketClientHandshakerFactory.newHandshaker(uri, WebSocketVersion.V13, null, false, new DefaultHttpHeaders());
        chl.attr(HAND_SHAKER).set(webSocketClientHandshaker);
        return new ChannelHandler[] {
                new HttpClientCodec(),
                new ChunkedWriteHandler(),
                new HttpObjectAggregator(8192),
                new WebSocketClientProtocolHandler(webSocketClientHandshaker)
        };
    }

    @Override
    protected EventLoopGroup newEventLoopGroup() {
        return new NioEventLoopGroup();
    }

    protected abstract ChannelHandler[] channelHandlers(Channel channel);

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public abstract static class StreamableChannelHandler extends SimpleChannelInboundHandler<WebSocketFrame> {
        private static final InternalLogger log = InternalLoggerFactory.getInstance(StreamableChannelHandler.class);
        protected            Channel        upstream;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            getHandshaker(ctx.channel()).handshake(ctx.channel()).sync();
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame frame) {
            Channel                   chl        = ctx.channel();
            WebSocketClientHandshaker handshaker = getHandshaker(chl);
            if (!handshaker.isHandshakeComplete()) {
                try {
                    handshaker.finishHandshake(chl, (FullHttpResponse) frame);
                    log.info("WebSocket Client connected!");
                }
                catch (WebSocketHandshakeException e) {
                    log.info("WebSocket Client failed to do handshake");
                }
                return;
            }

            if (frame instanceof BinaryWebSocketFrame binaryFrame) {
                log.info("WebSocket Client received binary message: {} bytes", binaryFrame.content().readableBytes());
                // stream
                upstream.writeAndFlush(binaryFrame.content());
            }
        }

        //************************************************ private start *********************************************//

        private WebSocketClientHandshaker getHandshaker(Channel ch) {
            return ch.attr(HAND_SHAKER).get();
        }

    }
}
