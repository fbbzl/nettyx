package org.fz.nettyx.action;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;
import java.util.function.Consumer;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:46
 */
public interface Actions {

    private static <T> void invokeIfNotNull(T action, Consumer<T> invoker)
    {
        if (action != null) invoker.accept(action);
    }

    /**
     * Act.
     *
     * @param channelAction the channel action
     * @param ctx           the ctx
     */
    static void invokeAction(
            ChannelHandlerContextAction channelAction,
            ChannelHandlerContext       ctx)
    {
        invokeIfNotNull(channelAction, a -> a.act(ctx));
    }

    /**
     * Act.
     *
     * @param channelBindAction the channel bind action
     * @param ctx               the ctx
     * @param localAddress      the local address
     * @param promise           the promise
     */
    static void invokeAction(
            ChannelBindAction     channelBindAction,
            ChannelHandlerContext ctx,
            SocketAddress         localAddress,
            ChannelPromise        promise)
    {
        invokeIfNotNull(channelBindAction, a -> a.act(ctx, localAddress, promise));
    }

    /**
     * Act.
     *
     * @param channelConnectAction the channel connect action
     * @param ctx                  the ctx
     * @param remoteAddress        the remote address
     * @param localAddress         the local address
     * @param promise              the promise
     */
    static void invokeAction(
            ChannelConnectAction  channelConnectAction,
            ChannelHandlerContext ctx,
            SocketAddress         remoteAddress,
            SocketAddress         localAddress,
            ChannelPromise        promise)
    {
        invokeIfNotNull(channelConnectAction, a -> a.act(ctx, remoteAddress, localAddress, promise));
    }

    /**
     * Act.
     *
     * @param channelPromiseAction the channel promise action
     * @param ctx                  the ctx
     * @param promise              the promise
     */
    static void invokeAction(
            ChannelPromiseAction  channelPromiseAction,
            ChannelHandlerContext ctx,
            ChannelPromise        promise)
    {
        invokeIfNotNull(channelPromiseAction, a -> a.act(ctx, promise));
    }

    /**
     * Act.
     *
     * @param channelWriteAction the channel write action
     * @param ctx                the ctx
     * @param msg                the msg
     * @param promise            the promise
     */
    static void invokeAction(
            ChannelWriteAction    channelWriteAction,
            ChannelHandlerContext ctx,
            Object                msg,
            ChannelPromise        promise)
    {
        invokeIfNotNull(channelWriteAction, a -> a.act(ctx, msg, promise));
    }

    static void invokeAction(
            ChannelFutureAction channelFutureAction,
            ChannelFuture       cf)
    {
        invokeIfNotNull(channelFutureAction, a -> a.act(cf));
    }

    /**
     * Act.
     *
     * @param channelReadAction the channel read action
     * @param ctx               the ctx
     * @param msg               the msg
     */
    static void invokeAction(
            ChannelReadAction     channelReadAction,
            ChannelHandlerContext ctx,
            Object                msg)
    {
        invokeIfNotNull(channelReadAction, a -> a.act(ctx, msg));
    }

    static void invokeAction(
            ChannelExceptionAction exceptionAction,
            ChannelHandlerContext  ctx,
            Throwable              throwable)
    {
        invokeIfNotNull(exceptionAction, a -> a.act(ctx, throwable));
    }

    static void invokeActionAndClose(
            ChannelExceptionAction exceptionAction,
            ChannelHandlerContext  ctx,
            Throwable              cause)
    {
        invokeAction(exceptionAction, ctx, cause);
        ctx.channel().close();
    }

}
