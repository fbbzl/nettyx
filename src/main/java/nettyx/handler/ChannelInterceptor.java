package nettyx.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.util.ReferenceCountUtil;
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
public abstract class ChannelInterceptor<T> extends ChannelInboundHandlerAdapter {

    private final AtomicBoolean freeStatus = new AtomicBoolean(false);

    // getter and setter of freeStatus
    private boolean freed() {
        return freeStatus.get();
    }
    private boolean unfreed() {
        return !freeStatus.get();
    }

    /**
     * after use this method, this ChannelInboundHandler will be freed which will no longer participate in message parsing
     *
     * @see ChannelInterceptor#reset() use this to recover this handler
     */
    public void free() {
        this.freeStatus.set(true);
    }

    /**
     * after using this method, this ChannelInboundHandler will re participate in message parsing
     *
     * @see ChannelInterceptor#free() use method to free
     */
    public void reset() {
        this.freeStatus.set(false);
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

    /**
     * Channel registered.
     *
     * @param ctx the ctx
     */
    protected void preChannelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    /**
     * Channel unregistered.
     *
     * @param ctx the ctx
     */
    protected void preChannelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
    }

    /**
     * Channel active.
     *
     * @param ctx the ctx
     */
    protected void preChannelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    /**
     * Channel inactive.
     *
     * @param ctx the ctx
     */
    protected void preChannelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    /**
     * Channel read.
     *
     * @param ctx the ctx
     * @param msg the msg
     */
    protected void preChannelRead(ChannelHandlerContext ctx, T msg) throws Exception {
        super.channelRead(ctx, msg);
    }

    /**
     * Channel read complete.
     *
     * @param ctx the ctx
     */
    protected void preChannelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    /**
     * User event triggered.
     *
     * @param ctx the ctx
     * @param evt the evt
     */
    protected void preUserEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
    }

    /**
     * Channel writability changed.
     *
     * @param ctx the ctx
     */
    protected void preChannelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
        super.channelWritabilityChanged(ctx);
    }

    /**
     * Exception caught.
     *
     * @param ctx the ctx
     * @param cause the cause
     */
    protected void preExceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
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
            getInterceptors(pipeline).stream().filter(ChannelInterceptor::unfreed).forEach(ChannelInterceptor::reset);
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
