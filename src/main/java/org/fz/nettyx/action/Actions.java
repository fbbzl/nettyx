package org.fz.nettyx.action;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

import java.net.SocketAddress;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:46
 */
public interface Actions {
    /**
     * Act.
     *
     * @param channelAction the channel action
     * @param ctx           the ctx
     */
     static void invokeAction(ChannelHandlerContextAction channelAction, ChannelHandlerContext ctx) {
        if (channelAction != null) channelAction.act(ctx);
    }

    /**
     * Act.
     *
     * @param channelBindAction the channel bind action
     * @param ctx               the ctx
     * @param localAddress      the local address
     * @param promise           the promise
     */
     static void invokeAction(ChannelBindAction channelBindAction, ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
        if (channelBindAction != null) channelBindAction.act(ctx, localAddress, promise);
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
     static void invokeAction(ChannelConnectAction channelConnectAction, ChannelHandlerContext ctx, SocketAddress remoteAddress, SocketAddress localAddress,
        ChannelPromise promise) {
        if (channelConnectAction != null) channelConnectAction.act(ctx, remoteAddress, localAddress, promise);
    }

    /**
     * Act.
     *
     * @param channelPromiseAction the channel promise action
     * @param ctx                  the ctx
     * @param promise              the promise
     */
     static void invokeAction(ChannelPromiseAction channelPromiseAction, ChannelHandlerContext ctx, ChannelPromise promise) {
        if (channelPromiseAction != null) channelPromiseAction.act(ctx, promise);
    }

    /**
     * Act.
     *
     * @param channelWriteAction the channel write action
     * @param ctx                the ctx
     * @param msg                the msg
     * @param promise            the promise
     */
     static void invokeAction(ChannelWriteAction channelWriteAction, ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        if (channelWriteAction != null) channelWriteAction.act(ctx, msg, promise);
    }

    static void invokeAction(ChannelFutureAction channelFutureAction, ChannelFuture channelFuture) {
        if (channelFutureAction != null) channelFutureAction.act(channelFuture);
    }

    /**
     * Act.
     *
     * @param channelReadAction the channel read action
     * @param ctx               the ctx
     * @param msg               the msg
     */
     static void invokeAction(ChannelReadAction channelReadAction, ChannelHandlerContext ctx, Object msg) {
        if (channelReadAction != null) channelReadAction.act(ctx, msg);
    }

    static void invokeAction(ChannelExceptionAction exceptionAction, ChannelHandlerContext ctx, Throwable throwable) {
        if (exceptionAction != null) exceptionAction.act(ctx, throwable);
    }

    static void invokeActionAndClose(ChannelExceptionAction exceptionAction, ChannelHandlerContext ctx, Throwable cause) {
        invokeAction(exceptionAction, ctx, cause);
        ctx.channel().close();
    }

}
