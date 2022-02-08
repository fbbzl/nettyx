package org.fz.nettyx.handler;


import static org.fz.nettyx.handler.ChannelAdvice.READ_TIME_OUT;
import static org.fz.nettyx.handler.ChannelAdvice.WRITE_TIME_OUT;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.exception.ClosingChannelException;
import org.fz.nettyx.function.ChannelExceptionAction;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2/7/2022 1:09 PM
 */

@Slf4j
@SuppressWarnings("unchecked")
public class InboundExceptionHandler extends ChannelInboundHandlerAdapter  {
    
    private ChannelExceptionAction whenExceptionCaught;

    public InboundExceptionHandler whenExceptionCaught(ChannelExceptionAction whenExceptionCaught) {
        this.whenExceptionCaught = whenExceptionCaught;
        return this;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("channel handler exception occurred: ", cause);

        if(cause instanceof ReadTimeoutException)    { act(findReadTimeoutAction(ctx), ctx, cause);  }
        else
        if(cause instanceof WriteTimeoutException)   { act(findWriteTimeoutAction(ctx), ctx, cause); }
        else
        if(cause instanceof ClosingChannelException) { actAndClose(whenExceptionCaught, ctx, cause); }
        else act(whenExceptionCaught, ctx, cause);
    }

    @Override
    public boolean isSharable() {
        return true;
    }

    private <T extends ActionableReadTimeoutHandler> ChannelExceptionAction findReadTimeoutAction(ChannelHandlerContext ctx) {
        T actionableHandler = this.findChannelHandler(ctx, READ_TIME_OUT);
        return actionableHandler == null ? null : actionableHandler.timeoutAction();
    }

    private <T extends ActionableWriteTimeoutHandler> ChannelExceptionAction findWriteTimeoutAction(ChannelHandlerContext ctx) {
        T actionableHandler = this.findChannelHandler(ctx, WRITE_TIME_OUT);
        return actionableHandler == null ? null : actionableHandler.timeoutAction();
    }

    private <T extends ChannelHandler> T findChannelHandler(ChannelHandlerContext ctx, String handlerName) {
        return (T) ctx.pipeline().get(handlerName);
    }

    static void act(ChannelExceptionAction exceptionAction, ChannelHandlerContext ctx, Throwable throwable) {
        if (exceptionAction != null) exceptionAction.act(ctx, throwable);
    }

    static void actAndClose(ChannelExceptionAction exceptionAction, ChannelHandlerContext ctx, Throwable cause) {
        act(exceptionAction, ctx, cause);
        ctx.channel().close();
    }
}
