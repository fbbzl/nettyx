package org.fz.nettyx.handler;

import static org.fz.nettyx.handler.AdvisableChannelInitializer.READ_TIME_OUT;
import static org.fz.nettyx.handler.AdvisableChannelInitializer.WRITE_TIME_OUT;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import java.net.SocketAddress;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.exception.ClosingChannelException;
import org.fz.nettyx.function.ChannelExceptionAction;
import org.fz.nettyx.handler.actionable.ActionableReadTimeoutHandler;
import org.fz.nettyx.handler.actionable.ActionableWriteTimeoutHandler;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2/9/2022 1:18 PM
 */
public class ExceptionHandler extends CombinedChannelDuplexHandler<ExceptionHandler.InboundExceptionHandler, ExceptionHandler.OutboundExceptionHandler> {

    public ExceptionHandler(InboundExceptionHandler inboundHandler, OutboundExceptionHandler outboundHandler) {
        super(inboundHandler, outboundHandler);
    }

    @Slf4j
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InboundExceptionHandler extends ChannelInboundHandlerAdapter {

        @Override
        public boolean isSharable() {
            return true;
        }

        @Setter
        @Accessors(chain = true, fluent = true)
        private ChannelExceptionAction whenExceptionCaught;

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("channel handler exception occurred: ", cause);

            if (cause instanceof ReadTimeoutException)    { act(findReadTimeoutAction(ctx), ctx, cause);  }
            else
            if (cause instanceof WriteTimeoutException)   { act(findWriteTimeoutAction(ctx), ctx, cause); }
            else
            if (cause instanceof ClosingChannelException) { actAndClose(whenExceptionCaught, ctx, cause); }
            else act(whenExceptionCaught, ctx, cause);
        }

        private <T extends ActionableReadTimeoutHandler> ChannelExceptionAction findReadTimeoutAction(ChannelHandlerContext ctx) {
            T actionableHandler = this.findChannelHandler(ctx, READ_TIME_OUT);
            return actionableHandler == null ? null : actionableHandler.timeoutAction();
        }

        private <T extends ActionableWriteTimeoutHandler> ChannelExceptionAction findWriteTimeoutAction(ChannelHandlerContext ctx) {
            T actionableHandler = this.findChannelHandler(ctx, WRITE_TIME_OUT);
            return actionableHandler == null ? null : actionableHandler.timeoutAction();
        }

        @SuppressWarnings("unchecked")
        private <T extends ChannelHandler> T findChannelHandler(ChannelHandlerContext ctx, String handlerName) {
            return (T) ctx.pipeline().get(handlerName);
        }
    }

    @Slf4j
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OutboundExceptionHandler extends ChannelOutboundHandlerAdapter {

        @Override
        public boolean isSharable() {
            return true;
        }

        @Setter
        @Accessors(chain = true, fluent = true)
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
            promise.addListener(writeFailureListener(ctx, this.whenExceptionCaught, msg));
            super.write(ctx, msg, promise);
        }

        static ChannelFutureListener writeFailureListener(ChannelHandlerContext ctx, ChannelExceptionAction whenExceptionCaught, Object msg) {
            return cf -> {
                if (!cf.isSuccess()) {
                    log.error("exception occur while writing, message is [" + msg + "]", cf.cause());
                    act(whenExceptionCaught, ctx, cf.cause());
                }
            };
        }

        static ChannelFutureListener failureListener(ChannelHandlerContext ctx, ChannelExceptionAction whenExceptionCaught) {
            return cf -> {
                if (!cf.isSuccess()) {
                    log.error(cf.cause().getMessage());
                    act(whenExceptionCaught, ctx, cf.cause());
                }
            };
        }
    }

    static void act(ChannelExceptionAction exceptionAction, ChannelHandlerContext ctx, Throwable throwable) {
        if (exceptionAction != null) {
            exceptionAction.act(ctx, throwable);
        }
    }

    static void actAndClose(ChannelExceptionAction exceptionAction, ChannelHandlerContext ctx, Throwable cause) {
        act(exceptionAction, ctx, cause);
        ctx.channel().close();
    }
}
