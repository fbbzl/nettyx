package org.fz.nettyx.handler;

import io.netty.channel.*;
import lombok.*;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.*;
import org.fz.nettyx.handler.ChannelAdvice.InboundAdvice;
import org.fz.nettyx.handler.ChannelAdvice.OutboundAdvice;
import org.fz.nettyx.handler.actionable.ActionableIdleStateHandler;
import org.fz.nettyx.handler.actionable.ActionableReadTimeoutHandler;
import org.fz.nettyx.handler.actionable.ActionableWriteTimeoutHandler;

import java.net.SocketAddress;

import static org.fz.nettyx.action.Actions.invokeAction;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/2/27 10:58
 */

public class ChannelAdvice extends CombinedChannelDuplexHandler<InboundAdvice, OutboundAdvice> {

    /**
     * The type Inbound advice.
     */
    @Slf4j
    @Setter
    @Getter
    @RequiredArgsConstructor
    @Accessors(chain = true, fluent = true)
    public static class InboundAdvice extends ChannelInboundHandlerAdapter {

        private final Channel channel;
        private ChannelHandlerContextAction whenChannelRegister, whenChannelUnRegister, whenChannelActive,
                whenChannelInactive, whenWritabilityChanged, whenChannelReadComplete;
        private ChannelReadAction whenChannelRead;
        private ChannelExceptionAction whenExceptionCaught;
        private ActionableIdleStateHandler readIdleStateHandler;
        private ActionableReadTimeoutHandler readTimeoutHandler;

        /**
         * When read idle inbound advice.
         *
         * @param idleSeconds the idle seconds
         * @param readIdleAct the read idle action
         *
         * @return the inbound advice
         */
        public final InboundAdvice whenReadIdle(int idleSeconds, ChannelHandlerContextAction readIdleAct) {
            this.readIdleStateHandler = ActionableIdleStateHandler.newReadIdleHandler(idleSeconds, readIdleAct);
            this.channel.pipeline().addFirst(this.readIdleStateHandler);
            return this;
        }

        /**
         * When read timeout inbound advice.
         *
         * @param timeoutSeconds the timeout seconds
         * @param timeoutAction  the timeout action
         *
         * @return the inbound advice
         */
        public final InboundAdvice whenReadTimeout(int timeoutSeconds, ChannelExceptionAction timeoutAction) {
            this.readTimeoutHandler = new ActionableReadTimeoutHandler(timeoutSeconds, timeoutAction);
            this.channel.pipeline().addFirst(this.readTimeoutHandler);
            return this;
        }

        @Override
        public boolean isSharable() {
            return true;
        }

        @Override
        public final void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel registered, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                      ctx.channel().localAddress());
            invokeAction(whenChannelRegister, ctx);
            super.channelRegistered(ctx);
        }

        @Override
        public final void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel unregistered, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                      ctx.channel().localAddress());
            invokeAction(whenChannelUnRegister, ctx);
            super.channelUnregistered(ctx);
        }

        @Override
        public final void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("channel active event triggered, address is [{}]", ctx.channel().remoteAddress());
            invokeAction(whenChannelActive, ctx);
            super.channelActive(ctx);
        }

        @Override
        public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.warn("channel in-active event triggered, address is [{}]", ctx.channel().remoteAddress());
            invokeAction(whenChannelInactive, ctx);
            super.channelInactive(ctx);
        }

        @Override
        public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.debug("channel read, remote-address is [{}], local-address is [{}], message is [{}]",
                      ctx.channel().remoteAddress(), ctx.channel().localAddress(), msg);
            invokeAction(whenChannelRead, ctx, msg);
            super.channelRead(ctx, msg);
        }

        @Override
        public final void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel read complete, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                      ctx.channel().localAddress());
            invokeAction(whenChannelReadComplete, ctx);
            super.channelReadComplete(ctx);
        }

        @Override
        public final void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel writability changed, remote-address is [{}], local-address is [{}]",
                      ctx.channel().remoteAddress(), ctx.channel().localAddress());
            invokeAction(whenWritabilityChanged, ctx);
            super.channelWritabilityChanged(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            log.error("channel handler exception occurred, remote-address is [{}], local-address is [{}]",
                      ctx.channel().remoteAddress(), ctx.channel().localAddress(), cause);
            invokeAction(whenExceptionCaught, ctx, cause);
            super.exceptionCaught(ctx, cause);
        }
    }

    /**
     * The type Outbound advice.
     */
    @Slf4j
    @Setter
    @Getter
    @RequiredArgsConstructor
    @Accessors(chain = true, fluent = true)
    public static class OutboundAdvice extends ChannelOutboundHandlerAdapter {

        private final Channel channel;
        private ChannelBindAction whenBind;
        private ChannelConnectAction whenConnect;
        private ChannelPromiseAction whenDisconnect, whenClose, whenDeregister;
        private ChannelHandlerContextAction whenRead, whenFlush;
        private ChannelWriteAction whenWrite;
        private ChannelExceptionAction whenExceptionCaught;
        private ActionableIdleStateHandler writeIdleStateHandler;
        private ActionableWriteTimeoutHandler writeTimeoutHandler;

        /**
         * When write idle outbound advice.
         *
         * @param idleSeconds the idle seconds
         * @param writeIdleAct the write idle action
         * @return the outbound advice
         */
        public final OutboundAdvice whenWriteIdle(int idleSeconds, ChannelHandlerContextAction writeIdleAct) {
            this.writeIdleStateHandler = ActionableIdleStateHandler.newWriteIdleHandler(idleSeconds, writeIdleAct);
            this.channel.pipeline().addFirst(this.writeIdleStateHandler);
            return this;
        }

        /**
         * When write timeout outbound advice.
         *
         * @param timeoutSeconds the timeout seconds
         * @param timeoutAction the timeout action
         * @return the outbound advice
         */
        public final OutboundAdvice whenWriteTimeout(int timeoutSeconds, ChannelExceptionAction timeoutAction) {
            this.writeTimeoutHandler = new ActionableWriteTimeoutHandler(timeoutSeconds, timeoutAction);
            this.channel.pipeline().addFirst(this.writeTimeoutHandler);
            return this;
        }

        public final OutboundAdvice whenExceptionCaught(ChannelExceptionAction whenExceptionCaught) {
            this.whenExceptionCaught = whenExceptionCaught;
            this.channel.pipeline().addFirst(new SimpleOutboundExceptionHandler(whenExceptionCaught));
            return this;
        }

        @Override
        public boolean isSharable() {
            return true;
        }

        @Override
        public final void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise)
            throws Exception {
            log.debug("channel binding, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                localAddress);
            invokeAction(whenBind, ctx, localAddress, promise);
            super.bind(ctx, localAddress, promise);
        }

        @Override
        public final void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
            ChannelPromise promise) throws Exception {
            log.debug("channel connecting, remote-address is [{}], local-address is [{}]", remoteAddress, localAddress);
            invokeAction(whenConnect, ctx, remoteAddress, localAddress, promise);
            super.connect(ctx, remoteAddress, localAddress, promise);
        }

        @Override
        public final void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            log.debug("channel disconnect, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress());
            invokeAction(whenDisconnect, ctx, promise);
            super.disconnect(ctx, promise);
        }

        @Override
        public final void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            log.debug("channel close, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress());
            invokeAction(whenClose, ctx, promise);
            super.close(ctx, promise);
        }

        @Override
        public final void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            log.debug("channel deregister, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress());
            invokeAction(whenDeregister, ctx, promise);
            super.deregister(ctx, promise);
        }

        @Override
        public final void read(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel read during writing, remote-address is [{}], local-address is [{}]",
                ctx.channel().remoteAddress(), ctx.channel().localAddress());
            invokeAction(whenRead, ctx);
            super.read(ctx);
        }

        @Override
        public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            log.debug("channel write, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress());
            invokeAction(whenWrite, ctx, msg, promise);
            super.write(ctx, msg, promise);
        }

        @Override
        public final void flush(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel flush, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress());
            invokeAction(whenFlush, ctx);
            super.flush(ctx);
        }

        @Setter
        @Slf4j
        @NoArgsConstructor
        @AllArgsConstructor
        private static class SimpleOutboundExceptionHandler extends ChannelOutboundHandlerAdapter {

            private ChannelExceptionAction whenExceptionCaught;
            @Override
            public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
                promise.addListener(failureListener(ctx, this.whenExceptionCaught));
                super.bind(ctx, localAddress, promise);
            }

            @Override
            public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
                promise.addListener(failureListener(ctx, this.whenExceptionCaught));
                super.disconnect(ctx, promise);
            }

            @Override
            public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
                promise.addListener(failureListener(ctx, this.whenExceptionCaught));
                super.close(ctx, promise);
            }

            @Override
            public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
                promise.addListener(failureListener(ctx, this.whenExceptionCaught));
                super.deregister(ctx, promise);
            }

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                promise.addListener(cf -> {
                    if (!cf.isSuccess()) {
                        log.error("exception occur while writing, message is [" + msg + "]", cf.cause());
                        invokeAction(whenExceptionCaught, ctx, cf.cause());
                    }
                });
                super.write(ctx, msg, promise);
            }

            static ChannelFutureListener failureListener(ChannelHandlerContext ctx, ChannelExceptionAction whenExceptionCaught) {
                return cf -> {
                    if (!cf.isSuccess()) {
                        log.error(cf.cause().getMessage());
                        invokeAction(whenExceptionCaught, ctx, cf.cause());
                    }
                };
            }
        }
    }
}