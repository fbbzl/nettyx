package org.fz.nettyx.event;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * wrap channel event to ChannelEvent, keep channel event could propagate in ApplicationContext and the others
 */
@RequiredArgsConstructor
public abstract class ChannelEvent<T> {

    @Getter private final long                  happenTime = System.currentTimeMillis();
    @Getter private T                           source;
    @Getter private final ChannelHandlerContext ctx;

    ChannelEvent(T source, ChannelHandlerContext ctx) {
        this.source = source;
        this.ctx = ctx;
    }

    public Channel getChannel() {
        return ctx.channel();
    }

    public ChannelPipeline getPipeline() {
        return ctx.pipeline();
    }

    public <A> Attribute<A> attr(AttributeKey<A> attributeKey) {
        return getChannel().attr(attributeKey);
    }

    /**
     * The type Channel register
     */
    public static class ChannelRegister<T> extends ChannelEvent<T> {

        public ChannelRegister(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public ChannelRegister(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Channel un register
     */
    public static class ChannelUnRegister<T> extends ChannelEvent<T> {

        public ChannelUnRegister(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public ChannelUnRegister(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Channel active
     */
    public static class ChannelActive<T> extends ChannelEvent<T> {

        public ChannelActive(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public ChannelActive(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Channel inactive
     */
    public static class ChannelInactive<T> extends ChannelEvent<T> {

        public ChannelInactive(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public ChannelInactive(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type ExceptionCaught.
     */
    public static class ExceptionCaught<T> extends ChannelEvent<T> {

        @Getter
        private final Throwable throwable;

        public ExceptionCaught(ChannelHandlerContext ctx, Throwable throwable) {
            super(ctx);
            this.throwable = throwable;
        }

        public ExceptionCaught(T source, ChannelHandlerContext ctx, Throwable throwable) {
            super(source, ctx);
            this.throwable = throwable;
        }
    }

    /**
     * The type Writability changed
     */
    public static class WritabilityChanged<T> extends ChannelEvent<T> {

        public WritabilityChanged(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public WritabilityChanged(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Channel read complete
     */
    public static class ChannelReadComplete<T> extends ChannelEvent<T> {

        public ChannelReadComplete(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public ChannelReadComplete(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Read idle
     */
    public static class ReadIdle<T> extends ChannelEvent<T> {

        public ReadIdle(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public ReadIdle(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Write idle
     */
    public static class WriteIdle<T> extends ChannelEvent<T> {

        public WriteIdle(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public WriteIdle(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

}
