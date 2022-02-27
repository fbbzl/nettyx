package org.fz.nettyx.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.net.SocketAddress;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.function.ChannelBindAction;
import org.fz.nettyx.function.ChannelConnectAction;
import org.fz.nettyx.function.ChannelExceptionAction;
import org.fz.nettyx.function.ChannelHandlerContextAction;
import org.fz.nettyx.function.ChannelPromiseAction;
import org.fz.nettyx.function.ChannelReadAction;
import org.fz.nettyx.function.ChannelWriteAction;
import org.fz.nettyx.handler.ExceptionHandler.InboundExceptionHandler;
import org.fz.nettyx.handler.ExceptionHandler.OutboundExceptionHandler;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2/10/2022 1:24 PM
 */

@Getter
public abstract class AdvisableChannelInitializer<C extends Channel> extends ChannelInitializer<C> {

    static final String
        INBOUND_ADVICE     = "$_inboundAdvice_$",
        OUTBOUND_ADVICE    = "$_outboundAdvice_$",
        READ_IDLE          = "$_readIdle_$",
        WRITE_IDLE         = "$_writeIdle_$",
        READ_TIME_OUT      = "$_readTimeout_$",
        WRITE_TIME_OUT     = "$_writeTimeout_$",
        INBOUND_EXCEPTION  = "$_inboundExceptionHandler_$",
        OUTBOUND_EXCEPTION = "$_outboundExceptionHandler_$";

    private final InboundAdvice  inboundAdvice;
    private final OutboundAdvice outboundAdvice;
    private final InboundExceptionHandler    inboundExceptionHandler  = new InboundExceptionHandler();
    private final OutboundExceptionHandler   outboundExceptionHandler = new OutboundExceptionHandler();
    private       ActionableIdleStateHandler readIdleStateHandler, writeIdleStateHandler;
    private       ReadTimeoutHandler         readTimeoutHandler;
    private       WriteTimeoutHandler        writeTimeoutHandler;

    protected AdvisableChannelInitializer(InboundAdvice inboundAdvice) {
        this(inboundAdvice, null);
    }

    protected AdvisableChannelInitializer(OutboundAdvice outboundAdvice) {
        this(null, outboundAdvice);
    }

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

    /**
     * keep channel handler in such order as default :
     *
     * 1. outboundExceptionHandler
     * 2. read-Idle
     * 3. read-timeout
     * 4. inboundAdvice
     *
     * 5. [business channel-handlers]
     *
     * 6. outboundAdvice
     * 7. write-timeout
     * 8. write-Idle
     * 9. inboundExceptionHandler
     *
     * if default order do not satisfy you, please override
     */
    @Override
    protected void initChannel(C channel) {
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

    static void addNonNullFirst(ChannelPipeline pipeline, String name, ChannelHandler channelHandler) {
        if (channelHandler != null) pipeline.addFirst(name, channelHandler);
    }
    static void addNonNullLast(ChannelPipeline pipeline, String name, ChannelHandler channelHandler) {
        if (channelHandler != null) pipeline.addLast(name, channelHandler);
    }
    static void addNonNullBefore(ChannelPipeline pipeline, String targetName, String name, ChannelHandler channelHandler) {
        if (channelHandler != null) pipeline.addBefore(targetName, name, channelHandler);
    }
    static void addNonNullAfter(ChannelPipeline pipeline, String targetName, String name, ChannelHandler channelHandler) {
        if (channelHandler != null) pipeline.addAfter(targetName, name, channelHandler);
    }

    @Slf4j
    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    public static class InboundAdvice extends ChannelInboundHandlerAdapter {

        private ChannelHandlerContextAction whenChannelRegister,
                                             whenChannelUnRegister,
                                             whenChannelActive,
                                             whenChannelInactive,
                                             whenWritabilityChanged,
                                             whenChannelReadComplete;
        private ChannelReadAction whenChannelRead;
        private ChannelExceptionAction whenExceptionCaught;
        private ActionableIdleStateHandler   readIdleStateHandler;
        private ActionableReadTimeoutHandler readTimeoutHandler;

        public final InboundAdvice whenReadIdle(int idleSeconds, ChannelHandlerContextAction readIdleAct) {
            this.readIdleStateHandler = ActionableIdleStateHandler.newReadIdleHandler(idleSeconds, readIdleAct);
            return this;
        }

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
        public final void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            log.debug("channel writability changed, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress());
            act(whenWritabilityChanged, ctx);
            super.channelWritabilityChanged(ctx);
        }
    }

    @Slf4j
    @Setter
    @Getter
    @Accessors(chain = true, fluent = true)
    public static class OutboundAdvice extends ChannelOutboundHandlerAdapter {

        private ChannelBindAction whenBind;
        private ChannelConnectAction whenConnect;
        private ChannelPromiseAction whenDisconnect, whenClose, whenDeregister;
        private ChannelHandlerContextAction   whenRead, whenFlush;
        private ChannelWriteAction whenWrite;
        private ChannelExceptionAction        whenExceptionCaught;
        private ActionableIdleStateHandler    writeIdleStateHandler;
        private ActionableWriteTimeoutHandler writeTimeoutHandler;

        public final OutboundAdvice whenWriteIdle(int idleSeconds, ChannelHandlerContextAction writeIdleAct) {
            this.writeIdleStateHandler = ActionableIdleStateHandler.newWriteIdleHandler(idleSeconds, writeIdleAct);
            return this;
        }

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
            log.debug("channel read during writing, remote-address is [{}], local-address is [{}]", ctx.channel().remoteAddress(),
                ctx.channel().localAddress());
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

    static void act(ChannelHandlerContextAction channelAction, ChannelHandlerContext ctx) {
        if (channelAction != null) {
            channelAction.act(ctx);
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
