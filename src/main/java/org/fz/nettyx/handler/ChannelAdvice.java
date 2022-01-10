package org.fz.nettyx.handler;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.channel.EventLoop;
import io.netty.handler.timeout.IdleStateHandler;
import java.net.SocketAddress;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.event.ActionableIdleStateHandler;
import org.fz.nettyx.event.ChannelEvents;
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
 * The type Channel advice.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 12 /24/2021 3:37 PM
 */
@SuppressWarnings("unchecked")
public class ChannelAdvice extends CombinedChannelDuplexHandler<InboundAdvice, OutboundAdvice> {

    private static final String
        READ_IDLE_HANDLER_NAME  = "_readIdle_",
        WRITE_IDLE_HANDLER_NAME = "_writeIdle_";

    /**
     * Instantiates a new Channel advice.
     *
     * @param inboundAdvice the inbound advice
     */
    public ChannelAdvice(InboundAdvice inboundAdvice) {
        super(inboundAdvice, OutboundAdvice.NONE);
    }

    /**
     * Instantiates a new Channel advice.
     *
     * @param outboundAdvice the outbound advice
     */
    public ChannelAdvice(OutboundAdvice outboundAdvice) {
        super(InboundAdvice.NONE, outboundAdvice);
    }

    /**
     * Instantiates a new Channel advice.
     *
     * @param inboundAdvice the inbound advice
     * @param outboundAdvice the outbound advice
     */
    public ChannelAdvice(InboundAdvice inboundAdvice, OutboundAdvice outboundAdvice) {
        super(inboundAdvice, outboundAdvice);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ChannelHandler last = ctx.pipeline().last();
        if (last instanceof ChannelAdvice) {
            super.handlerAdded(ctx);
        }
        else throw new UnsupportedOperationException("channel advice can only use as the last-handler of pipeline, keep it being the last-handler of pipeline");
    }

    /**
     * The type Inbound advice.
     */
    @Slf4j
    @Setter
    @Accessors(chain = true, fluent = true)
    public static class InboundAdvice extends ChannelInboundHandlerAdapter {

        /**
         * The None.
         */
        static final InboundAdvice NONE = new InboundAdvice(null);

        private Channel channel;

        private ChannelHandlerContextAction
            whenChannelRegister,
            whenChannelUnRegister,
            whenChannelActive,
            whenChannelInactive,
            whenWritabilityChanged,
            whenChannelReadComplete;

        private int readIdleSeconds;

        private ChannelReadAction      whenChannelRead;
        private ChannelExceptionAction whenExceptionCaught;

        public InboundAdvice(Channel channel) {
            this.channel = channel;
        }

        /**
         * When read idle inbound advice.
         *
         * @param idleSeconds the idle seconds
         * @param readIdleAct the read idle act
         * @return the inbound advice
         */
        public InboundAdvice whenReadIdle(int idleSeconds, ChannelHandlerContextAction readIdleAct) {
            this.readIdleSeconds = idleSeconds;

            ChannelPipeline pipeline = channel.pipeline();
            EventLoop eventLoop      = channel.eventLoop();

            IdleStateHandler readIdleHandler =
                new ActionableIdleStateHandler(this.readIdleSeconds, 0, 0).idleAction(readIdleAct);

            // read idle handler will always add pipeline first
            pipeline.addFirst(eventLoop, READ_IDLE_HANDLER_NAME, readIdleHandler);

            return this;
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel registered, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenChannelRegister, ctx);

            super.channelRegistered(ctx);
        }

        @Override
        public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel unregistered, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenChannelUnRegister, ctx);

            super.channelUnregistered(ctx);
        }

        /**
         * channel active action, will run channelActiveAction
         *
         * @param ctx ChannelHandlerContext
         * @throws Exception ex
         */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("channel active event triggered, address is [{}]", ctx.channel().remoteAddress());

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
            log.warn("channel in-active event triggered, address is [{}]", ctx.channel().remoteAddress());

            act(whenChannelInactive, ctx);

            super.channelInactive(ctx);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (log.isDebugEnabled()) {
                log.debug("channel read, remote-address is [{}], local-address is [{}], message is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress(), msg);
            }

            act(whenChannelRead, ctx, msg);

            super.channelRead(ctx, msg);
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel read complete, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenChannelReadComplete, ctx);

            super.channelReadComplete(ctx);
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
                log.warn("have been in read-idle state for [{}] seconds on [{}]", readIdleSeconds, ctx.channel().remoteAddress());

                act(this.findReadIdleAction(), ctx);
            }

            if (ChannelEvents.isWriteIdle(evt)) {
                log.warn("have been in write-idle state for [{}] seconds on [{}]", this.findWriteIdleSeconds(), ctx.channel().remoteAddress());

                act(this.findWriteIdleAction(), ctx);
            }

            super.userEventTriggered(ctx, evt);
        }

        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel writability changed, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress());

            act(whenWritabilityChanged, ctx);

            super.channelWritabilityChanged(ctx);
        }

        /**
         * Unified inbound exception handling, if the exception inherits from ChannelHandlerException, specific methods will be called to achieve
         *
         * @param ctx ChannelHandlerContext
         * @param cause Throwable
         */
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("channel handler exception occurred: ", cause);

            act(whenExceptionCaught, ctx, cause);

            ctx.channel().close();
        }

        private long findWriteIdleSeconds() {
            IdleStateHandler idleStateHandler = (IdleStateHandler) this.channel.pipeline().get(WRITE_IDLE_HANDLER_NAME);
            return idleStateHandler.getWriterIdleTimeInMillis() / 1000;
        }

        private <T extends ActionableIdleStateHandler> ChannelHandlerContextAction findWriteIdleAction() {
            T actionableIdleStateHandler = (T) this.channel.pipeline().get(WRITE_IDLE_HANDLER_NAME);
            return actionableIdleStateHandler == null ? null : actionableIdleStateHandler.idleAction();
        }

        private <T extends ActionableIdleStateHandler> ChannelHandlerContextAction findReadIdleAction() {
            T actionableIdleStateHandler = (T) this.channel.pipeline().get(READ_IDLE_HANDLER_NAME);
            return actionableIdleStateHandler == null ? null : actionableIdleStateHandler.idleAction();
        }
    }

    /**
     * The type Outbound advice.
     */
    @Slf4j
    @Setter
    @Accessors(chain = true, fluent = true)
    public static class OutboundAdvice extends ChannelOutboundHandlerAdapter {

        /**
         * The None.
         */
        static final OutboundAdvice NONE = new OutboundAdvice(null);

        private Channel channel;

        private ChannelBindAction whenBind;
        private ChannelConnectAction whenConnect;
        private ChannelPromiseAction whenDisconnect, whenClose, whenDeregister;
        private ChannelHandlerContextAction whenRead, whenFlush;
        private ChannelWriteAction whenWrite;

        private int writesIdleSeconds;

        public OutboundAdvice(Channel channel) {
            this.channel = channel;
        }

        /**
         * When write idle outbound advice.
         *
         * @param idleSeconds the idle seconds
         * @param writeIdleAct the write idle act
         * @return the outbound advice
         */
        public OutboundAdvice whenWriteIdle(int idleSeconds, ChannelHandlerContextAction writeIdleAct) {
            this.writesIdleSeconds = idleSeconds;

            ChannelPipeline pipeline = channel.pipeline();
            EventLoop eventLoop      = channel.eventLoop();

            IdleStateHandler writeIdleHandler =
                new ActionableIdleStateHandler(0, this.writesIdleSeconds, 0).idleAction(writeIdleAct);

            // write idle handler will always add pipeline last
            pipeline.addLast(eventLoop, WRITE_IDLE_HANDLER_NAME, writeIdleHandler);

            return this;
        }

        @Override
        public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            log.debug("channel binding, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), localAddress);

            act(whenBind, ctx, localAddress, promise);

            super.bind(ctx, localAddress, promise);
        }

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            log.debug("channel connecting, remote-address is [{}], local-address is [{}]", remoteAddress, localAddress);

            act(whenConnect, ctx, remoteAddress, localAddress, promise);

            super.connect(ctx, remoteAddress, localAddress, promise);
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            log.debug("channel disconnect, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenDisconnect, ctx, promise);

            super.disconnect(ctx, promise);
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            log.debug("channel close, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenClose, ctx, promise);

            super.close(ctx, promise);
        }

        @Override
        public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            log.debug("channel deregister, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenDeregister, ctx, promise);

            super.deregister(ctx, promise);
        }

        @Override
        public void read(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel read during writing, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenRead, ctx);

            super.read(ctx);
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (log.isDebugEnabled()) {
                log.debug("channel write, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());
            }

            act(whenWrite, ctx, msg, promise);

            super.write(ctx, msg, promise);
        }

        @Override
        public void flush(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel flush, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenFlush, ctx);

            super.flush(ctx);
        }
    }

    /**
     * Act.
     *
     * @param channelAction the channel action
     * @param ctx the ctx
     */
    static void act(ChannelHandlerContextAction channelAction, ChannelHandlerContext ctx) {
        if (channelAction != null) {
            channelAction.act(ctx);
        }
    }

    /**
     * Act.
     *
     * @param exceptionAction the exception action
     * @param ctx the ctx
     * @param throwable the throwable
     */
    static void act(ChannelExceptionAction exceptionAction, ChannelHandlerContext ctx, Throwable throwable) {
        if (exceptionAction != null) {
            exceptionAction.act(ctx, throwable);
        }
    }

    /**
     * Act.
     *
     * @param channelBindAction the channel bind action
     * @param ctx the ctx
     * @param localAddress the local address
     * @param promise the promise
     */
    static void act(ChannelBindAction channelBindAction, ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
        if (channelBindAction != null) {
            channelBindAction.act(ctx, localAddress, promise);
        }
    }

    /**
     * Act.
     *
     * @param channelConnectAction the channel connect action
     * @param ctx the ctx
     * @param remoteAddress the remote address
     * @param localAddress the local address
     * @param promise the promise
     */
    static void act(ChannelConnectAction channelConnectAction, ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
        ChannelPromise promise) {
        if (channelConnectAction != null) {
            channelConnectAction.act(ctx, remoteAddress, localAddress, promise);
        }
    }

    /**
     * Act.
     *
     * @param channelPromiseAction the channel promise action
     * @param ctx the ctx
     * @param promise the promise
     */
    static void act(ChannelPromiseAction channelPromiseAction, ChannelHandlerContext ctx, ChannelPromise promise) {
        if (channelPromiseAction != null) {
            channelPromiseAction.act(ctx, promise);
        }
    }

    /**
     * Act.
     *
     * @param channelWriteAction the channel write action
     * @param ctx the ctx
     * @param msg the msg
     * @param promise the promise
     */
    static void act(ChannelWriteAction channelWriteAction, ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (channelWriteAction != null) {
            channelWriteAction.act(ctx, msg, promise);
        }
    }

    /**
     * Act.
     *
     * @param channelReadAction the channel read action
     * @param ctx the ctx
     * @param msg the msg
     */
    static void act(ChannelReadAction channelReadAction, ChannelHandlerContext ctx, Object msg) {
        if (channelReadAction != null) {
            channelReadAction.act(ctx, msg);
        }
    }
}
