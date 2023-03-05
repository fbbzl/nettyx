package org.fz.nettyx.event;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import java.net.SocketAddress;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * wrap channel event to ChannelEvent, keep channel event could propagate in ApplicationContext and the others
 * @author fengbinbin
 * @version 1.0
 * @since 2021/5/13 9:10
 * @param <T> the type parameter
 */
@Getter
@RequiredArgsConstructor
public abstract class ChannelEvent<T> {

    private final long happenTime = System.currentTimeMillis();
    private T source;
    private final ChannelHandlerContext ctx;

    /**
     * Instantiates a new Channel event.
     *
     * @param source the source
     * @param ctx the ctx
     */
    protected ChannelEvent(T source, ChannelHandlerContext ctx) {
        this.source = source;
        this.ctx = ctx;
    }

    /**
     * Gets channel.
     *
     * @return the channel
     */
    public Channel getChannel() {
        return ctx.channel();
    }

    /**
     * Gets pipeline.
     *
     * @return the pipeline
     */
    public ChannelPipeline getPipeline() {
        return ctx.pipeline();
    }

    /**
     * Attr attribute.
     *
     * @param <A> the type parameter
     * @param attributeKey the attribute key
     * @return the attribute
     */
    public <A> Attribute<A> attr(AttributeKey<A> attributeKey) {
        return getChannel().attr(attributeKey);
    }

    /**
     * The type Channel register
     *
     * @param <T> the type parameter
     */
    public static class ChannelRegister<T> extends ChannelEvent<T> {

        /**
         * Instantiates a new Channel register.
         *
         * @param ctx the ctx
         */
        public ChannelRegister(ChannelHandlerContext ctx) {
            super(ctx);
        }

        /**
         * Instantiates a new Channel register.
         *
         * @param source the source
         * @param ctx the ctx
         */
        public ChannelRegister(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Channel un register
     *
     * @param <T> the type parameter
     */
    public static class ChannelUnRegister<T> extends ChannelEvent<T> {

        /**
         * Instantiates a new Channel un register.
         *
         * @param ctx the ctx
         */
        public ChannelUnRegister(ChannelHandlerContext ctx) {
            super(ctx);
        }

        /**
         * Instantiates a new Channel un register.
         *
         * @param source the source
         * @param ctx the ctx
         */
        public ChannelUnRegister(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Channel active
     *
     * @param <T> the type parameter
     */
    public static class ChannelActive<T> extends ChannelEvent<T> {

        /**
         * Instantiates a new Channel active.
         *
         * @param ctx the ctx
         */
        public ChannelActive(ChannelHandlerContext ctx) {
            super(ctx);
        }

        /**
         * Instantiates a new Channel active.
         *
         * @param source the source
         * @param ctx the ctx
         */
        public ChannelActive(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Channel inactive
     *
     * @param <T> the type parameter
     */
    public static class ChannelInactive<T> extends ChannelEvent<T> {

        /**
         * Instantiates a new Channel inactive.
         *
         * @param ctx the ctx
         */
        public ChannelInactive(ChannelHandlerContext ctx) {
            super(ctx);
        }

        /**
         * Instantiates a new Channel inactive.
         *
         * @param source the source
         * @param ctx the ctx
         */
        public ChannelInactive(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Channel read.
     *
     * @param <T> the type parameter
     */
    @Getter
    public static class ChannelRead<T> extends ChannelEvent<T> {

        private Object msg;

        public ChannelRead(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public ChannelRead(ChannelHandlerContext ctx, Object msg) {
            super(ctx);
            this.msg = msg;
        }

        public ChannelRead(T source, ChannelHandlerContext ctx, Object msg) {
            super(source, ctx);
            this.msg = msg;
        }
    }

    /**
     * The type Channel read complete
     *
     * @param <T> the type parameter
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
     * The type User event triggered.
     *
     * @param <T> the type parameter
     */
    @Getter
    public static class UserEventTriggered<T> extends ChannelEvent<T>  {

        private Object event;

        public UserEventTriggered(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public UserEventTriggered(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }

        public UserEventTriggered(ChannelHandlerContext ctx, Object event) {
            super(ctx);
            this.event = event;
        }

        public UserEventTriggered(T source, ChannelHandlerContext ctx, Object event) {
            super(source, ctx);
            this.event = event;
        }
    }

    /**
     * The type Writability changed
     *
     * @param <T> the type parameter
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
     * The type ExceptionCaught.
     *
     * @param <T> the type parameter
     */
    @Getter
    public static class ExceptionCaught<T> extends ChannelEvent<T> {

        private Throwable throwable;

        public ExceptionCaught(ChannelHandlerContext ctx) {
            super(ctx);
        }

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
     * The type Read idle
     *
     * @param <T> the type parameter
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
     *
     * @param <T> the type parameter
     */
    public static class WriteIdle<T> extends ChannelEvent<T> {

        public WriteIdle(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public WriteIdle(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Bind.
     *
     * @param <T> the type parameter
     */
    @Getter
    public static class Bind<T> extends ChannelEvent<T> {

        private SocketAddress  socketAddress;
        private ChannelPromise channelPromise;

        public Bind(ChannelHandlerContext ctx, SocketAddress socketAddress) {
            super(ctx);
            this.socketAddress = socketAddress;
        }

        public Bind(ChannelHandlerContext ctx, SocketAddress socketAddress, ChannelPromise channelPromise) {
            super(ctx);
            this.socketAddress = socketAddress;
            this.channelPromise = channelPromise;
        }

        public Bind(T source, ChannelHandlerContext ctx, SocketAddress socketAddress, ChannelPromise channelPromise) {
            super(source, ctx);
            this.socketAddress = socketAddress;
            this.channelPromise = channelPromise;
        }

        public Bind(ChannelHandlerContext ctx, ChannelPromise channelPromise) {
            super(ctx);
            this.channelPromise = channelPromise;
        }

        public Bind(T source, ChannelHandlerContext ctx, ChannelPromise channelPromise) {
            super(source, ctx);
            this.channelPromise = channelPromise;
        }

        public Bind(T source, ChannelHandlerContext ctx, SocketAddress socketAddress) {
            super(source, ctx);
            this.socketAddress = socketAddress;
        }
    }

    /**
     * The type Connect.
     *
     * @param <T> the type parameter
     */
    @Getter
    public static class Connect<T> extends ChannelEvent<T> {

        private SocketAddress  localAddress, remoteAddress;
        private ChannelPromise channelPromise;

        public Connect(ChannelHandlerContext ctx, SocketAddress localAddress, SocketAddress remoteAddress) {
            super(ctx);
            this.localAddress = localAddress;
            this.remoteAddress = remoteAddress;
        }

        public Connect(T source, ChannelHandlerContext ctx, SocketAddress localAddress, SocketAddress remoteAddress) {
            super(source, ctx);
            this.localAddress = localAddress;
            this.remoteAddress = remoteAddress;
        }

        public Connect(ChannelHandlerContext ctx, ChannelPromise channelPromise) {
            super(ctx);
            this.channelPromise = channelPromise;
        }

        public Connect(ChannelHandlerContext ctx, SocketAddress localAddress, SocketAddress remoteAddress, ChannelPromise channelPromise) {
            super(ctx);
            this.localAddress = localAddress;
            this.remoteAddress = remoteAddress;
            this.channelPromise = channelPromise;
        }

        public Connect(T source, ChannelHandlerContext ctx, SocketAddress localAddress, SocketAddress remoteAddress, ChannelPromise channelPromise) {
            super(source, ctx);
            this.localAddress = localAddress;
            this.remoteAddress = remoteAddress;
            this.channelPromise = channelPromise;
        }

        public Connect(T source, ChannelHandlerContext ctx, ChannelPromise channelPromise) {
            super(source, ctx);
            this.channelPromise = channelPromise;
        }
    }

    /**
     * The type Disconnect.
     *
     * @param <T> the type parameter
     */
    @Getter
    public static class Disconnect<T> extends ChannelEvent<T> {

        private ChannelPromise channelPromise;

        public Disconnect(ChannelHandlerContext ctx, ChannelPromise channelPromise) {
            super(ctx);
            this.channelPromise = channelPromise;
        }

        public Disconnect(T source, ChannelHandlerContext ctx, ChannelPromise channelPromise) {
            super(source, ctx);
            this.channelPromise = channelPromise;
        }

        public Disconnect(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public Disconnect(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Close.
     *
     * @param <T> the type parameter
     */
    @Getter
    public static class Close<T> extends ChannelEvent<T> {

        private ChannelPromise channelPromise;

        public Close(ChannelHandlerContext ctx, ChannelPromise channelPromise) {
            super(ctx);
            this.channelPromise = channelPromise;
        }

        public Close(T source, ChannelHandlerContext ctx, ChannelPromise channelPromise) {
            super(source, ctx);
            this.channelPromise = channelPromise;
        }

        public Close(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public Close(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Deregister.
     *
     * @param <T> the type parameter
     */
    @Getter
    public static class Deregister<T> extends ChannelEvent<T> {

        private ChannelPromise channelPromise;

        public Deregister(ChannelHandlerContext ctx, ChannelPromise channelPromise) {
            super(ctx);
            this.channelPromise = channelPromise;
        }

        public Deregister(T source, ChannelHandlerContext ctx, ChannelPromise channelPromise) {
            super(source, ctx);
            this.channelPromise = channelPromise;
        }

        public Deregister(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public Deregister(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Read.
     *
     * @param <T> the type parameter
     */
    public static class ReadInWrite<T> extends ChannelEvent<T> {

        public ReadInWrite(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public ReadInWrite(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

    /**
     * The type Write.
     *
     * @param <T> the type parameter
     */
    @Getter
    public static class Write<T> extends ChannelEvent<T> {

        private Object         msg;
        private ChannelPromise channelPromise;

        public Write(ChannelHandlerContext ctx, Object msg) {
            super(ctx);
            this.msg = msg;
        }

        public Write(T source, ChannelHandlerContext ctx, Object msg) {
            super(source, ctx);
            this.msg = msg;
        }

        public Write(ChannelHandlerContext ctx, ChannelPromise channelPromise) {
            super(ctx);
            this.channelPromise = channelPromise;
        }

        public Write(T source, ChannelHandlerContext ctx, ChannelPromise channelPromise) {
            super(source, ctx);
            this.channelPromise = channelPromise;
        }

        public Write(ChannelHandlerContext ctx, Object msg, ChannelPromise channelPromise) {
            super(ctx);
            this.msg = msg;
            this.channelPromise = channelPromise;
        }

        public Write(T source, ChannelHandlerContext ctx, Object msg, ChannelPromise channelPromise) {
            super(source, ctx);
            this.msg = msg;
            this.channelPromise = channelPromise;
        }
    }

    /**
     * The type Flush.
     *
     * @param <T> the type parameter
     */
    public static class Flush<T> extends ChannelEvent<T> {

        public Flush(ChannelHandlerContext ctx) {
            super(ctx);
        }

        public Flush(T source, ChannelHandlerContext ctx) {
            super(source, ctx);
        }
    }

}
