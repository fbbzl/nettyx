package org.fz.nettyx.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.util.ReferenceCountUtil;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * channel interceptor
 * All methods will NOT be intercepted by default, which method should be overridden for which event you want to intercept
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /4/25 15:46
 */
@SuppressWarnings("unchecked")
public abstract class ChannelInterceptor<T> extends ChannelDuplexHandler {

    /**
     * interceptor state
     * true : Means this interceptor no longer intercepts any events
     * false: Means the specified channel-event will be intercepted
     */
    private final AtomicBoolean state = new AtomicBoolean(false);

    // getter and setter of freeStatus
    private boolean freed() {
        return state.get();
    }
    private boolean unfreed() {
        return !state.get();
    }

    /**
     * after use this method, this ChannelInboundHandler will be freed which will no longer participate in message parsing
     *
     * @see ChannelInterceptor#reset() use this to recover this handler
     */
    public void free() {
        this.state.set(true);
    }

    /**
     * after using this method, this ChannelInboundHandler will re participate in message parsing
     *
     * @see ChannelInterceptor#free() use method to free
     */
    public void reset() {
        this.state.set(false);
    }

    @Override
    public final void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (freed()) {
            super.channelRegistered(ctx);
        }
        else this.preChannelRegistered(ctx);
    }

    @Override
    public final void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (freed()) {
            super.channelUnregistered(ctx);
        }
        else this.preChannelUnregistered(ctx);
    }

    @Override
    public final void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (freed()) {
            super.channelActive(ctx);
        }
        else this.preChannelActive(ctx);
    }

    @Override
    public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (freed()) {
            super.channelInactive(ctx);
        }
        else this.preChannelInactive(ctx);
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (freed()) {
            super.channelRead(ctx, msg);
            return;
        }

        try {
            this.preChannelRead(ctx, (T) msg);
        }
        finally {
            // always free this msg, important
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public final void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (freed()) {
            super.channelReadComplete(ctx);
        }
        else this.preChannelReadComplete(ctx);
    }

    @Override
    public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (freed()) {
            super.userEventTriggered(ctx, evt);
        }
        else this.preUserEventTriggered(ctx, evt);
    }

    @Override
    public final void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (freed()) {
            super.channelWritabilityChanged(ctx);
        }
        else this.preChannelWritabilityChanged(ctx);
    }

    @Override
    public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (freed()) {
            super.exceptionCaught(ctx, cause);
        }
        else this.preExceptionCaught(ctx, cause);
    }

    @Override
    public final void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        if (freed()) {
            super.bind(ctx, localAddress, promise);
        }
        else this.preBind(ctx, localAddress, promise);
    }

    @Override
    public final void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        if (freed()) {
            super.connect(ctx, remoteAddress, localAddress, promise);
        }
        else this.preConnect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public final void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (freed()) {
            super.disconnect(ctx, promise);
        }
        else this.preDisconnect(ctx, promise);
    }

    @Override
    public final void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (freed()) {
            super.close(ctx, promise);
        }
        else this.preClose(ctx, promise);
    }

    @Override
    public final void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (freed()) {
            super.deregister(ctx, promise);
        }
        else this.preDeregister(ctx, promise);
    }

    @Override
    public final void read(ChannelHandlerContext ctx) throws Exception {
        if (freed()) {
            super.read(ctx);
        }
        else this.preRead(ctx);
    }

    @Override
    public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (freed()) {
            super.write(ctx, msg, promise);
        }
        else this.preWrite(ctx, msg, promise);
    }

    @Override
    public final void flush(ChannelHandlerContext ctx) throws Exception {
        if (freed()) {
            super.flush(ctx);
        }
        else this.preFlush(ctx);
    }

    // all preXxx methods will not intercept the process
    protected void preChannelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    protected void preChannelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    protected void preChannelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    protected void preChannelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    protected void preChannelRead(ChannelHandlerContext ctx, T msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    protected void preChannelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    protected void preUserEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    protected void preChannelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    protected void preExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }

    public final void preBind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        super.bind(ctx, localAddress, promise);
    }

    public final void preConnect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    public final void preDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
    }

    public final void preClose(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.close(ctx, promise);
    }

    public final void preDeregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.deregister(ctx, promise);
    }

    public final void preRead(ChannelHandlerContext ctx) throws Exception {
        super.read(ctx);
    }

    public final void preWrite(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        super.write(ctx, msg, promise);
    }

    public final void preFlush(ChannelHandlerContext ctx) throws Exception {
        super.flush(ctx);
    }

    //**********************************           free combined-method start              **************************************//

    protected void freeAndRegistered(ChannelHandlerContext ctx) throws Exception {
        this.free();
        this.channelRegistered(ctx);
    }

    protected void freeAndUnregistered(ChannelHandlerContext ctx) throws Exception {
        this.free();
        this.channelUnregistered(ctx);
    }

    protected void freeAndActive(ChannelHandlerContext ctx) throws Exception {
        this.free();
        this.channelActive(ctx);
    }

    protected void freeAndInactive(ChannelHandlerContext ctx) throws Exception {
        this.free();
        this.channelInactive(ctx);
    }

    protected void freeAndRead(ChannelHandlerContext ctx, T msg) throws Exception {
        this.free();
        this.channelRead(ctx, msg);
    }

    protected void freeAndReadComplete(ChannelHandlerContext ctx) throws Exception {
        this.free();
        this.channelReadComplete(ctx);
    }

    protected void freeAndUserEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        this.free();
        this.userEventTriggered(ctx, evt);
    }

    protected void freeAndWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        this.free();
        this.channelWritabilityChanged(ctx);
    }

    protected void freeAndExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.free();
        this.exceptionCaught(ctx, cause);
    }

    protected void freeAndBind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        this.free();
        this.bind(ctx, localAddress, promise);
    }
    
    protected void freeAndConnect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        this.free();
        this.connect(ctx, remoteAddress, localAddress, promise);
    }

    protected void freeAndDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        this.free();
        this.disconnect(ctx, promise);
    }

    protected void freeAndClose(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        this.free();
        this.close(ctx, promise);
    }

    protected void freeAndDeregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
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
    
    protected void freeAndFlush(ChannelHandlerContext ctx) throws Exception {
        this.free();
        this.flush(ctx);
    }

    //**********************************           free combined-method end             **************************************//

    //**********************************           reset combined-method start          **************************************//

    protected void resetAndRegistered(ChannelHandlerContext ctx) throws Exception {
        this.reset();
        this.channelRegistered(ctx);
    }

    protected void  resetAndUnregistered(ChannelHandlerContext ctx) throws Exception {
        this.reset();
        this.channelUnregistered(ctx);
    }

    protected void resetAndActive(ChannelHandlerContext ctx) throws Exception {
        this.reset();
        this.channelActive(ctx);
    }

    protected void resetAndInactive(ChannelHandlerContext ctx) throws Exception {
        this.reset();
        this.channelInactive(ctx);
    }

    protected void resetAndRead(ChannelHandlerContext ctx, T msg) throws Exception {
        this.reset();
        this.channelRead(ctx, msg);
    }

    protected void resetAndReadComplete(ChannelHandlerContext ctx) throws Exception {
        this.reset();
        this.channelReadComplete(ctx);
    }

    protected void resetAndUserEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        this.reset();
        this.userEventTriggered(ctx, evt);
    }

    protected void resetAndWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        this.reset();
        this.channelWritabilityChanged(ctx);
    }

    protected void resetAndExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        this.reset();
        this.exceptionCaught(ctx, cause);
    }

    protected void resetAndBind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
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

    protected void resetAndDeregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
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

    protected void resetAndFlush(ChannelHandlerContext ctx) throws Exception {
        this.reset();
        this.flush(ctx);
    }

    //**********************************           reset combined-method end           **************************************//

    /**
     * tool class used with ChannelInterceptor
     * @see ChannelInterceptor
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ChannelInterceptors {

        public static <T extends ChannelInterceptor<?>> List<T> getInterceptors(Channel channel) {
            return getInterceptors(channel.pipeline());
        }
        public static <T extends ChannelInterceptor<?>> List<T> getInterceptors(ChannelHandlerContext ctx) {
            return getInterceptors(ctx.pipeline());
        }

        public static <T extends ChannelInterceptor<?>> List<T> getInterceptors(ChannelPipeline pipeline) {
            List<T> result = new ArrayList<>(10);

            for (Entry<String, ChannelHandler> entry : pipeline) {
                if (ChannelInterceptor.class.isAssignableFrom(entry.getValue().getClass())) {
                    result.add((T) entry.getValue());
                }
            }

            return result;
        }

        /**
         * reset all channel interceptors in the pipeline
         * @param channel channel
         */
        public static void freeAll(Channel channel) {
            freeAll(channel.pipeline());
        }

        /**
         * reset all channel interceptors in the pipeline
         * @param ctx ctx
         */
        public static void freeAll(ChannelHandlerContext ctx) {
            freeAll(ctx.pipeline());
        }

        /**
         * reset all channel interceptors in the pipeline
         * @param pipeline pipeline
         */
        public static void freeAll(ChannelPipeline pipeline) {
            getInterceptors(pipeline).stream().filter(ChannelInterceptor::unfreed).forEach(ChannelInterceptor::free);
        }

        /**
         * reset all channel interceptors in the pipeline
         * @param channel channel
         */
        public static void resetAll(Channel channel) {
            resetAll(channel.pipeline());
        }

        /**
         * reset all channel interceptors in the pipeline
         * @param ctx ctx
         */
        public static void resetAll(ChannelHandlerContext ctx) {
            resetAll(ctx.pipeline());
        }

        /**
         * reset all channel interceptors in the pipeline
         * @param pipeline pipeline
         */
        public static void resetAll(ChannelPipeline pipeline) {
            getInterceptors(pipeline).stream().filter(ChannelInterceptor::freed).forEach(ChannelInterceptor::reset);
        }
    }
}
