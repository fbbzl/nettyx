package org.fz.nettyx.handler;

import io.netty.channel.*;
import io.netty.util.ReferenceCountUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 * channel interceptor
 * All methods will NOT be intercepted by default, which method should be overridden for which event you want to intercept
 *
 * @param <T> the type parameter
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
    private volatile boolean state = false;

    // getter and setter of state
    private boolean isFreed() {
        return state;
    }
    private boolean isNotFreed() {
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

    @Override
    public final void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        if (isFreed()) {
            super.channelRegistered(ctx);
        }
        else this.preChannelRegistered(ctx);
    }

    @Override
    public final void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        if (isFreed()) {
            super.channelUnregistered(ctx);
        }
        else this.preChannelUnregistered(ctx);
    }

    @Override
    public final void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (isFreed()) {
            super.channelActive(ctx);
        }
        else this.preChannelActive(ctx);
    }

    @Override
    public final void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (isFreed()) {
            super.channelInactive(ctx);
        }
        else this.preChannelInactive(ctx);
    }

    @Override
    public final void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (isFreed()) {
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
        if (isFreed()) {
            super.channelReadComplete(ctx);
        }
        else this.preChannelReadComplete(ctx);
    }

    @Override
    public final void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (isFreed()) {
            super.userEventTriggered(ctx, evt);
        }
        else this.preUserEventTriggered(ctx, evt);
    }

    @Override
    public final void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        if (isFreed()) {
            super.channelWritabilityChanged(ctx);
        }
        else this.preChannelWritabilityChanged(ctx);
    }

    @Override
    public final void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if (isFreed()) {
            super.exceptionCaught(ctx, cause);
        }
        else this.preExceptionCaught(ctx, cause);
    }

    @Override
    public final void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        if (isFreed()) {
            super.bind(ctx, localAddress, promise);
        }
        else this.preBind(ctx, localAddress, promise);
    }

    @Override
    public final void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress, ChannelPromise promise) throws Exception {
        if (isFreed()) {
            super.connect(ctx, remoteAddress, localAddress, promise);
        }
        else this.preConnect(ctx, remoteAddress, localAddress, promise);
    }

    @Override
    public final void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (isFreed()) {
            super.disconnect(ctx, promise);
        }
        else this.preDisconnect(ctx, promise);
    }

    @Override
    public final void close(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (isFreed()) {
            super.close(ctx, promise);
        }
        else this.preClose(ctx, promise);
    }

    @Override
    public final void deregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        if (isFreed()) {
            super.deregister(ctx, promise);
        }
        else this.preDeregister(ctx, promise);
    }

    @Override
    public final void read(ChannelHandlerContext ctx) throws Exception {
        if (isFreed()) {
            super.read(ctx);
        }
        else this.preRead(ctx);
    }

    @Override
    public final void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (isFreed()) {
            super.write(ctx, msg, promise);
        }
        else this.preWrite(ctx, msg, promise);
    }

    @Override
    public final void flush(ChannelHandlerContext ctx) throws Exception {
        if (isFreed()) {
            super.flush(ctx);
        }
        else this.preFlush(ctx);
    }

    /**
     * Pre channel registered.
     *
     * @param ctx the ctx
     * @throws Exception the exception
     */
// all preXxx methods will not intercept the process
    protected void preChannelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    /**
     * Pre channel unregistered.
     *
     * @param ctx the ctx
     * @throws Exception the exception
     */
    protected void preChannelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    /**
     * Pre channel active.
     *
     * @param ctx the ctx
     * @throws Exception the exception
     */
    protected void preChannelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    /**
     * Pre channel inactive.
     *
     * @param ctx the ctx
     * @throws Exception the exception
     */
    protected void preChannelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    /**
     * Pre channel read.
     *
     * @param ctx the ctx
     * @param msg the msg
     * @throws Exception the exception
     */
    protected void preChannelRead(ChannelHandlerContext ctx, T msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    /**
     * Pre channel read complete.
     *
     * @param ctx the ctx
     * @throws Exception the exception
     */
    protected void preChannelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    /**
     * Pre user event triggered.
     *
     * @param ctx the ctx
     * @param evt the evt
     * @throws Exception the exception
     */
    protected void preUserEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    /**
     * Pre channel writability changed.
     *
     * @param ctx the ctx
     * @throws Exception the exception
     */
    protected void preChannelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    /**
     * Pre exception caught.
     *
     * @param ctx   the ctx
     * @param cause the cause
     * @throws Exception the exception
     */
    protected void preExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
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
        super.bind(ctx, localAddress, promise);
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
        super.connect(ctx, remoteAddress, localAddress, promise);
    }

    /**
     * Pre disconnect.
     *
     * @param ctx     the ctx
     * @param promise the promise
     * @throws Exception the exception
     */
    public final void preDisconnect(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.disconnect(ctx, promise);
    }

    /**
     * Pre close.
     *
     * @param ctx     the ctx
     * @param promise the promise
     * @throws Exception the exception
     */
    public final void preClose(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.close(ctx, promise);
    }

    /**
     * Pre deregister.
     *
     * @param ctx     the ctx
     * @param promise the promise
     * @throws Exception the exception
     */
    public final void preDeregister(ChannelHandlerContext ctx, ChannelPromise promise) throws Exception {
        super.deregister(ctx, promise);
    }

    /**
     * Pre read.
     *
     * @param ctx the ctx
     * @throws Exception the exception
     */
    public final void preRead(ChannelHandlerContext ctx) throws Exception {
        super.read(ctx);
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
        super.write(ctx, msg, promise);
    }

    /**
     * Pre flush.
     *
     * @param ctx the ctx
     * @throws Exception the exception
     */
    public final void preFlush(ChannelHandlerContext ctx) throws Exception {
        super.flush(ctx);
    }

    //**********************************           free combined-method start              **************************************//

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
     * @param msg the msg
     * @throws Exception the exception
     */
    protected void freeAndRead(ChannelHandlerContext ctx, T msg) throws Exception {
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

    //**********************************           free combined-method end             **************************************//

    //**********************************           reset combined-method start          **************************************//

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
    protected void  resetAndUnregistered(ChannelHandlerContext ctx) throws Exception {
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
    protected void resetAndRead(ChannelHandlerContext ctx, T msg) throws Exception {
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

    //**********************************           reset combined-method end           **************************************//

    /**
     * tool class used with ChannelInterceptor
     *
     * @see ChannelInterceptor
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ChannelInterceptors {

        /**
         * Gets interceptors.
         *
         * @param <T>     the type parameter
         * @param channel the channel
         * @return the interceptors
         */
        public static <T extends ChannelInterceptor<?>> List<T> getInterceptors(Channel channel) {
            return getInterceptors(channel.pipeline());
        }

        /**
         * Gets interceptors.
         *
         * @param <T> the type parameter
         * @param ctx the ctx
         * @return the interceptors
         */
        public static <T extends ChannelInterceptor<?>> List<T> getInterceptors(ChannelHandlerContext ctx) {
            return getInterceptors(ctx.pipeline());
        }

        /**
         * Gets interceptors.
         *
         * @param <T>      the type parameter
         * @param pipeline the pipeline
         * @return the interceptors
         */
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
         *
         * @param channel channel
         */
        public static void freeAll(Channel channel) {
            freeAll(channel.pipeline());
        }

        /**
         * reset all channel interceptors in the pipeline
         *
         * @param ctx ctx
         */
        public static void freeAll(ChannelHandlerContext ctx) {
            freeAll(ctx.pipeline());
        }

        /**
         * reset all channel interceptors in the pipeline
         *
         * @param pipeline pipeline
         */
        public static void freeAll(ChannelPipeline pipeline) {
            getInterceptors(pipeline).stream().filter(ChannelInterceptor::isNotFreed).forEach(ChannelInterceptor::free);
        }

        /**
         * reset all channel interceptors in the pipeline
         *
         * @param channel channel
         */
        public static void resetAll(Channel channel) {
            resetAll(channel.pipeline());
        }

        /**
         * reset all channel interceptors in the pipeline
         *
         * @param ctx ctx
         */
        public static void resetAll(ChannelHandlerContext ctx) {
            resetAll(ctx.pipeline());
        }

        /**
         * reset all channel interceptors in the pipeline
         *
         * @param pipeline pipeline
         */
        public static void resetAll(ChannelPipeline pipeline) {
            getInterceptors(pipeline).stream().filter(ChannelInterceptor::isFreed).forEach(ChannelInterceptor::reset);
        }
    }
}
