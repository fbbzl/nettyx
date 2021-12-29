package org.fz.nettyx.handler;


import static org.fz.nettyx.support.Logs.debug;
import static org.fz.nettyx.support.Logs.error;
import static org.fz.nettyx.support.Logs.info;
import static org.fz.nettyx.support.Logs.warn;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.channel.EventLoop;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.SocketAddress;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.event.ChannelEvents;
import org.fz.nettyx.event.ActionedIdleStateHandler;
import org.fz.nettyx.function.ChannelBindAction;
import org.fz.nettyx.function.ChannelConnectAction;
import org.fz.nettyx.function.ChannelExceptionAction;
import org.fz.nettyx.function.ChannelHandlerContextAction;
import org.fz.nettyx.function.ChannelPromiseAction;
import org.fz.nettyx.function.ChannelReadAction;
import org.fz.nettyx.function.ChannelWriteAction;
import org.fz.nettyx.handler.ChannelAdvice.InboundAdvice;
import org.fz.nettyx.handler.ChannelAdvice.OutboundAdvice;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 12/24/2021 3:37 PM
 */

public class ChannelAdvice extends CombinedChannelDuplexHandler<InboundAdvice, OutboundAdvice> {

    private static final String
        READ_IDLE_HANDLER_NAME  = "_readIdle_",
        WRITE_IDLE_HANDLER_NAME = "_writeIdle_";

    public ChannelAdvice(InboundAdvice inboundAdvice) {
        super(inboundAdvice, OutboundAdvice.NONE);
    }

    public ChannelAdvice(OutboundAdvice outboundAdvice) {
        super(InboundAdvice.NONE, outboundAdvice);
    }

    public ChannelAdvice(InboundAdvice inboundAdvice, OutboundAdvice outboundAdvice) {
        super(inboundAdvice, outboundAdvice);
    }

    @Slf4j
    @Setter
    @Accessors(chain = true, fluent = true)
    public static class InboundAdvice extends ChannelInboundHandlerAdapter {
        static final InboundAdvice NONE = new InboundAdvice(null);

        private Channel channel;

        private ChannelHandlerContextAction
            whenChannelRegister,
            whenChannelUnRegister,
            whenChannelActive,
            whenChannelInactive,
            whenWritabilityChanged,
            whenChannelReadComplete;

        @Setter(AccessLevel.NONE)
        private ChannelHandlerContextAction whenReadIdle;

        private int readIdleSeconds;

        private ChannelReadAction      whenChannelRead;
        private ChannelExceptionAction whenExceptionCaught;

        public InboundAdvice(Channel channel) {
            this.channel = channel;
        }

        public InboundAdvice whenReadIdle(int idleSeconds, ChannelHandlerContextAction act) {
            this.whenReadIdle = act;
            this.readIdleSeconds = idleSeconds;

            ChannelPipeline pipeline = channel.pipeline();
            EventLoop eventLoop      = channel.eventLoop();

            IdleStateHandler readIdleHandler  = new IdleStateHandler(this.readIdleSeconds, 0, 0),
                             writeIdleHandler = (IdleStateHandler) pipeline.get(WRITE_IDLE_HANDLER_NAME);

            // if writeIdleHandler configured, readIdleHandler will set before writeIdleHandler
            if (writeIdleHandler != null) {
                pipeline.addBefore(eventLoop, WRITE_IDLE_HANDLER_NAME, READ_IDLE_HANDLER_NAME, readIdleHandler);
            }
            else pipeline.addFirst(eventLoop, READ_IDLE_HANDLER_NAME, readIdleHandler);

            return this;
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            debug(log, "channel registered, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenChannelRegister, ctx);

            super.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            debug(log, "channel unregistered, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenChannelUnRegister, ctx);

            super.channelUnregistered(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            debug(log, "channel read, remote-address is [{}], local-address is [{}], message is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress(), msg);

            act(whenChannelRead, ctx, msg);

            super.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            debug(log, "channel read complete, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenChannelReadComplete, ctx);

            super.channelReadComplete(ctx);
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            debug(log, "channel writability changed, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress());

            act(whenWritabilityChanged, ctx);

            super.channelWritabilityChanged(ctx);
        }

        /**
         * channel active action, will run channelActiveAction
         *
         * @param ctx ChannelHandlerContext
         * @throws Exception ex
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            info(log, "channel active event triggered, address is [{}]", ctx.channel().remoteAddress());

            act(whenChannelActive, ctx);

            super.channelActive(ctx);
        }

        /**
         * channel in-active action, will run channelInactiveAction
         *
         * @param ctx ChannelHandlerContext
         * @throws Exception ex
         */
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            warn(log, "channel in-active event triggered, address is [{}]", ctx.channel().remoteAddress());

            act(whenChannelInactive, ctx);

            super.channelInactive(ctx);
        }

        /**
         * Unified inbound exception handling, if the exception inherits from ChannelHandlerException, specific methods will be called to achieve
         *
         * @param ctx ChannelHandlerContext
         * @param cause Throwable
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            error(log, "channel handler exception occurred: ", cause);

            act(whenExceptionCaught, ctx, cause);

            ctx.channel().close();
        }

        /**
         * channel event action execution
         *
         * @param ctx ChannelHandlerContext
         * @param evt channel event
         * @throws Exception ex
         */
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (ChannelEvents.isReadIdle(evt)) {
                warn(log, "have been in read-idle state for [{}] seconds on [{}]", readIdleSeconds, ctx.channel().remoteAddress());

                act(whenReadIdle, ctx);
            }

            if (ChannelEvents.isWriteIdle(evt)) {
                warn(log, "have been in write-idle state for [{}] seconds on [{}]", this.findWriteIdleSeconds(), ctx.channel().remoteAddress());

                act(this.findWriteIdleAction(), ctx);
            }

            super.userEventTriggered(ctx, evt);
        }

        private long findWriteIdleSeconds() {
            IdleStateHandler idleStateHandler = (IdleStateHandler) this.channel.pipeline().get(WRITE_IDLE_HANDLER_NAME);
            return idleStateHandler.getWriterIdleTimeInMillis() / 1000;
        }

        private ChannelHandlerContextAction findWriteIdleAction() {
            ActionedIdleStateHandler actionedIdleStateHandler = this.channel.pipeline().get(ActionedIdleStateHandler.class);
            return actionedIdleStateHandler == null ? null : actionedIdleStateHandler.action();
        }
    }

    @Slf4j
    @Setter
    @Accessors(chain = true, fluent = true)
    public static class OutboundAdvice extends ChannelOutboundHandlerAdapter {
        static final OutboundAdvice NONE = new OutboundAdvice(null);

        private Channel channel;

        private ChannelBindAction           whenBind;
        private ChannelConnectAction        whenConnect;
        private ChannelPromiseAction        whenDisconnect, whenClose, whenDeregister;
        private ChannelHandlerContextAction whenRead, whenFlush;
        private ChannelWriteAction          whenWrite;

        @Setter(AccessLevel.NONE)
        private ChannelHandlerContextAction whenWriteIdle;
        private int writesIdleSeconds;

        public OutboundAdvice(Channel channel) {
            this.channel = channel;
        }

        public OutboundAdvice whenWriteIdle(int idleSeconds, ChannelHandlerContextAction act) {
            this.whenWriteIdle = act;
            this.writesIdleSeconds = idleSeconds;

            ChannelPipeline pipeline = channel.pipeline();
            EventLoop eventLoop      = channel.eventLoop();

            IdleStateHandler writeIdleHandler = new ActionedIdleStateHandler(0, this.writesIdleSeconds, 0).action(this.whenWriteIdle),
                             readIdleHandler  = (IdleStateHandler) pipeline.get(READ_IDLE_HANDLER_NAME);

            // if readIdleHandler configured, writeIdleHandler will set after readIdleHandler
            if (readIdleHandler != null) {
                pipeline.addAfter(eventLoop, READ_IDLE_HANDLER_NAME, WRITE_IDLE_HANDLER_NAME, writeIdleHandler);
            }
            else pipeline.addFirst(eventLoop, WRITE_IDLE_HANDLER_NAME, writeIdleHandler);

            return this;
        }

        public void whenBind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            debug(log, "channel binding, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), localAddress);

            act(whenBind, ctx, localAddress, promise);

            super.bind(ctx, localAddress, promise);
        }

        public void whenConnect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            debug(log, "channel connecting, remote-address is [{}], local-address is [{}]", remoteAddress, localAddress);

            act(whenConnect, ctx, remoteAddress, localAddress, promise);

            super.connect(ctx, remoteAddress, localAddress, promise);
        }

        public void whenDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            debug(log, "channel disconnect, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenDisconnect, ctx, promise);

            super.disconnect(ctx, promise);
        }

        public void whenClose(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            debug(log, "channel close, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenClose, ctx, promise);

            super.close(ctx, promise);
        }

        public void whenDeregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            debug(log, "channel deregister, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenDeregister, ctx, promise);

            super.deregister(ctx, promise);
        }

        public void whenRead(ChannelHandlerContext ctx) throws Exception {
            debug(log, "channel read, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenRead, ctx);

            super.read(ctx);
        }

        public void whenWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            debug(log, "channel write, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenWrite, ctx, msg, promise);

            super.write(ctx, msg, promise);
        }

        public void whenFlush(ChannelHandlerContext ctx) throws Exception {
            debug(log, "channel flush, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenFlush, ctx);

            super.flush(ctx);
        }
    }

    static void act(ChannelHandlerContextAction channelAction, ChannelHandlerContext ctx) {
        if (channelAction != null) {
            channelAction.act(ctx);
        }
    }

    static void act(ChannelExceptionAction exceptionAction, ChannelHandlerContext ctx, Throwable throwable) {
        if (exceptionAction != null) {
            exceptionAction.act(ctx, throwable);
        }
    }

    static void act(ChannelBindAction channelBindAction, ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
        if (channelBindAction != null) {
            channelBindAction.act(ctx, localAddress, promise);
        }
    }

    static void act(ChannelConnectAction channelConnectAction, ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
        ChannelPromise promise) {
        if (channelConnectAction != null) {
            channelConnectAction.act(ctx, remoteAddress, localAddress, promise);
        }
    }

    static void act(ChannelPromiseAction channelPromiseAction, ChannelHandlerContext ctx, ChannelPromise promise) {
        if (channelPromiseAction != null) {
            channelPromiseAction.act(ctx, promise);
        }
    }

    static void act(ChannelWriteAction channelWriteAction, ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (channelWriteAction != null) {
            channelWriteAction.act(ctx, msg, promise);
        }
    }

    static void act(ChannelReadAction channelReadAction, ChannelHandlerContext ctx, Object msg) {
        if (channelReadAction != null) {
            channelReadAction.act(ctx, msg);
        }
    }
}
