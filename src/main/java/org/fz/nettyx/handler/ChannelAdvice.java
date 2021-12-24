package org.fz.nettyx.handler;


import static org.fz.nettyx.Logs.debug;
import static org.fz.nettyx.Logs.error;
import static org.fz.nettyx.Logs.info;
import static org.fz.nettyx.Logs.warn;

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
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.event.ChannelEvents;
import org.fz.nettyx.function.ChannelBindAction;
import org.fz.nettyx.function.ChannelConnectAction;
import org.fz.nettyx.function.ChannelExceptionAction;
import org.fz.nettyx.function.ChannelHandlerContextAction;
import org.fz.nettyx.function.ChannelPromiseAction;
import org.fz.nettyx.function.ChannelWriteAction;
import org.fz.nettyx.handler.ChannelAdvice.InboundAdvice;
import org.fz.nettyx.handler.ChannelAdvice.OutboundAdvice;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 12/24/2021 3:37 PM
 */

public class ChannelAdvice extends CombinedChannelDuplexHandler<InboundAdvice, OutboundAdvice> {

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
    @NoArgsConstructor
    @Accessors(chain = true, fluent = true)
    public static class InboundAdvice extends ChannelInboundHandlerAdapter {

        static final  InboundAdvice NONE = new InboundAdvice();

        private static final String
            READ_IDLE_NAME  = "_readIdle_",
            WRITE_IDLE_NAME = "_writeIdle_";

        private Channel channel;

        private ChannelHandlerContextAction
            whenChannelRegister,
            whenChannelUnRegister,
            whenChannelActive,
            whenChannelInactive,
            whenWritabilityChanged,
            whenChannelReadComplete,
            whenReadIdle,
            whenWriteIdle;

        private ChannelExceptionAction whenExceptionCaught;

        /**
         * used with {@link InboundAdvice#whenReadIdle}
         */
        private int readIdleSeconds;
        /**
         * used with {@link InboundAdvice#whenWriteIdle}
         */
        private int writesIdleSeconds;

        public InboundAdvice(Channel channel) {
            this.channel = channel;
        }

        public InboundAdvice whenReadIdle(int idleSeconds, ChannelHandlerContextAction act) {
            this.whenReadIdle    = act;
            this.readIdleSeconds = idleSeconds;

            ChannelPipeline pipeline  = channel.pipeline();
            EventLoop eventLoop = channel.eventLoop();

            IdleStateHandler readIdleHandler  = new IdleStateHandler(this.readIdleSeconds, 0, 0),
                writeIdleHandler = (IdleStateHandler) pipeline.get(WRITE_IDLE_NAME);

            // if writeIdleHandler configured, readIdleHandler will set before writeIdleHandler
            if (writeIdleHandler != null) {
                pipeline.addBefore(eventLoop, WRITE_IDLE_NAME, READ_IDLE_NAME, readIdleHandler);
            }
            else {
                pipeline.addFirst(eventLoop, READ_IDLE_NAME, readIdleHandler);
            }

            return this;
        }

        public InboundAdvice whenWriteIdle(int idleSeconds, ChannelHandlerContextAction act) {
            this.whenWriteIdle     = act;
            this.writesIdleSeconds = idleSeconds;

            ChannelPipeline pipeline  = channel.pipeline();
            EventLoop       eventLoop = channel.eventLoop();

            IdleStateHandler writeIdleHandler = new IdleStateHandler(0, this.writesIdleSeconds, 0),
                readIdleHandler  = (IdleStateHandler) pipeline.get(READ_IDLE_NAME);

            // if readIdleHandler configured, writeIdleHandler will set after readIdleHandler
            if (readIdleHandler != null) {
                pipeline.addAfter(eventLoop, READ_IDLE_NAME, WRITE_IDLE_NAME, writeIdleHandler);
            }
            else {
                pipeline.addFirst(eventLoop, WRITE_IDLE_NAME, writeIdleHandler);
            }

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

        /**
         * do not support channel read advice!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
         */

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            debug(log, "channel read complete, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenChannelReadComplete, ctx);

            super.channelReadComplete(ctx);
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            debug(log, "channel writability changed, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

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
                warn(log, "have been in write-idle state for [{}] seconds on [{}]", writesIdleSeconds, ctx.channel().remoteAddress());

                act(whenWriteIdle, ctx);
            }

            super.userEventTriggered(ctx, evt);
        }

    }

    /**
     * TODO add log
     */
    @Slf4j
    @Setter
    @NoArgsConstructor
    @Accessors(chain = true, fluent = true)
    public static class OutboundAdvice extends ChannelOutboundHandlerAdapter {
       static final  OutboundAdvice NONE = new OutboundAdvice();

        private ChannelBindAction           bind;
        private ChannelConnectAction        connect;
        private ChannelPromiseAction        disconnect, close, deregister;
        private ChannelHandlerContextAction read, flush;
        private ChannelWriteAction          write;

        @Override
        public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            debug(log, "channel binding, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), localAddress);

            act(bind, ctx, localAddress, promise);
            super.bind(ctx, localAddress, promise);
        }

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {

            act(connect, ctx, remoteAddress, localAddress, promise);
            super.connect(ctx, remoteAddress, localAddress, promise);
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {

            act(disconnect, ctx, promise);
            super.disconnect(ctx, promise);
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {

            act(close, ctx, promise);
            super.close(ctx, promise);
        }

        @Override
        public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {

            act(deregister, ctx, promise);
            super.deregister(ctx, promise);
        }

        @Override
        public void read(ChannelHandlerContext ctx) throws Exception {

            act(read, ctx);
            super.read(ctx);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {

            act(write, ctx, msg, promise);
            super.write(ctx, msg, promise);
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {

            act(flush, ctx);
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

    static void act(ChannelConnectAction channelConnectAction, ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
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

}
