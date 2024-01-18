package org.fz.nettyx.handler.interceptor;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;

import java.net.SocketAddress;

/**
 * channel interceptor
 * All methods will NOT be intercepted by default, which method should be overridden for which event you want to intercept
 * Not Thread Safe!!!
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /4/25 15:46
 */
public abstract class ChannelInterceptor extends ChannelHandlerAdapter {

    /**
     * interceptor state
     * true : Means this interceptor no longer intercepts any events
     * false: Means the specified channel-event will be intercepted
     */
    private boolean state = false;

    public boolean isFreed() {
        return state;
    }

    public boolean isNotFreed() {
        return !state;
    }

    /**
     * after use this method, this ChannelInboundHandler will be freed which will no longer participate in message parsing
     *
     * @see ChannelInterceptor#reset() ChannelInterceptor#reset()ChannelInterceptor#reset()use this to recover this handler
     */
    public void free() {
        this.state = true;
    }

    /**
     * after using this method, this ChannelInboundHandler will re participate in message parsing
     *
     * @see ChannelInterceptor#free() ChannelInterceptor#free()ChannelInterceptor#free()use method to free
     */
    public void reset() {
        this.state = false;
    }


    @SuppressWarnings("unchecked")
    public static class InboundInterceptor<M> extends ChannelInterceptor implements ChannelInboundHandler {
        @Override
        public final void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            if (isFreed()) {
                ctx.fireChannelRegistered();
            } else this.preChannelRegistered(ctx);
        }

        @Override
        public final void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
            if (isFreed()) {
                ctx.fireChannelUnregistered();
            } else this.preChannelUnregistered(ctx);
        }

        @Override
        public final void channelActive(ChannelHandlerContext ctx) throws Exception {
            if (isFreed()) {
                ctx.fireChannelActive();
            } else this.preChannelActive(ctx);
        }

        @Override
        public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if (isFreed()) {
                ctx.fireChannelInactive();
            } else this.preChannelInactive(ctx);
        }

        @Override
        public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (isFreed()) {
                ctx.fireChannelRead(msg);
                return;
            }

            try {
                this.preChannelRead(ctx, (M) msg);
            } finally {
                // always free this msg, important
                ReferenceCountUtil.release(msg);
            }
        }

        @Override
        public final void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
            if (isFreed()) {
                ctx.fireChannelReadComplete();
            } else this.preChannelReadComplete(ctx);
        }

        @Override
        public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            if (isFreed()) {
                ctx.fireUserEventTriggered(evt);
            } else this.preUserEventTriggered(ctx, evt);
        }

        @Override
        public final void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            if (isFreed()) {
                ctx.fireChannelWritabilityChanged();
            } else this.preChannelWritabilityChanged(ctx);
        }

        @Override
        @SuppressWarnings("deprecation")
        public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            if (isFreed()) {
                ctx.fireExceptionCaught(cause);
            } else this.preExceptionCaught(ctx, cause);
        }

        /**
         * Pre channel registered.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void preChannelRegistered(ChannelHandlerContext ctx) throws Exception {
            ctx.fireChannelRegistered();
        }

        /**
         * Pre channel unregistered.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void preChannelUnregistered(ChannelHandlerContext ctx) throws Exception {
            ctx.fireChannelUnregistered();
        }

        /**
         * Pre channel active.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void preChannelActive(ChannelHandlerContext ctx) throws Exception {
            ctx.fireChannelActive();
        }

        /**
         * Pre channel inactive.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void preChannelInactive(ChannelHandlerContext ctx) throws Exception {
            ctx.fireChannelInactive();
        }

        /**
         * Pre channel read.
         *
         * @param ctx the ctx
         * @param msg the msg
         * @throws Exception the exception
         */
        protected void preChannelRead(ChannelHandlerContext ctx, M msg) throws Exception {
            ctx.fireChannelRead(msg);
        }

        /**
         * Pre channel read complete.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void preChannelReadComplete(ChannelHandlerContext ctx) throws Exception {
            ctx.fireChannelReadComplete();
        }

        /**
         * Pre user event triggered.
         *
         * @param ctx the ctx
         * @param evt the evt
         * @throws Exception the exception
         */
        protected void preUserEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            ctx.fireUserEventTriggered(evt);
        }

        /**
         * Pre channel writability changed.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void preChannelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            ctx.fireChannelWritabilityChanged();
        }

        /**
         * Pre exception caught.
         *
         * @param ctx   the ctx
         * @param cause the cause
         * @throws Exception the exception
         */
        protected void preExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            ctx.fireExceptionCaught(cause);
        }

        /**
         * Free and registered.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void freeAndRegistered(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.channelRegistered(ctx);
        }

        /**
         * Free and unregistered.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void freeAndUnregistered(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.channelUnregistered(ctx);
        }

        /**
         * Free and active.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void freeAndActive(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.channelActive(ctx);
        }

        /**
         * Free and inactive.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void freeAndInactive(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.channelInactive(ctx);
        }

        /**
         * Free and read.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void freeAndRead(ChannelHandlerContext ctx, M msg) throws Exception {
            this.free();
            this.channelRead(ctx, msg);
        }

        /**
         * Free and read complete.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void freeAndReadComplete(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.channelReadComplete(ctx);
        }

        /**
         * Free and user event triggered.
         *
         * @param ctx the ctx
         * @param evt the evt
         * @throws Exception the exception
         */
        protected void freeAndUserEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            this.free();
            this.userEventTriggered(ctx, evt);
        }

        /**
         * Free and writability changed.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void freeAndWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.channelWritabilityChanged(ctx);
        }

        /**
         * Free and exception caught.
         *
         * @param ctx   the ctx
         * @param cause the cause
         * @throws Exception the exception
         */
        protected void freeAndExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            this.free();
            this.exceptionCaught(ctx, cause);
        }

        /**
         * Reset and registered.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void resetAndRegistered(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.channelRegistered(ctx);
        }

        /**
         * Reset and unregistered.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void resetAndUnregistered(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.channelUnregistered(ctx);
        }

        /**
         * Reset and active.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void resetAndActive(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.channelActive(ctx);
        }

        /**
         * Reset and inactive.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void resetAndInactive(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.channelInactive(ctx);
        }

        /**
         * Reset and read.
         *
         * @param ctx the ctx
         * @param msg the msg
         * @throws Exception the exception
         */
        protected void resetAndRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            this.reset();
            this.channelRead(ctx, msg);
        }

        /**
         * Reset and read complete.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void resetAndReadComplete(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.channelReadComplete(ctx);
        }

        /**
         * Reset and user event triggered.
         *
         * @param ctx the ctx
         * @param evt the evt
         * @throws Exception the exception
         */
        protected void resetAndUserEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            this.reset();
            this.userEventTriggered(ctx, evt);
        }

        /**
         * Reset and writability changed.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void resetAndWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.channelWritabilityChanged(ctx);
        }

        /**
         * Reset and exception caught.
         *
         * @param ctx   the ctx
         * @param cause the cause
         * @throws Exception the exception
         */
        protected void resetAndExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            this.reset();
            this.exceptionCaught(ctx, cause);
        }
    }

    public static class OutboundInterceptor extends ChannelInterceptor implements ChannelOutboundHandler {
        @Override
        public final void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            if (isFreed()) {
                ctx.bind(localAddress, promise);
            } else this.preBind(ctx, localAddress, promise);
        }

        @Override
        public final void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            if (isFreed()) {
                ctx.connect(remoteAddress, localAddress, promise);
            } else this.preConnect(ctx, remoteAddress, localAddress, promise);
        }

        @Override
        public final void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            if (isFreed()) {
                ctx.disconnect(promise);
            } else this.preDisconnect(ctx, promise);
        }

        @Override
        public final void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            if (isFreed()) {
                ctx.close(promise);
            } else this.preClose(ctx, promise);
        }

        @Override
        public final void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            if (isFreed()) {
                ctx.deregister(promise);
            } else this.preDeregister(ctx, promise);
        }

        @Override
        public final void read(ChannelHandlerContext ctx) throws Exception {
            if (isFreed()) {
                ctx.read();
            } else this.preRead(ctx);
        }

        @Override
        public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            if (isFreed()) {
                ctx.write(msg, promise);
            } else this.preWrite(ctx, msg, promise);
        }

        @Override
        public final void flush(ChannelHandlerContext ctx) throws Exception {
            if (isFreed()) {
                ctx.flush();
            } else this.preFlush(ctx);
        }

        /**
         * Pre bind.
         *
         * @param ctx          the ctx
         * @param localAddress the local address
         * @param promise      the promise
         * @throws Exception the exception
         */
        public final void preBind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            ctx.bind(localAddress, promise);
        }

        /**
         * Pre connect.
         *
         * @param ctx           the ctx
         * @param remoteAddress the remote address
         * @param localAddress  the local address
         * @param promise       the promise
         * @throws Exception the exception
         */
        public final void preConnect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            ctx.connect(remoteAddress, localAddress, promise);
        }

        /**
         * Pre disconnect.
         *
         * @param ctx     the ctx
         * @param promise the promise
         * @throws Exception the exception
         */
        public final void preDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            ctx.disconnect(promise);
        }

        /**
         * Pre close.
         *
         * @param ctx     the ctx
         * @param promise the promise
         * @throws Exception the exception
         */
        public final void preClose(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            ctx.close(promise);
        }

        /**
         * Pre deregister.
         *
         * @param ctx     the ctx
         * @param promise the promise
         * @throws Exception the exception
         */
        public final void preDeregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            ctx.deregister(promise);
        }

        /**
         * Pre read.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        public final void preRead(ChannelHandlerContext ctx) throws Exception {
            ctx.read();
        }

        /**
         * Pre write.
         *
         * @param ctx     the ctx
         * @param msg     the msg
         * @param promise the promise
         * @throws Exception the exception
         */
        public final void preWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            ctx.write(msg, promise);
        }

        /**
         * Pre flush.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        public final void preFlush(ChannelHandlerContext ctx) throws Exception {
            ctx.flush();
        }

        /**
         * Free and bind.
         *
         * @param ctx          the ctx
         * @param localAddress the local address
         * @param promise      the promise
         * @throws Exception the exception
         */
        protected void freeAndBind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            this.free();
            this.bind(ctx, localAddress, promise);
        }

        /**
         * Free and connect.
         *
         * @param ctx           the ctx
         * @param remoteAddress the remote address
         * @param localAddress  the local address
         * @param promise       the promise
         * @throws Exception the exception
         */
        protected void freeAndConnect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            this.free();
            this.connect(ctx, remoteAddress, localAddress, promise);
        }

        /**
         * Free and disconnect.
         *
         * @param ctx     the ctx
         * @param promise the promise
         * @throws Exception the exception
         */
        protected void freeAndDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.free();
            this.disconnect(ctx, promise);
        }

        /**
         * Free and close.
         *
         * @param ctx     the ctx
         * @param promise the promise
         * @throws Exception the exception
         */
        protected void freeAndClose(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.free();
            this.close(ctx, promise);
        }

        /**
         * Free and deregister.
         *
         * @param ctx     the ctx
         * @param promise the promise
         * @throws Exception the exception
         */
        protected void freeAndDeregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.free();
            this.deregister(ctx, promise);
        }

        /**
         * Free and read.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void freeAndRead(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.read(ctx);
        }

        /**
         * Free and write.
         *
         * @param ctx     the ctx
         * @param msg     the msg
         * @param promise the promise
         * @throws Exception the exception
         */
        protected void freeAndWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            this.free();
            this.write(ctx, msg, promise);
        }

        /**
         * Free and flush.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void freeAndFlush(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.flush(ctx);
        }

        /**
         * Reset and bind.
         *
         * @param ctx          the ctx
         * @param localAddress the local address
         * @param promise      the promise
         * @throws Exception the exception
         */
        protected void resetAndBind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            this.reset();
            this.bind(ctx, localAddress, promise);
        }

        /**
         * Reset and connect.
         *
         * @param ctx           the ctx
         * @param remoteAddress the remote address
         * @param localAddress  the local address
         * @param promise       the promise
         * @throws Exception the exception
         */
        protected void resetAndConnect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            this.reset();
            this.connect(ctx, remoteAddress, localAddress, promise);
        }

        /**
         * Reset and disconnect.
         *
         * @param ctx     the ctx
         * @param promise the promise
         * @throws Exception the exception
         */
        protected void resetAndDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.reset();
            this.disconnect(ctx, promise);
        }

        /**
         * Reset and close.
         *
         * @param ctx     the ctx
         * @param promise the promise
         * @throws Exception the exception
         */
        protected void resetAndClose(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.reset();
            this.close(ctx, promise);
        }

        /**
         * Reset and deregister.
         *
         * @param ctx     the ctx
         * @param promise the promise
         * @throws Exception the exception
         */
        protected void resetAndDeregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.reset();
            this.deregister(ctx, promise);
        }

        /**
         * Reset and read.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void resetAndRead(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.read(ctx);
        }

        /**
         * Reset and write.
         *
         * @param ctx     the ctx
         * @param msg     the msg
         * @param promise the promise
         * @throws Exception the exception
         */
        protected void resetAndWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            this.reset();
            this.write(ctx, msg, promise);
        }

        /**
         * Reset and flush.
         *
         * @param ctx the ctx
         * @throws Exception the exception
         */
        protected void resetAndFlush(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.flush(ctx);
        }

    }
}
