package org.fz.nettyx.handler.interceptor;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPromise;
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

        protected void preChannelRegistered(ChannelHandlerContext ctx) {
            ctx.fireChannelRegistered();
        }

        protected void preChannelUnregistered(ChannelHandlerContext ctx) {
            ctx.fireChannelUnregistered();
        }

        protected void preChannelActive(ChannelHandlerContext ctx) {
            ctx.fireChannelActive();
        }

        protected void preChannelInactive(ChannelHandlerContext ctx) {
            ctx.fireChannelInactive();
        }

        protected void preChannelRead(ChannelHandlerContext ctx, M msg) {
            ctx.fireChannelRead(msg);
        }

        protected void preChannelReadComplete(ChannelHandlerContext ctx) {
            ctx.fireChannelReadComplete();
        }

        protected void preUserEventTriggered(ChannelHandlerContext ctx, Object evt) {
            ctx.fireUserEventTriggered(evt);
        }

        protected void preChannelWritabilityChanged(ChannelHandlerContext ctx) {
            ctx.fireChannelWritabilityChanged();
        }

        protected void preExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.fireExceptionCaught(cause);
        }

        protected void freeAndFireRegistered(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.channelRegistered(ctx);
        }

        protected void freeAndFireUnregistered(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.channelUnregistered(ctx);
        }

        protected void freeAndFireActive(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.channelActive(ctx);
        }

        protected void freeAndFireInactive(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.channelInactive(ctx);
        }

        protected void freeAndFireRead(ChannelHandlerContext ctx, M msg) throws Exception {
            this.free();
            this.channelRead(ctx, msg);
        }

        protected void freeAndFireReadComplete(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.channelReadComplete(ctx);
        }

        protected void freeAndFireUserEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            this.free();
            this.userEventTriggered(ctx, evt);
        }

        protected void freeAndFireWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.channelWritabilityChanged(ctx);
        }

        protected void freeAndFireExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            this.free();
            this.exceptionCaught(ctx, cause);
        }

        protected void resetAndFireRegistered(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.channelRegistered(ctx);
        }

        protected void resetAndFireUnregistered(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.channelUnregistered(ctx);
        }

        protected void resetAndFireActive(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.channelActive(ctx);
        }

        protected void resetAndFireInactive(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.channelInactive(ctx);
        }

        protected void resetAndFireRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            this.reset();
            this.channelRead(ctx, msg);
        }

        protected void resetAndFireReadComplete(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.channelReadComplete(ctx);
        }

        protected void resetAndFireUserEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
            this.reset();
            this.userEventTriggered(ctx, evt);
        }

        protected void resetAndFireWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.channelWritabilityChanged(ctx);
        }

        protected void resetAndFireExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            this.reset();
            this.exceptionCaught(ctx, cause);
        }
    }

    public static class OutboundInterceptor extends ChannelInterceptor implements ChannelOutboundHandler {

        @Override
        public final void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
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
        public final void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
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
        public final void deregister(ChannelHandlerContext ctx, ChannelPromise promise) {
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
        public final void flush(ChannelHandlerContext ctx) {
            if (isFreed()) {
                ctx.flush();
            } else this.preFlush(ctx);
        }

        public final void preBind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
            ctx.bind(localAddress, promise);
        }

        public final void preConnect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            ctx.connect(remoteAddress, localAddress, promise);
        }

        public final void preDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
            ctx.disconnect(promise);
        }

        public final void preClose(ChannelHandlerContext ctx, ChannelPromise promise) {
            ctx.close(promise);
        }

        public final void preDeregister(ChannelHandlerContext ctx, ChannelPromise promise) {
            ctx.deregister(promise);
        }

        public final void preRead(ChannelHandlerContext ctx) {
            ctx.read();
        }

        public final void preWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            ctx.write(msg, promise);
        }

        public final void preFlush(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        protected void freeAndBind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            this.free();
            this.bind(ctx, localAddress, promise);
        }

        protected void freeAndConnect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            this.free();
            this.connect(ctx, remoteAddress, localAddress, promise);
        }

        protected void freeAndDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
            this.free();
            this.disconnect(ctx, promise);
        }

        protected void freeAndClose(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.free();
            this.close(ctx, promise);
        }

        protected void freeAndDeregister(ChannelHandlerContext ctx, ChannelPromise promise) {
            this.free();
            this.deregister(ctx, promise);
        }

        protected void freeAndRead(ChannelHandlerContext ctx) throws Exception {
            this.free();
            this.read(ctx);
        }

        protected void freeAndWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            this.free();
            this.write(ctx, msg, promise);
        }

        protected void freeAndFlush(ChannelHandlerContext ctx) {
            this.free();
            this.flush(ctx);
        }

        protected void resetAndBind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
            this.reset();
            this.bind(ctx, localAddress, promise);
        }

        protected void resetAndConnect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
            this.reset();
            this.connect(ctx, remoteAddress, localAddress, promise);
        }

        protected void resetAndDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.reset();
            this.disconnect(ctx, promise);
        }

        protected void resetAndClose(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
            this.reset();
            this.close(ctx, promise);
        }

        protected void resetAndDeregister(ChannelHandlerContext ctx, ChannelPromise promise) {
            this.reset();
            this.deregister(ctx, promise);
        }

        protected void resetAndRead(ChannelHandlerContext ctx) throws Exception {
            this.reset();
            this.read(ctx);
        }

        protected void resetAndWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
            this.reset();
            this.write(ctx, msg, promise);
        }

        protected void resetAndFlush(ChannelHandlerContext ctx) {
            this.reset();
            this.flush(ctx);
        }

    }
}
