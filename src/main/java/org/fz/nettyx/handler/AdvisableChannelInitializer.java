package org.fz.nettyx.handler;

import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.function.*;
import org.fz.nettyx.handler.ExceptionHandler.InboundExceptionHandler;
import org.fz.nettyx.handler.ExceptionHandler.OutboundExceptionHandler;
import org.fz.nettyx.handler.actionable.ActionableIdleStateHandler;
import org.fz.nettyx.handler.actionable.ActionableReadTimeoutHandler;
import org.fz.nettyx.handler.actionable.ActionableWriteTimeoutHandler;

import java.net.SocketAddress;

/**
 * The type Advisable channel initializer.
 *
 * @param <C> the type parameter
 * @author fengbinbin
 * @version 1.0
 * @since 2 /10/2022 1:24 PM
 */
@Getter
public abstract class AdvisableChannelInitializer<C extends Channel> extends ChannelInitializer<C> {

    /**
     * The Inbound advice.
     */
    static final String
        INBOUND_ADVICE     = "$_inboundAdvice_$",
    /**
     * The Outbound advice.
     */
    OUTBOUND_ADVICE    = "$_outboundAdvice_$",
    /**
     * The Read idle.
     */
    READ_IDLE          = "$_readIdle_$",
    /**
     * The Write idle.
     */
    WRITE_IDLE         = "$_writeIdle_$",
    /**
     * The Read time out.
     */
    READ_TIME_OUT      = "$_readTimeout_$",
    /**
     * The Write time out.
     */
    WRITE_TIME_OUT     = "$_writeTimeout_$",
    /**
     * The Inbound exception.
     */
    INBOUND_EXCEPTION  = "$_inboundExceptionHandler_$",
    /**
     * The Outbound exception.
     */
    OUTBOUND_EXCEPTION = "$_outboundExceptionHandler_$";

    private final InboundAdvice  inboundAdvice;
    private final OutboundAdvice outboundAdvice;
    private final InboundExceptionHandler    inboundExceptionHandler  = new InboundExceptionHandler();
    private final OutboundExceptionHandler   outboundExceptionHandler = new OutboundExceptionHandler();
    private ActionableIdleStateHandler readIdleStateHandler, writeIdleStateHandler;
    private       ReadTimeoutHandler         readTimeoutHandler;
    private       WriteTimeoutHandler        writeTimeoutHandler;

    /**
     * Instantiates a new Advisable channel initializer.
     *
     * @param inboundAdvice the inbound advice
     */
    protected AdvisableChannelInitializer(InboundAdvice inboundAdvice) {
        this(inboundAdvice, null);
    }

    /**
     * Instantiates a new Advisable channel initializer.
     *
     * @param outboundAdvice the outbound advice
     */
    protected AdvisableChannelInitializer(OutboundAdvice outboundAdvice) {
        this(null, outboundAdvice);
    }

    /**
     * Instantiates a new Advisable channel initializer.
     *
     * @param inboundAdvice  the inbound advice
     * @param outboundAdvice the outbound advice
     */
    protected AdvisableChannelInitializer(InboundAdvice inboundAdvice, OutboundAdvice outboundAdvice) {
        this.inboundAdvice  = inboundAdvice;
        this.outboundAdvice = outboundAdvice;

        this.initInternalHandlers();
    }

    private void initInternalHandlers() {
        // init create after null check
        if (inboundAdvice != null) {
            this.readIdleStateHandler = inboundAdvice.readIdleStateHandler();
            this.readTimeoutHandler   = inboundAdvice.readTimeoutHandler();
            this.inboundExceptionHandler.whenExceptionCaught(inboundAdvice.whenExceptionCaught());
        }

        if (outboundAdvice != null) {
            this.writeIdleStateHandler = outboundAdvice.writeIdleStateHandler();
            this.writeTimeoutHandler   = outboundAdvice.writeTimeoutHandler();
            this.outboundExceptionHandler.whenExceptionCaught(outboundAdvice.whenExceptionCaught());
        }
    }

    /**
     * add custom handlers
     *
     * @param channel current channel
     */
    protected abstract void addHandlers(C channel);

    @Override
    protected void initChannel(C channel) {
        // if default channel-handler order do not satisfy you, please override this method
        this.addDefaultOrderedHandlers(channel);
    }

    /**
     * keep channel handler in such order as default :
     * <p>
     * 1. outboundExceptionHandler
     * 2. read-Idle
     * 3. read-timeout
     * 4. inboundAdvice
     * <p>
     * 5. [business channel-handlers]
     * <p>
     * 6. outboundAdvice
     * 7. write-timeout
     * 8. write-Idle
     * 9. inboundExceptionHandler
     *
     * @param channel the channel
     */
    void addDefaultOrderedHandlers(C channel) {
        ChannelPipeline pipeline = channel.pipeline();

        addNonNullLast(pipeline, OUTBOUND_EXCEPTION, outboundExceptionHandler);
        addNonNullLast(pipeline, READ_IDLE,          readIdleStateHandler);
        addNonNullLast(pipeline, READ_TIME_OUT,      readTimeoutHandler);
        addNonNullLast(pipeline, INBOUND_ADVICE,     inboundAdvice);
        this.addHandlers(channel);
        addNonNullLast(pipeline, OUTBOUND_ADVICE,    outboundAdvice);
        addNonNullLast(pipeline, WRITE_TIME_OUT,     writeTimeoutHandler);
        addNonNullLast(pipeline, WRITE_IDLE,         writeIdleStateHandler);
        addNonNullLast(pipeline, INBOUND_EXCEPTION,  inboundExceptionHandler);
    }

    /**
     * Add non null first.
     *
     * @param pipeline       the pipeline
     * @param name           the name
     * @param channelHandler the channel handler
     */
    static void addNonNullFirst(ChannelPipeline pipeline, String name, ChannelHandler channelHandler) {
        if (channelHandler != null) pipeline.addFirst(name, channelHandler);
    }

    /**
     * Add non null last.
     *
     * @param pipeline       the pipeline
     * @param name           the name
     * @param channelHandler the channel handler
     */
    static void addNonNullLast(ChannelPipeline pipeline, String name, ChannelHandler channelHandler) {
        if (channelHandler != null) pipeline.addLast(name, channelHandler);
    }

    /**
     * Add non null before.
     *
     * @param pipeline       the pipeline
     * @param targetName     the target name
     * @param name           the name
     * @param channelHandler the channel handler
     */
    static void addNonNullBefore(ChannelPipeline pipeline, String targetName, String name, ChannelHandler channelHandler) {
        if (channelHandler != null) pipeline.addBefore(targetName, name, channelHandler);
    }

    /**
     * Add non null after.
     *
     * @param pipeline       the pipeline
     * @param targetName     the target name
     * @param name           the name
     * @param channelHandler the channel handler
     */
    static void addNonNullAfter(ChannelPipeline pipeline, String targetName, String name, ChannelHandler channelHandler) {
        if (channelHandler != null) pipeline.addAfter(targetName, name, channelHandler);
    }

    /**
     * The type Inbound advice.
     */
    @Slf4j
    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    public static class InboundAdvice extends ChannelInboundHandlerAdapter {

        private ChannelHandlerContextAction  whenChannelRegister,
                                             whenChannelUnRegister,
                                             whenChannelActive,
                                             whenChannelInactive,
                                             whenWritabilityChanged,
                                             whenChannelReadComplete;
        private ChannelReadAction            whenChannelRead;
        private ChannelExceptionAction       whenExceptionCaught;
        private ActionableIdleStateHandler   readIdleStateHandler;
        private ActionableReadTimeoutHandler readTimeoutHandler;

        /**
         * When read idle inbound advice.
         *
         * @param idleSeconds the idle seconds
         * @param readIdleAct the read idle invokeAction
         * @return the inbound advice
         */
        public final InboundAdvice whenReadIdle(int idleSeconds, ChannelHandlerContextAction readIdleAct) {
            this.readIdleStateHandler = ActionableIdleStateHandler.newReadIdleHandler(idleSeconds, readIdleAct);
            return this;
        }

        /**
         * When read timeout inbound advice.
         *
         * @param timeoutSeconds the timeout seconds
         * @param timeoutAction  the timeout action
         * @return the inbound advice
         */
        public final InboundAdvice whenReadTimeout(int timeoutSeconds, ChannelExceptionAction timeoutAction) {
            this.readTimeoutHandler = new ActionableReadTimeoutHandler(timeoutSeconds, timeoutAction);
            return this;
        }

        @Override
        public boolean isSharable() {
            return true;
        }

        @Override
        public final void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel registered, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());
            invokeAction(whenChannelRegister, ctx);
            super.channelRegistered(ctx);
        }

        @Override
        public final void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel unregistered, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());
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
            log.debug("channel read, remote-address is [{}], local-address is [{}], message is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress(), msg);
            invokeAction(whenChannelRead, ctx, msg);
            super.channelRead(ctx, msg);
        }

        @Override
        public final void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel read complete, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());
            invokeAction(whenChannelReadComplete, ctx);
            super.channelReadComplete(ctx);
        }

        @Override
        public final void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel writability changed, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());
            invokeAction(whenWritabilityChanged, ctx);
            super.channelWritabilityChanged(ctx);
        }
    }

    /**
     * The type Outbound advice.
     */
    @Slf4j
    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    public static class OutboundAdvice extends ChannelOutboundHandlerAdapter {

        private ChannelBindAction             whenBind;
        private ChannelConnectAction          whenConnect;
        private ChannelPromiseAction          whenDisconnect, whenClose, whenDeregister;
        private ChannelHandlerContextAction   whenRead, whenFlush;
        private ChannelWriteAction            whenWrite;
        private ChannelExceptionAction        whenExceptionCaught;
        private ActionableIdleStateHandler    writeIdleStateHandler;
        private ActionableWriteTimeoutHandler writeTimeoutHandler;

        /**
         * When write idle outbound advice.
         *
         * @param idleSeconds  the idle seconds
         * @param writeIdleAct the write idle invokeAction
         * @return the outbound advice
         */
        public final OutboundAdvice whenWriteIdle(int idleSeconds, ChannelHandlerContextAction writeIdleAct) {
            this.writeIdleStateHandler = ActionableIdleStateHandler.newWriteIdleHandler(idleSeconds, writeIdleAct);
            return this;
        }

        /**
         * When write timeout outbound advice.
         *
         * @param timeoutSeconds the timeout seconds
         * @param timeoutAction  the timeout action
         * @return the outbound advice
         */
        public final OutboundAdvice whenWriteTimeout(int timeoutSeconds, ChannelExceptionAction timeoutAction) {
            this.writeTimeoutHandler = new ActionableWriteTimeoutHandler(timeoutSeconds, timeoutAction);
            return this;
        }

        @Override
        public boolean isSharable() {
            return true;
        }

        @Override
        public final void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            log.debug("channel binding, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), localAddress);
            invokeAction(whenBind, ctx, localAddress, promise);
            super.bind(ctx, localAddress, promise);
        }

        @Override
        public final void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            log.debug("channel connecting, remote-address is [{}], local-address is [{}]", remoteAddress, localAddress);
            invokeAction(whenConnect, ctx, remoteAddress, localAddress, promise);
            super.connect(ctx, remoteAddress, localAddress, promise);
        }

        @Override
        public final void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            log.debug("channel disconnect, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());
            invokeAction(whenDisconnect, ctx, promise);
            super.disconnect(ctx, promise);
        }

        @Override
        public final void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            log.debug("channel close, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());
            invokeAction(whenClose, ctx, promise);
            super.close(ctx, promise);
        }

        @Override
        public final void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            log.debug("channel deregister, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());
            invokeAction(whenDeregister, ctx, promise);
            super.deregister(ctx, promise);
        }

        @Override
        public final void read(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel read during writing, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress());
            invokeAction(whenRead, ctx);
            super.read(ctx);
        }

        @Override
        public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            log.debug("channel write, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());
            invokeAction(whenWrite, ctx, msg, promise);
            super.write(ctx, msg, promise);
        }

        @Override
        public final void flush(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel flush, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(), ctx.channel().localAddress());
            invokeAction(whenFlush, ctx);
            super.flush(ctx);
        }
    }

    /**
     * Act.
     *
     * @param channelAction the channel action
     * @param ctx           the ctx
     */
    static void invokeAction(ChannelHandlerContextAction channelAction, ChannelHandlerContext ctx) {
        if (channelAction != null) {
            channelAction.act(ctx);
        }
    }

    /**
     * Act.
     *
     * @param channelBindAction the channel bind action
     * @param ctx               the ctx
     * @param localAddress      the local address
     * @param promise           the promise
     */
    static void invokeAction(ChannelBindAction channelBindAction, ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
        if (channelBindAction != null) {
            channelBindAction.act(ctx, localAddress, promise);
        }
    }

    /**
     * Act.
     *
     * @param channelConnectAction the channel connect action
     * @param ctx                  the ctx
     * @param remoteAddress        the remote address
     * @param localAddress         the local address
     * @param promise              the promise
     */
    static void invokeAction(ChannelConnectAction channelConnectAction, ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
                             ChannelPromise promise) {
        if (channelConnectAction != null) {
            channelConnectAction.act(ctx, remoteAddress, localAddress, promise);
        }
    }

    /**
     * Act.
     *
     * @param channelPromiseAction the channel promise action
     * @param ctx                  the ctx
     * @param promise              the promise
     */
    static void invokeAction(ChannelPromiseAction channelPromiseAction, ChannelHandlerContext ctx, ChannelPromise promise) {
        if (channelPromiseAction != null) {
            channelPromiseAction.act(ctx, promise);
        }
    }

    /**
     * Act.
     *
     * @param channelWriteAction the channel write action
     * @param ctx                the ctx
     * @param msg                the msg
     * @param promise            the promise
     */
    static void invokeAction(ChannelWriteAction channelWriteAction, ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (channelWriteAction != null) {
            channelWriteAction.act(ctx, msg, promise);
        }
    }

    /**
     * Act.
     *
     * @param channelReadAction the channel read action
     * @param ctx               the ctx
     * @param msg               the msg
     */
    static void invokeAction(ChannelReadAction channelReadAction, ChannelHandlerContext ctx, Object msg) {
        if (channelReadAction != null) {
            channelReadAction.act(ctx, msg);
        }
    }
}
