package org.fz.nettyx.handler.advice;

import static org.fz.nettyx.action.Actions.invokeAction;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import java.net.SocketAddress;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.fz.nettyx.action.ChannelBindAction;
import org.fz.nettyx.action.ChannelConnectAction;
import org.fz.nettyx.action.ChannelExceptionAction;
import org.fz.nettyx.action.ChannelHandlerContextAction;
import org.fz.nettyx.action.ChannelPromiseAction;
import org.fz.nettyx.action.ChannelWriteAction;
import org.fz.nettyx.handler.actionable.ActionableIdleStateHandler;
import org.fz.nettyx.handler.actionable.ActionableWriteTimeoutHandler;


/**
 * The type Outbound advice.
 */
@Slf4j
@Setter
@Getter
@Accessors(chain = true, fluent = true)
public class OutboundAdvice extends ChannelOutboundHandlerAdapter {

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
}
