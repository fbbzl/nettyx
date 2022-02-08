package org.fz.nettyx.handler;


import static java.util.Optional.ofNullable;

import com.gehc.nettyx.event.ChannelEvents;
import com.gehc.nettyx.function.ChannelBindAction;
import com.gehc.nettyx.function.ChannelConnectAction;
import com.gehc.nettyx.function.ChannelExceptionAction;
import com.gehc.nettyx.function.ChannelHandlerContextAction;
import com.gehc.nettyx.function.ChannelPromiseAction;
import com.gehc.nettyx.function.ChannelReadAction;
import com.gehc.nettyx.function.ChannelWriteAction;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.net.SocketAddress;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Channel Event advice, will execute the Action assigned
 *
 * @author fengbinbin
 * @since 2022-01-27 18:07
 */
@AllArgsConstructor
@SuppressWarnings("unchecked")
public class ChannelAdvice extends ChannelInboundHandlerAdapter {

    static final String
        INBOUND_ADVICE    = "$_inboundAdvice_$",
        OUTBOUND_ADVICE   = "$_outboundAdvice_$",
        READ_IDLE         = "$_readIdle_$",
        WRITE_IDLE        = "$_writeIdle_$",
        READ_TIME_OUT     = "$_readTimeout_$",
        WRITE_TIME_OUT    = "$_writeTimeout_$",
        INBOUND_EXCEPTION = "$_inboundExceptionHandler_$";

    private InboundAdvice inbound;
    private OutboundAdvice outbound;

    public ChannelAdvice(InboundAdvice inboundAdvice) {
        this(inboundAdvice, null);
    }

    public ChannelAdvice(OutboundAdvice outboundAdvice) {
        this(null, outboundAdvice);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ChannelPipeline pipeline = ctx.pipeline();

        this.checkInterceptor(pipeline);

        this.checkPosition(pipeline);

        this.polarize(pipeline);

        pipeline.remove(ChannelAdvice.class);
    }

    /**
     * keep channel handler in such order as default:
     * 0. read Idle
     * 1. write Idle
     * 2. inboundAdvice
     * 3. [business channel-handler...]
     * 4. outboundAdvice
     * 5. read-timeout
     * 6. write-timeout
     * 7. inboundExceptionHandler
     *
     * if default order do not satisfy you, please override
     */
    protected void polarize(ChannelPipeline pipeline) {
        ChannelHandler
            readTimeout  = pipeline.get(READ_TIME_OUT),
            writeTimeout = pipeline.get(WRITE_TIME_OUT),
            readIdle     = pipeline.get(READ_IDLE),
            writeIdle    = pipeline.get(WRITE_IDLE),
            inboundExceptionHandler = ofNullable(pipeline.get(INBOUND_EXCEPTION)).orElseGet(InboundExceptionHandler::new);

        // seed position
        if (inbound   != null) { pipeline.addFirst(INBOUND_ADVICE, inbound);  }
        if (outbound  != null) { pipeline.addLast(OUTBOUND_ADVICE, outbound); }

        // witch depends on inbound advice
        if (readIdle  != null) { pipeline.remove(readIdle);  pipeline.addBefore(INBOUND_ADVICE, READ_IDLE,  readIdle);  }
        if (writeIdle != null) { pipeline.remove(writeIdle); pipeline.addBefore(INBOUND_ADVICE, WRITE_IDLE, writeIdle); }

        pipeline.addLast(INBOUND_EXCEPTION, inboundExceptionHandler);

        // witch depends on inbound exception handler
        if (readTimeout  != null) { pipeline.remove(readTimeout);  pipeline.addBefore(INBOUND_EXCEPTION, READ_TIME_OUT,  readTimeout);  }
        if (writeTimeout != null) { pipeline.remove(writeTimeout); pipeline.addBefore(INBOUND_EXCEPTION, WRITE_TIME_OUT, writeTimeout); }
    }

    private void checkPosition(ChannelPipeline pipeline) {
        if (pipeline.names().size() > 2 && pipeline.first() instanceof ChannelAdvice) {
            throw new UnsupportedOperationException("do not make channel-advice being the first in the pipeline");
        }
    }

    private void checkInterceptor(ChannelPipeline pipeline) {
        ChannelInterceptor<?> channelInterceptor = pipeline.get(ChannelInterceptor.class);

        if (channelInterceptor != null) {
            throw new UnsupportedOperationException("channel-advice can not use with any channel-interceptor");
        }
    }

    @Slf4j
    @Setter
    @Accessors(chain = true, fluent = true)
    public static class InboundAdvice extends ChannelInboundHandlerAdapter {

        private Channel channel;

        private ChannelHandlerContextAction
            whenChannelRegister,
            whenChannelUnRegister,
            whenChannelActive,
            whenChannelInactive,
            whenWritabilityChanged,
            whenChannelReadComplete;

        private ChannelReadAction whenChannelRead;

        private int readIdleSeconds;
        private int readTimeoutSeconds;

        public InboundAdvice(Channel channel) {
            this.channel = channel;
        }

        public final InboundAdvice whenReadIdle(int idleSeconds, ChannelHandlerContextAction readIdleAct) {
            this.readIdleSeconds = idleSeconds;

            ChannelPipeline pipeline = channel.pipeline();

            IdleStateHandler readIdleHandler  =
                new ActionableIdleStateHandler(this.readIdleSeconds, 0, 0).idleAction(readIdleAct);

            uniqueCheck(pipeline, READ_IDLE);
            pipeline.addFirst(READ_IDLE, readIdleHandler);

            return this;
        }

        public final InboundAdvice whenReadTimeout(int timeoutSeconds, ChannelExceptionAction timeoutAction) {
            this.readTimeoutSeconds = timeoutSeconds;

            ChannelPipeline pipeline = channel.pipeline();

            ReadTimeoutHandler readTimeoutHandler =
                new ActionableReadTimeoutHandler(this.readTimeoutSeconds).timeoutAction(timeoutAction);

            uniqueCheck(pipeline, READ_TIME_OUT);
            pipeline.addFirst(READ_TIME_OUT, readTimeoutHandler);

            return this;
        }

        public final InboundAdvice whenExceptionCaught(ChannelExceptionAction exceptionAction) {
            ChannelPipeline pipeline = channel.pipeline();

            InboundExceptionHandler inboundExceptionHandler = pipeline.get(InboundExceptionHandler.class);

            if (inboundExceptionHandler == null) {
                this.channel.pipeline().addLast(new InboundExceptionHandler().whenExceptionCaught(exceptionAction));
            }
            else inboundExceptionHandler.whenExceptionCaught(exceptionAction);

            return this;
        }

        @Override
        public boolean isSharable() {
            return true;
        }

        @Override
        public final void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel registered, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenChannelRegister, ctx);

            super.channelRegistered(ctx);
        }

        @Override
        public final void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel unregistered, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenChannelUnRegister, ctx);

            super.channelUnregistered(ctx);
        }

        @Override
        public final void channelActive(ChannelHandlerContext ctx) throws Exception {
            log.info("channel active event triggered, address is [{}]", ctx.channel().remoteAddress());

            act(whenChannelActive, ctx);

            super.channelActive(ctx);
        }

        @Override
        public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
            log.warn("channel in-active event triggered, address is [{}]", ctx.channel().remoteAddress());

            act(whenChannelInactive, ctx);

            super.channelInactive(ctx);
        }

        @Override
        public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            log.debug("channel read, remote-address is [{}], local-address is [{}], message is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress(), msg);

            act(whenChannelRead, ctx, msg);

            super.channelRead(ctx, msg);
        }

        @Override
        public final void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel read complete, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenChannelReadComplete, ctx);

            super.channelReadComplete(ctx);
        }

        @Override
        public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
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
        public final void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel writability changed, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress());

            act(whenWritabilityChanged, ctx);

            super.channelWritabilityChanged(ctx);
        }

        private long findWriteIdleSeconds() {
            IdleStateHandler idleStateHandler = (IdleStateHandler) this.channel.pipeline().get(WRITE_IDLE);
            return idleStateHandler.getWriterIdleTimeInMillis() / 1000;
        }

        private <T extends ActionableIdleStateHandler> ChannelHandlerContextAction findWriteIdleAction() {
            T actionableHandler = this.findChannelHandler(WRITE_IDLE);
            return actionableHandler == null ? null : actionableHandler.idleAction();
        }

        private <T extends ActionableIdleStateHandler> ChannelHandlerContextAction findReadIdleAction() {
            T actionableHandler = this.findChannelHandler(READ_IDLE);
            return actionableHandler == null ? null : actionableHandler.idleAction();
        }

        private <T extends ChannelHandler> T findChannelHandler(String handlerName) {
            return (T) this.channel.pipeline().get(handlerName);
        }
    }

    @Slf4j
    @Setter
    @Accessors(chain = true, fluent = true)
    public static class OutboundAdvice extends ChannelOutboundHandlerAdapter {

        private Channel channel;

        private ChannelBindAction whenBind;
        private ChannelConnectAction whenConnect;
        private ChannelPromiseAction whenDisconnect, whenClose, whenDeregister;
        private ChannelHandlerContextAction whenRead, whenFlush;
        private ChannelWriteAction whenWrite;

        private int writesIdleSeconds;
        private int writeTimeoutSeconds;

        public OutboundAdvice(Channel channel) {
            this.channel = channel;
        }

        public final OutboundAdvice whenWriteIdle(int idleSeconds, ChannelHandlerContextAction writeIdleAct) {
            this.writesIdleSeconds = idleSeconds;

            ChannelPipeline pipeline = channel.pipeline();

            IdleStateHandler writeIdleHandler =
                new ActionableIdleStateHandler(0, this.writesIdleSeconds, 0).idleAction(writeIdleAct);

            uniqueCheck(pipeline, WRITE_IDLE);
            pipeline.addFirst(WRITE_IDLE, writeIdleHandler);

            return this;
        }

        public final OutboundAdvice whenWriteTimeout(int timeoutSeconds, ChannelExceptionAction timeoutAction) {
            this.writeTimeoutSeconds = timeoutSeconds;

            ChannelPipeline pipeline = channel.pipeline();

            WriteTimeoutHandler writeTimeoutHandler =
                new ActionableWriteTimeoutHandler(this.writeTimeoutSeconds).timeoutAction(timeoutAction);

            uniqueCheck(pipeline, WRITE_TIME_OUT);
            pipeline.addFirst(WRITE_TIME_OUT, writeTimeoutHandler);

            return this;
        }

        @Override
        public boolean isSharable() {
            return true;
        }

        @Override
        public final void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            log.debug("channel binding, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), localAddress);

            act(whenBind, ctx, localAddress, promise);

            super.bind(ctx, localAddress, promise);
        }

        @Override
        public final void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            log.debug("channel connecting, remote-address is [{}], local-address is [{}]", remoteAddress, localAddress);

            act(whenConnect, ctx, remoteAddress, localAddress, promise);

            super.connect(ctx, remoteAddress, localAddress, promise);
        }

        @Override
        public final void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            log.debug("channel disconnect, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenDisconnect, ctx, promise);

            super.disconnect(ctx, promise);
        }

        @Override
        public final void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            log.debug("channel close, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenClose, ctx, promise);

            super.close(ctx, promise);
        }

        @Override
        public final void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            log.debug("channel deregister, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenDeregister, ctx, promise);

            super.deregister(ctx, promise);
        }

        @Override
        public final void read(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel read during writing, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenRead, ctx);

            super.read(ctx);
        }

        @Override
        public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            log.debug("channel write, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenWrite, ctx, msg, promise);

            super.write(ctx, msg, promise);
        }

        @Override
        public final void flush(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel flush, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());

            act(whenFlush, ctx);

            super.flush(ctx);
        }

    }

    static void uniqueCheck(ChannelPipeline pipeline, String handlerName) {
        final ChannelHandler configured = pipeline.get(handlerName);

        if (configured != null) throw new UnsupportedOperationException("handler named [" + handlerName + "] already defined in pipeline [" + pipeline + "]");
    }

    static void act(ChannelHandlerContextAction channelAction, ChannelHandlerContext ctx) {
        if (channelAction != null) channelAction.act(ctx);
    }

    static void act(ChannelBindAction channelBindAction, ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
        if (channelBindAction != null) channelBindAction.act(ctx, localAddress, promise);
    }

    static void act(ChannelConnectAction channelConnectAction, ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
        ChannelPromise promise) {
        if (channelConnectAction != null) channelConnectAction.act(ctx, remoteAddress, localAddress, promise);
    }

    static void act(ChannelPromiseAction channelPromiseAction, ChannelHandlerContext ctx, ChannelPromise promise) {
        if (channelPromiseAction != null) channelPromiseAction.act(ctx, promise);
    }

    static void act(ChannelWriteAction channelWriteAction, ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (channelWriteAction != null) channelWriteAction.act(ctx, msg, promise);
    }

    static void act(ChannelReadAction channelReadAction, ChannelHandlerContext ctx, Object msg) {
        if (channelReadAction != null) channelReadAction.act(ctx, msg);
    }

}
