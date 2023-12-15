package org.fz.nettyx.handler.advice;

import static org.fz.nettyx.action.Actions.invokeAction;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelExceptionAction;
import org.fz.nettyx.action.ChannelHandlerContextAction;
import org.fz.nettyx.action.ChannelReadAction;
import org.fz.nettyx.handler.actionable.ActionableIdleStateHandler;
import org.fz.nettyx.handler.actionable.ActionableReadTimeoutHandler;


/**
 * The type Inbound advice.
 */
@Slf4j
@Setter
@Getter
@Accessors(chain = true, fluent = true)
public class InboundAdvice extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContextAction whenChannelRegister, whenChannelUnRegister, whenChannelActive, whenChannelInactive, whenWritabilityChanged, whenChannelReadComplete;
    private ChannelReadAction whenChannelRead;
    private ChannelExceptionAction whenExceptionCaught;
    private ActionableIdleStateHandler readIdleStateHandler;
    private ActionableReadTimeoutHandler readTimeoutHandler;

    /**
     * When read idle inbound advice.
     *
     * @param idleSeconds the idle seconds
     * @param readIdleAct the read idle action
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
     * @param timeoutAction the timeout action
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
}
