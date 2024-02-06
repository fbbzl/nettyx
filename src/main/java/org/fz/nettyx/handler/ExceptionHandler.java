package org.fz.nettyx.handler;

import io.netty.channel.*;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.handler.timeout.WriteTimeoutException;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.fz.nettyx.action.ChannelExceptionAction;
import org.fz.nettyx.exception.ClosingChannelException;
import org.fz.nettyx.handler.ExceptionHandler.SimpleInboundExceptionHandler;
import org.fz.nettyx.handler.ExceptionHandler.SimpleOutboundExceptionHandler;
import org.fz.nettyx.handler.actionable.ActionableReadTimeoutHandler;
import org.fz.nettyx.handler.actionable.ActionableWriteTimeoutHandler;

import java.net.SocketAddress;

import static org.fz.nettyx.handler.AdvisableChannelInitializer.READ_TIME_OUT;
import static org.fz.nettyx.handler.AdvisableChannelInitializer.WRITE_TIME_OUT;

/**
 * The type Exception handler.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2 /9/2022 1:18 PM
 */
public class ExceptionHandler extends CombinedChannelDuplexHandler<SimpleInboundExceptionHandler, SimpleOutboundExceptionHandler> {

    /**
     * Instantiates a new Exception handler.
     *
     * @param inboundHandler  the inbound handler
     * @param outboundHandler the outbound handler
     */
    public ExceptionHandler(SimpleInboundExceptionHandler inboundHandler, SimpleOutboundExceptionHandler outboundHandler) {
        super(inboundHandler, outboundHandler);
    }

    /**
     * The type Inbound exception handler.
     */
    @Setter
    @Slf4j
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleInboundExceptionHandler extends ChannelInboundHandlerAdapter {

        @Override
        public boolean isSharable() {
            return true;
        }

        @Accessors(chain = true, fluent = true)
        private ChannelExceptionAction whenExceptionCaught;

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("channel handler exception occurred: ", cause);

            if (cause instanceof ReadTimeoutException)    { invokeAction(findReadTimeoutAction(ctx), ctx, cause);  }
            else
            if (cause instanceof WriteTimeoutException)   { invokeAction(findWriteTimeoutAction(ctx), ctx, cause); }
            else
            if (cause instanceof ClosingChannelException) { invokeActionAndClose(whenExceptionCaught, ctx, cause); }
            else invokeAction(whenExceptionCaught, ctx, cause);
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

    /**
     * The type Outbound exception handler.
     */
    @Setter
    @Slf4j
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SimpleOutboundExceptionHandler extends ChannelOutboundHandlerAdapter {

        @Override
        public boolean isSharable() {
            return true;
        }

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

        /**
         * Write failure listener channel future listener.
         *
         * @param ctx                 the ctx
         * @param whenExceptionCaught the when exception caught
         * @param msg                 the msg
         * @return the channel future listener
         */
        static ChannelFutureListener writeFailureListener(ChannelHandlerContext ctx, ChannelExceptionAction whenExceptionCaught, Object msg) {
            return cf -> {
                if (!cf.isSuccess()) {
                    log.error("exception occur while writing, message is [" + msg + "]", cf.cause());
                    invokeAction(whenExceptionCaught, ctx, cf.cause());
                }
            };
        }

        /**
         * Failure listener channel future listener.
         *
         * @param ctx                 the ctx
         * @param whenExceptionCaught the when exception caught
         * @return the channel future listener
         */
        static ChannelFutureListener failureListener(ChannelHandlerContext ctx, ChannelExceptionAction whenExceptionCaught) {
            return cf -> {
                if (!cf.isSuccess()) {
                    log.error(cf.cause().getMessage());
                    invokeAction(whenExceptionCaught, ctx, cf.cause());
                }
            };
        }
    }

    /**
     * Act.
     *
     * @param exceptionAction the exception action
     * @param ctx             the ctx
     * @param throwable       the throwable
     */
    static void invokeAction(ChannelExceptionAction exceptionAction, ChannelHandlerContext ctx, Throwable throwable) {
        if (exceptionAction != null) {
            exceptionAction.act(ctx, throwable);
        }
    }

    /**
     * Act and close.
     *
     * @param exceptionAction the exception action
     * @param ctx             the ctx
     * @param cause           the cause
     */
    static void invokeActionAndClose(ChannelExceptionAction exceptionAction, ChannelHandlerContext ctx, Throwable cause) {
        invokeAction(exceptionAction, ctx, cause);
        ctx.channel().close();
    }
}
