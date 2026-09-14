package org.fz.nettyx.action;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ActionsTest {

    @Test
    public void invokesEachActionWithOriginalArguments() {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        ChannelHandlerContext ctx = channel.pipeline().firstContext();
        ChannelPromise promise = channel.newPromise();
        SocketAddress local = new InetSocketAddress(10001);
        SocketAddress remote = new InetSocketAddress(10002);
        Object message = new Object();
        Throwable error = new IllegalStateException("test");
        AtomicInteger calls = new AtomicInteger();

        Actions.invokeAction((ChannelHandlerContextAction) actual -> {
            assertSame(ctx, actual);
            calls.incrementAndGet();
        }, ctx);
        Actions.invokeAction((ChannelBindAction) (actualCtx, actualAddress, actualPromise) -> {
            assertSame(ctx, actualCtx);
            assertSame(local, actualAddress);
            assertSame(promise, actualPromise);
            calls.incrementAndGet();
        }, ctx, local, promise);
        Actions.invokeAction((ChannelConnectAction) (actualCtx, actualRemote, actualLocal, actualPromise) -> {
            assertSame(ctx, actualCtx);
            assertSame(remote, actualRemote);
            assertSame(local, actualLocal);
            assertSame(promise, actualPromise);
            calls.incrementAndGet();
        }, ctx, remote, local, promise);
        Actions.invokeAction((ChannelPromiseAction) (actualCtx, actualPromise) -> {
            assertSame(ctx, actualCtx);
            assertSame(promise, actualPromise);
            calls.incrementAndGet();
        }, ctx, promise);
        Actions.invokeAction((ChannelWriteAction) (actualCtx, actualMessage, actualPromise) -> {
            assertSame(ctx, actualCtx);
            assertSame(message, actualMessage);
            assertSame(promise, actualPromise);
            calls.incrementAndGet();
        }, ctx, message, promise);
        Actions.invokeAction((ChannelFutureAction) actual -> {
            assertSame(promise, actual);
            calls.incrementAndGet();
        }, promise);
        Actions.invokeAction((ChannelReadAction) (actualCtx, actualMessage) -> {
            assertSame(ctx, actualCtx);
            assertSame(message, actualMessage);
            calls.incrementAndGet();
        }, ctx, message);
        Actions.invokeAction((ChannelExceptionAction) (actualCtx, actualError) -> {
            assertSame(ctx, actualCtx);
            assertSame(error, actualError);
            calls.incrementAndGet();
        }, ctx, error);

        assertEquals(8, calls.get());
        channel.finishAndReleaseAll();
    }

    @Test
    public void nullActionsAreNoOpsAndExceptionActionCanCloseChannel() {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        ChannelHandlerContext ctx = channel.pipeline().firstContext();
        ChannelPromise promise = channel.newPromise();

        Actions.invokeAction((ChannelHandlerContextAction) null, ctx);
        Actions.invokeAction((ChannelPromiseAction) null, ctx, promise);
        Actions.invokeAction((ChannelFutureAction) null, promise);
        Actions.invokeAction((ChannelBindAction) null, ctx, null, promise);
        Actions.invokeAction((ChannelConnectAction) null, ctx, null, null, promise);
        Actions.invokeAction((ChannelWriteAction) null, ctx, new Object(), promise);
        Actions.invokeAction((ChannelReadAction) null, ctx, new Object());
        Actions.invokeAction((ChannelExceptionAction) null, ctx, new RuntimeException());
        Actions.invokeActionAndClose(null, ctx, new RuntimeException());

        assertFalse(channel.isOpen());
        channel.finishAndReleaseAll();
    }

    @Test
    public void predefinedNoOpActionsCanBeInvoked() {
        EmbeddedChannel channel = new EmbeddedChannel();
        ChannelHandlerContext ctx = channel.pipeline().firstContext();
        ChannelPromise promise = channel.newPromise();
        ChannelAction.DO_NOTHING.act(channel);
        ChannelBindAction.DO_NOTHING.act(ctx, null, promise);
        ChannelConnectAction.DO_NOTHING.act(ctx, null, null, promise);
        ChannelEventAction.DO_NOTHING.act(ctx, "event");
        ChannelExceptionAction.DO_NOTHING.act(ctx, new RuntimeException());
        ChannelFutureAction.DO_NOTHING.act(promise);
        ChannelHandlerContextAction.DO_NOTHING.act(ctx);
        ChannelPromiseAction.DO_NOTHING.act(ctx, promise);
        ChannelReadAction.DO_NOTHING.act(ctx, "message");
        ChannelWriteAction.DO_NOTHING.act(ctx, "message", promise);
        channel.finishAndReleaseAll();
    }
}
