package org.fz.nettyx.event;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.net.SocketAddress;

/**
 * wrap channel event to ChannelEvent, keep channel event could propagate in ApplicationContext and the others
 *
 * @param <S> the type parameter
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /5/13 9:10
 */
@Getter
@RequiredArgsConstructor
public abstract class ChannelEvent<S> {

    private final long                  happenTime = System.currentTimeMillis();
    private       S                     source;
    private final ChannelHandlerContext ctx;

    /**
     * Instantiates a new Channel event.
     *
     * @param source the source
     * @param ctx    the ctx
     */
    protected ChannelEvent(S source, ChannelHandlerContext ctx)
    {
        this.source = source;
        this.ctx    = ctx;
    }

    /**
     * Gets channel.
     *
     * @return the channel
     */
    public Channel getChannel()
    {
        return ctx.channel();
    }

    /**
     * Gets pipeline.
     *
     * @return the pipeline
     */
    public ChannelPipeline getPipeline()
    {
        return ctx.pipeline();
    }

    /**
     * Attr attribute.
     *
     * @param <A>          the type parameter
     * @param attributeKey the attribute key
     * @return the attribute
     */
    public <A> Attribute<A> attr(AttributeKey<A> attributeKey)
    {
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
        public ChannelRegister(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Channel register.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public ChannelRegister(T source, ChannelHandlerContext ctx)
        {
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
        public ChannelUnRegister(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Channel un register.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public ChannelUnRegister(T source, ChannelHandlerContext ctx)
        {
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
        public ChannelActive(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Channel active.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public ChannelActive(T source, ChannelHandlerContext ctx)
        {
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
        public ChannelInactive(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Channel inactive.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public ChannelInactive(T source, ChannelHandlerContext ctx)
        {
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

        /**
         * Instantiates a new Channel read.
         *
         * @param ctx the ctx
         */
        public ChannelRead(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Channel read.
         *
         * @param ctx the ctx
         * @param msg the msg
         */
        public ChannelRead(ChannelHandlerContext ctx, Object msg)
        {
            super(ctx);
            this.msg = msg;
        }

        /**
         * Instantiates a new Channel read.
         *
         * @param source the source
         * @param ctx    the ctx
         * @param msg    the msg
         */
        public ChannelRead(
                T                     source,
                ChannelHandlerContext ctx,
                Object                msg)
        {
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

        /**
         * Instantiates a new Channel read complete.
         *
         * @param ctx the ctx
         */
        public ChannelReadComplete(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Channel read complete.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public ChannelReadComplete(T source, ChannelHandlerContext ctx)
        {
            super(source, ctx);
        }
    }

    /**
     * The type User event triggered.
     *
     * @param <T> the type parameter
     */
    @Getter
    public static class UserEventTriggered<T> extends ChannelEvent<T> {

        private Object event;

        /**
         * Instantiates a new User event triggered.
         *
         * @param ctx the ctx
         */
        public UserEventTriggered(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new User event triggered.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public UserEventTriggered(T source, ChannelHandlerContext ctx)
        {
            super(source, ctx);
        }

        /**
         * Instantiates a new User event triggered.
         *
         * @param ctx   the ctx
         * @param event the event
         */
        public UserEventTriggered(ChannelHandlerContext ctx, Object event)
        {
            super(ctx);
            this.event = event;
        }

        /**
         * Instantiates a new User event triggered.
         *
         * @param source the source
         * @param ctx    the ctx
         * @param event  the event
         */
        public UserEventTriggered(
                T                     source,
                ChannelHandlerContext ctx,
                Object                event)
        {
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

        /**
         * Instantiates a new Writability changed.
         *
         * @param ctx the ctx
         */
        public WritabilityChanged(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Writability changed.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public WritabilityChanged(T source, ChannelHandlerContext ctx)
        {
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

        /**
         * Instantiates a new Exception caught.
         *
         * @param ctx the ctx
         */
        public ExceptionCaught(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Exception caught.
         *
         * @param ctx       the ctx
         * @param throwable the throwable
         */
        public ExceptionCaught(ChannelHandlerContext ctx, Throwable throwable)
        {
            super(ctx);
            this.throwable = throwable;
        }

        /**
         * Instantiates a new Exception caught.
         *
         * @param source    the source
         * @param ctx       the ctx
         * @param throwable the throwable
         */
        public ExceptionCaught(
                T                     source,
                ChannelHandlerContext ctx,
                Throwable             throwable)
        {
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

        /**
         * Instantiates a new Read idle.
         *
         * @param ctx the ctx
         */
        public ReadIdle(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Read idle.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public ReadIdle(T source, ChannelHandlerContext ctx)
        {
            super(source, ctx);
        }
    }

    /**
     * The type Write idle
     *
     * @param <T> the type parameter
     */
    public static class WriteIdle<T> extends ChannelEvent<T> {

        /**
         * Instantiates a new Write idle.
         *
         * @param ctx the ctx
         */
        public WriteIdle(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Write idle.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public WriteIdle(T source, ChannelHandlerContext ctx)
        {
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

        /**
         * Instantiates a new Bind.
         *
         * @param ctx           the ctx
         * @param socketAddress the socket address
         */
        public Bind(ChannelHandlerContext ctx, SocketAddress socketAddress)
        {
            super(ctx);
            this.socketAddress = socketAddress;
        }

        /**
         * Instantiates a new Bind.
         *
         * @param ctx            the ctx
         * @param socketAddress  the socket address
         * @param channelPromise the channel promise
         */
        public Bind(
                ChannelHandlerContext ctx,
                SocketAddress         socketAddress,
                ChannelPromise        channelPromise)
        {
            super(ctx);
            this.socketAddress  = socketAddress;
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Bind.
         *
         * @param source         the source
         * @param ctx            the ctx
         * @param socketAddress  the socket address
         * @param channelPromise the channel promise
         */
        public Bind(
                T                     source,
                ChannelHandlerContext ctx,
                SocketAddress         socketAddress,
                ChannelPromise        channelPromise)
        {
            super(source, ctx);
            this.socketAddress  = socketAddress;
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Bind.
         *
         * @param ctx            the ctx
         * @param channelPromise the channel promise
         */
        public Bind(ChannelHandlerContext ctx, ChannelPromise channelPromise)
        {
            super(ctx);
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Bind.
         *
         * @param source         the source
         * @param ctx            the ctx
         * @param channelPromise the channel promise
         */
        public Bind(
                T                     source,
                ChannelHandlerContext ctx,
                ChannelPromise        channelPromise)
        {
            super(source, ctx);
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Bind.
         *
         * @param source        the source
         * @param ctx           the ctx
         * @param socketAddress the socket address
         */
        public Bind(
                T                     source,
                ChannelHandlerContext ctx,
                SocketAddress         socketAddress)
        {
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

        private SocketAddress localAddress, remoteAddress;
        private ChannelPromise channelPromise;

        /**
         * Instantiates a new Connect.
         *
         * @param ctx           the ctx
         * @param localAddress  the local address
         * @param remoteAddress the remote address
         */
        public Connect(
                ChannelHandlerContext ctx,
                SocketAddress         localAddress,
                SocketAddress         remoteAddress)
        {
            super(ctx);
            this.localAddress  = localAddress;
            this.remoteAddress = remoteAddress;
        }

        /**
         * Instantiates a new Connect.
         *
         * @param source        the source
         * @param ctx           the ctx
         * @param localAddress  the local address
         * @param remoteAddress the remote address
         */
        public Connect(
                T                     source,
                ChannelHandlerContext ctx,
                SocketAddress         localAddress,
                SocketAddress         remoteAddress)
        {
            super(source, ctx);
            this.localAddress  = localAddress;
            this.remoteAddress = remoteAddress;
        }

        /**
         * Instantiates a new Connect.
         *
         * @param ctx            the ctx
         * @param channelPromise the channel promise
         */
        public Connect(ChannelHandlerContext ctx, ChannelPromise channelPromise)
        {
            super(ctx);
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Connect.
         *
         * @param ctx            the ctx
         * @param localAddress   the local address
         * @param remoteAddress  the remote address
         * @param channelPromise the channel promise
         */
        public Connect(
                ChannelHandlerContext ctx,
                SocketAddress         localAddress,
                SocketAddress         remoteAddress,
                ChannelPromise        channelPromise)
        {
            super(ctx);
            this.localAddress   = localAddress;
            this.remoteAddress  = remoteAddress;
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Connect.
         *
         * @param source         the source
         * @param ctx            the ctx
         * @param localAddress   the local address
         * @param remoteAddress  the remote address
         * @param channelPromise the channel promise
         */
        public Connect(
                T                     source,
                ChannelHandlerContext ctx,
                SocketAddress         localAddress,
                SocketAddress         remoteAddress,
                ChannelPromise        channelPromise)
        {
            super(source, ctx);
            this.localAddress   = localAddress;
            this.remoteAddress  = remoteAddress;
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Connect.
         *
         * @param source         the source
         * @param ctx            the ctx
         * @param channelPromise the channel promise
         */
        public Connect(
                T                     source,
                ChannelHandlerContext ctx,
                ChannelPromise        channelPromise)
        {
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

        /**
         * Instantiates a new Disconnect.
         *
         * @param ctx            the ctx
         * @param channelPromise the channel promise
         */
        public Disconnect(ChannelHandlerContext ctx, ChannelPromise channelPromise)
        {
            super(ctx);
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Disconnect.
         *
         * @param source         the source
         * @param ctx            the ctx
         * @param channelPromise the channel promise
         */
        public Disconnect(
                T                     source,
                ChannelHandlerContext ctx,
                ChannelPromise        channelPromise)
        {
            super(source, ctx);
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Disconnect.
         *
         * @param ctx the ctx
         */
        public Disconnect(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Disconnect.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public Disconnect(T source, ChannelHandlerContext ctx)
        {
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

        /**
         * Instantiates a new Close.
         *
         * @param ctx            the ctx
         * @param channelPromise the channel promise
         */
        public Close(ChannelHandlerContext ctx, ChannelPromise channelPromise)
        {
            super(ctx);
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Close.
         *
         * @param source         the source
         * @param ctx            the ctx
         * @param channelPromise the channel promise
         */
        public Close(
                T                     source,
                ChannelHandlerContext ctx,
                ChannelPromise        channelPromise)
        {
            super(source, ctx);
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Close.
         *
         * @param ctx the ctx
         */
        public Close(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Close.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public Close(T source, ChannelHandlerContext ctx)
        {
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

        /**
         * Instantiates a new Deregister.
         *
         * @param ctx            the ctx
         * @param channelPromise the channel promise
         */
        public Deregister(ChannelHandlerContext ctx, ChannelPromise channelPromise)
        {
            super(ctx);
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Deregister.
         *
         * @param source         the source
         * @param ctx            the ctx
         * @param channelPromise the channel promise
         */
        public Deregister(
                T                     source,
                ChannelHandlerContext ctx,
                ChannelPromise        channelPromise)
        {
            super(source, ctx);
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Deregister.
         *
         * @param ctx the ctx
         */
        public Deregister(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Deregister.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public Deregister(T source, ChannelHandlerContext ctx)
        {
            super(source, ctx);
        }
    }

    /**
     * The type Read.
     *
     * @param <T> the type parameter
     */
    public static class ReadInWrite<T> extends ChannelEvent<T> {

        /**
         * Instantiates a new Read in write.
         *
         * @param ctx the ctx
         */
        public ReadInWrite(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Read in write.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public ReadInWrite(T source, ChannelHandlerContext ctx)
        {
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

        /**
         * Instantiates a new Write.
         *
         * @param ctx the ctx
         * @param msg the msg
         */
        public Write(ChannelHandlerContext ctx, Object msg)
        {
            super(ctx);
            this.msg = msg;
        }

        /**
         * Instantiates a new Write.
         *
         * @param source the source
         * @param ctx    the ctx
         * @param msg    the msg
         */
        public Write(
                T                     source,
                ChannelHandlerContext ctx,
                Object                msg)
        {
            super(source, ctx);
            this.msg = msg;
        }

        /**
         * Instantiates a new Write.
         *
         * @param ctx            the ctx
         * @param channelPromise the channel promise
         */
        public Write(ChannelHandlerContext ctx, ChannelPromise channelPromise)
        {
            super(ctx);
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Write.
         *
         * @param source         the source
         * @param ctx            the ctx
         * @param channelPromise the channel promise
         */
        public Write(
                T                     source,
                ChannelHandlerContext ctx,
                ChannelPromise        channelPromise)
        {
            super(source, ctx);
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Write.
         *
         * @param ctx            the ctx
         * @param msg            the msg
         * @param channelPromise the channel promise
         */
        public Write(
                ChannelHandlerContext ctx,
                Object                msg,
                ChannelPromise        channelPromise)
        {
            super(ctx);
            this.msg            = msg;
            this.channelPromise = channelPromise;
        }

        /**
         * Instantiates a new Write.
         *
         * @param source         the source
         * @param ctx            the ctx
         * @param msg            the msg
         * @param channelPromise the channel promise
         */
        public Write(
                T                     source,
                ChannelHandlerContext ctx,
                Object                msg,
                ChannelPromise        channelPromise)
        {
            super(source, ctx);
            this.msg            = msg;
            this.channelPromise = channelPromise;
        }
    }

    /**
     * The type Flush.
     *
     * @param <T> the type parameter
     */
    public static class Flush<T> extends ChannelEvent<T> {

        /**
         * Instantiates a new Flush.
         *
         * @param ctx the ctx
         */
        public Flush(ChannelHandlerContext ctx)
        {
            super(ctx);
        }

        /**
         * Instantiates a new Flush.
         *
         * @param source the source
         * @param ctx    the ctx
         */
        public Flush(T source, ChannelHandlerContext ctx)
        {
            super(source, ctx);
        }
    }

}
