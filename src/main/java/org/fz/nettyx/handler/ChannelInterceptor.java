package org.fz.nettyx.handler;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import org.fz.util.exception.Throws;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * channel interceptor
 * Not Thread Safe!!!
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /4/25 15:46
 */
@SuppressWarnings("unchecked")
public abstract class ChannelInterceptor extends ChannelHandlerAdapter {

    protected final boolean defaultInterceptAll;

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

    protected ChannelInterceptor() {
        // All methods will NOT be intercepted by default, which method should be overridden for which event you want to intercept
        this(false);
    }

    protected ChannelInterceptor(boolean defaultInterceptAll) {
        this.defaultInterceptAll = defaultInterceptAll;
    }

    /**
     * Subclass [InboundInterceptor, OutboundInterceptor] implementation start
     */

    @SuppressWarnings("unchecked")
    public static class InboundInterceptor<M> extends ChannelInterceptor implements ChannelInboundHandler {

        @Override
        public final void channelRegistered(ChannelHandlerContext ctx) {
            if (isFreed()) ctx.fireChannelRegistered();
            else           this.preChannelRegistered(ctx);
        }

        @Override
        public final void channelUnregistered(ChannelHandlerContext ctx) {
            if (isFreed()) ctx.fireChannelUnregistered();
            else           this.preChannelUnregistered(ctx);
        }

        @Override
        public final void channelActive(ChannelHandlerContext ctx) {
            if (isFreed()) ctx.fireChannelActive();
            else           this.preChannelActive(ctx);
        }

        @Override
        public final void channelInactive(ChannelHandlerContext ctx) {
            if (isFreed()) ctx.fireChannelInactive();
            else           this.preChannelInactive(ctx);
        }

        @Override
        public final void channelRead(ChannelHandlerContext ctx, Object msg) {
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
        public final void channelReadComplete(ChannelHandlerContext ctx) {
            if (isFreed()) ctx.fireChannelReadComplete();
            else           this.preChannelReadComplete(ctx);
        }

        @Override
        public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (isFreed()) ctx.fireUserEventTriggered(evt);
            else           this.preUserEventTriggered(ctx, evt);
        }

        @Override
        public final void channelWritabilityChanged(ChannelHandlerContext ctx) {
            if (isFreed()) ctx.fireChannelWritabilityChanged();
            else           this.preChannelWritabilityChanged(ctx);
        }

        @Override
        @SuppressWarnings("deprecation")
        public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (isFreed()) ctx.fireExceptionCaught(cause);
            else           this.preExceptionCaught(ctx, cause);
        }

        protected void preChannelRegistered(ChannelHandlerContext ctx) {
            if (!defaultInterceptAll) ctx.fireChannelRegistered();
        }

        protected void preChannelUnregistered(ChannelHandlerContext ctx) {
            if (!defaultInterceptAll) ctx.fireChannelUnregistered();
        }

        protected void preChannelActive(ChannelHandlerContext ctx) {
            if (!defaultInterceptAll) ctx.fireChannelActive();
        }

        protected void preChannelInactive(ChannelHandlerContext ctx) {
            if (!defaultInterceptAll) ctx.fireChannelInactive();
        }

        protected void preChannelRead(ChannelHandlerContext ctx, M msg) {
            if (!defaultInterceptAll) ctx.fireChannelRead(msg);
        }

        protected void preChannelReadComplete(ChannelHandlerContext ctx) {
            if (!defaultInterceptAll) ctx.fireChannelReadComplete();
        }

        protected void preUserEventTriggered(ChannelHandlerContext ctx, Object evt) {
            if (!defaultInterceptAll) ctx.fireUserEventTriggered(evt);
        }

        protected void preChannelWritabilityChanged(ChannelHandlerContext ctx) {
            if (!defaultInterceptAll) ctx.fireChannelWritabilityChanged();
        }

        protected void preExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            if (!defaultInterceptAll) ctx.fireExceptionCaught(cause);
        }

        protected void freeAndFireRegistered(ChannelHandlerContext ctx) {
            this.free();
            this.channelRegistered(ctx);
        }

        protected void freeAndFireUnregistered(ChannelHandlerContext ctx) {
            this.free();
            this.channelUnregistered(ctx);
        }

        protected void freeAndFireActive(ChannelHandlerContext ctx) {
            this.free();
            this.channelActive(ctx);
        }

        protected void freeAndFireInactive(ChannelHandlerContext ctx) {
            this.free();
            this.channelInactive(ctx);
        }

        protected void freeAndFireRead(ChannelHandlerContext ctx, M msg) {
            this.free();
            this.channelRead(ctx, msg);
        }

        protected void freeAndFireReadComplete(ChannelHandlerContext ctx) {
            this.free();
            this.channelReadComplete(ctx);
        }

        protected void freeAndFireUserEventTriggered(ChannelHandlerContext ctx, Object evt) {
            this.free();
            this.userEventTriggered(ctx, evt);
        }

        protected void freeAndFireWritabilityChanged(ChannelHandlerContext ctx) {
            this.free();
            this.channelWritabilityChanged(ctx);
        }

        protected void freeAndFireExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            this.free();
            this.exceptionCaught(ctx, cause);
        }

        protected void resetAndFireRegistered(ChannelHandlerContext ctx) {
            this.reset();
            this.channelRegistered(ctx);
        }

        protected void resetAndFireUnregistered(ChannelHandlerContext ctx) {
            this.reset();
            this.channelUnregistered(ctx);
        }

        protected void resetAndFireActive(ChannelHandlerContext ctx) {
            this.reset();
            this.channelActive(ctx);
        }

        protected void resetAndFireInactive(ChannelHandlerContext ctx) {
            this.reset();
            this.channelInactive(ctx);
        }

        protected void resetAndFireRead(ChannelHandlerContext ctx, Object msg) {
            this.reset();
            this.channelRead(ctx, msg);
        }

        protected void resetAndFireReadComplete(ChannelHandlerContext ctx) {
            this.reset();
            this.channelReadComplete(ctx);
        }

        protected void resetAndFireUserEventTriggered(ChannelHandlerContext ctx, Object evt) {
            this.reset();
            this.userEventTriggered(ctx, evt);
        }

        protected void resetAndFireWritabilityChanged(ChannelHandlerContext ctx) {
            this.reset();
            this.channelWritabilityChanged(ctx);
        }

        protected void resetAndFireExceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            this.reset();
            this.exceptionCaught(ctx, cause);
        }
    }

    public static class OutboundInterceptor extends ChannelInterceptor implements ChannelOutboundHandler {

        @Override
        public final void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
            if (isFreed()) ctx.bind(localAddress, promise);
            else           this.preBind(ctx, localAddress, promise);
        }

        @Override
        public final void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            if (isFreed()) ctx.connect(remoteAddress, localAddress, promise);
            else           this.preConnect(ctx, remoteAddress, localAddress, promise);
        }

        @Override
        public final void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
            if (isFreed()) ctx.disconnect(promise);
            else           this.preDisconnect(ctx, promise);
        }

        @Override
        public final void close(ChannelHandlerContext ctx, ChannelPromise promise) {
            if (isFreed()) ctx.close(promise);
            else           this.preClose(ctx, promise);
        }

        @Override
        public final void deregister(ChannelHandlerContext ctx, ChannelPromise promise) {
            if (isFreed()) ctx.deregister(promise);
            else           this.preDeregister(ctx, promise);
        }

        @Override
        public final void read(ChannelHandlerContext ctx) {
            if (isFreed()) ctx.read();
            else           this.preRead(ctx);
        }

        @Override
        public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            if (isFreed()) ctx.write(msg, promise);
            else           this.preWrite(ctx, msg, promise);
        }

        @Override
        public final void flush(ChannelHandlerContext ctx) {
            if (isFreed()) ctx.flush();
            else           this.preFlush(ctx);
        }

        public final void preBind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
            if (!defaultInterceptAll) ctx.bind(localAddress, promise);
        }

        public final void preConnect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            if (!defaultInterceptAll) ctx.connect(remoteAddress, localAddress, promise);
        }

        public final void preDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
            if (!defaultInterceptAll) ctx.disconnect(promise);
        }

        public final void preClose(ChannelHandlerContext ctx, ChannelPromise promise) {
            if (!defaultInterceptAll) ctx.close(promise);
        }

        public final void preDeregister(ChannelHandlerContext ctx, ChannelPromise promise) {
            if (!defaultInterceptAll) ctx.deregister(promise);
        }

        public final void preRead(ChannelHandlerContext ctx) {
            if (!defaultInterceptAll) ctx.read();
        }

        public final void preWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            if (!defaultInterceptAll) ctx.write(msg, promise);
        }

        public final void preFlush(ChannelHandlerContext ctx) {
            if (!defaultInterceptAll) ctx.flush();
        }

        protected void freeAndBind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
            this.free();
            this.bind(ctx, localAddress, promise);
        }

        protected void freeAndConnect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            this.free();
            this.connect(ctx, remoteAddress, localAddress, promise);
        }

        protected void freeAndDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
            this.free();
            this.disconnect(ctx, promise);
        }

        protected void freeAndClose(ChannelHandlerContext ctx, ChannelPromise promise) {
            this.free();
            this.close(ctx, promise);
        }

        protected void freeAndDeregister(ChannelHandlerContext ctx, ChannelPromise promise) {
            this.free();
            this.deregister(ctx, promise);
        }

        protected void freeAndRead(ChannelHandlerContext ctx) {
            this.free();
            this.read(ctx);
        }

        protected void freeAndWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
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

        protected void resetAndConnect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) {
            this.reset();
            this.connect(ctx, remoteAddress, localAddress, promise);
        }

        protected void resetAndDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
            this.reset();
            this.disconnect(ctx, promise);
        }

        protected void resetAndClose(ChannelHandlerContext ctx, ChannelPromise promise) {
            this.reset();
            this.close(ctx, promise);
        }

        protected void resetAndDeregister(ChannelHandlerContext ctx, ChannelPromise promise) {
            this.reset();
            this.deregister(ctx, promise);
        }

        protected void resetAndRead(ChannelHandlerContext ctx) {
            this.reset();
            this.read(ctx);
        }

        protected void resetAndWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            this.reset();
            this.write(ctx, msg, promise);
        }

        protected void resetAndFlush(ChannelHandlerContext ctx) {
            this.reset();
            this.flush(ctx);
        }

    }

    //***************************************    public static start   ***********************************************//

    public static <T extends ChannelInterceptor> T getInterceptor(ChannelPipeline pipeline, String interceptorName) {
        for (Map.Entry<String, ChannelHandler> entry : pipeline) {
            boolean isTargetInterceptor =
                    entry.getKey().equalsIgnoreCase(interceptorName)
                    &&
                    ChannelInterceptor.class.isAssignableFrom(entry.getValue().getClass());

            if (isTargetInterceptor) return (T) entry.getValue();
        }

        return null;
    }

    public static <T extends ChannelInterceptor> T getInterceptor(ChannelPipeline pipeline, Class<?> interceptorClass) {
        Throws.ifNotAssignable(ChannelInterceptor.class, interceptorClass,
                               () -> "class [" + interceptorClass + "] is not assignable to ChannelInterceptor");

        for (Map.Entry<String, ChannelHandler> entry : pipeline) {
            if (interceptorClass.isAssignableFrom(entry.getValue().getClass())) return (T) entry.getValue();
        }

        return null;
    }

    public static <T extends ChannelInterceptor> List<T> getInterceptors(Channel channel) {
        return getInterceptors(channel.pipeline());
    }

    public static <T extends ChannelInterceptor> List<T> getInterceptors(ChannelHandlerContext ctx) {
        return getInterceptors(ctx.pipeline());
    }


    public static <T extends ChannelInterceptor> List<T> getInterceptors(ChannelPipeline pipeline) {
        List<T> result = new ArrayList<>(10);

        for (Map.Entry<String, ChannelHandler> entry : pipeline) {
            if (ChannelInterceptor.class.isAssignableFrom(entry.getValue().getClass())) result.add((T) entry.getValue());
        }

        return result;
    }

    public static void free(Channel channel, Class<?> interceptorClass) {
        free(channel.pipeline(), interceptorClass);
    }

    public static void free(ChannelHandlerContext ctx, Class<?> interceptorClass) {
        free(ctx.pipeline(), interceptorClass);
    }

    public static void free(Channel channel, String interceptorName) {
        free(channel.pipeline(), interceptorName);
    }

    public static void free(ChannelHandlerContext ctx, String interceptorName) {
        free(ctx.pipeline(), interceptorName);
    }

    public static void free(ChannelPipeline pipeline, String interceptorName) {
        ChannelInterceptor interceptor = getInterceptor(pipeline, interceptorName);
        if (interceptor != null) interceptor.free();
    }

    public static void free(ChannelPipeline pipeline, Class<?> interceptorClass) {
        ChannelInterceptor interceptor = getInterceptor(pipeline, interceptorClass);
        if (interceptor != null) interceptor.free();
    }

    public static void freeAll(Channel channel) {
        freeAll(channel.pipeline());
    }

    public static void freeAll(ChannelHandlerContext ctx) {
        freeAll(ctx.pipeline());
    }

    public static void freeAll(ChannelPipeline pipeline) {
        getInterceptors(pipeline).stream().filter(ChannelInterceptor::isNotFreed).forEach(ChannelInterceptor::free);
    }

    public static void reset(Channel channel, Class<?> interceptorClass) {
        reset(channel.pipeline(), interceptorClass);
    }

    public static void reset(ChannelHandlerContext ctx, Class<?> interceptorClass) {
        reset(ctx.pipeline(), interceptorClass);
    }

    public static void reset(Channel channel, String interceptorName) {
        reset(channel.pipeline(), interceptorName);
    }

    public static void reset(ChannelHandlerContext ctx, String interceptorName) {
        reset(ctx.pipeline(), interceptorName);
    }

    public static void reset(ChannelPipeline pipeline, String interceptorName) {
        ChannelInterceptor interceptor = getInterceptor(pipeline, interceptorName);
        if (interceptor != null) interceptor.reset();
    }

    public static void reset(ChannelPipeline pipeline, Class<?> interceptorClass) {
        ChannelInterceptor interceptor = getInterceptor(pipeline, interceptorClass);
        if (interceptor != null) interceptor.reset();
    }

    public static void resetAll(Channel channel) {
        resetAll(channel.pipeline());
    }

    public static void resetAll(ChannelHandlerContext ctx) {
        resetAll(ctx.pipeline());
    }

    public static void resetAll(ChannelPipeline pipeline) {
        getInterceptors(pipeline).stream().filter(ChannelInterceptor::isFreed).forEach(ChannelInterceptor::reset);
    }

    //***************************************    public static end     ***********************************************//
}
