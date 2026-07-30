package org.fz.nettyx.event;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.AttributeKey;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ChannelEventTest {

    @Test
    public void baseEventExposesContextChannelPipelineAndAttributes() {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        ChannelHandlerContext ctx = channel.pipeline().firstContext();
        AttributeKey<String> key = AttributeKey.valueOf("event-test");
        channel.attr(key).set("value");

        ChannelEvent.ChannelActive<String> event = new ChannelEvent.ChannelActive<>("source", ctx);

        assertEquals("source", event.getSource());
        assertSame(ctx, event.getCtx());
        assertSame(channel, event.getChannel());
        assertSame(channel.pipeline(), event.getPipeline());
        assertEquals("value", event.attr(key).get());
        assertTrue(event.getHappenTime() > 0);
        channel.finishAndReleaseAll();
    }

    @Test
    public void payloadEventsRetainPayloadPromiseAndAddresses() {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        ChannelHandlerContext ctx = channel.pipeline().firstContext();
        ChannelPromise promise = channel.newPromise();
        Object payload = new Object();
        RuntimeException error = new RuntimeException("test");
        InetSocketAddress local = new InetSocketAddress(10001);
        InetSocketAddress remote = new InetSocketAddress(10002);

        assertSame(payload, new ChannelEvent.ChannelRead<>(ctx, payload).getMsg());
        assertSame(payload, new ChannelEvent.Write<>(ctx, payload, promise).getMsg());
        assertSame(promise, new ChannelEvent.Write<>(ctx, payload, promise).getChannelPromise());
        assertSame(error, new ChannelEvent.ExceptionCaught<>(ctx, error).getThrowable());
        assertEquals("idle", new ChannelEvent.UserEventTriggered<>(ctx, "idle").getEvent());

        ChannelEvent.Connect<String> connect =
                new ChannelEvent.Connect<>("source", ctx, local, remote, promise);
        assertSame(local, connect.getLocalAddress());
        assertSame(remote, connect.getRemoteAddress());
        assertSame(promise, connect.getChannelPromise());
        assertNotNull(new ChannelEvent.Disconnect<>(ctx, promise).getChannelPromise());
        assertNotNull(new ChannelEvent.Close<>(ctx, promise).getChannelPromise());
        assertNotNull(new ChannelEvent.Deregister<>(ctx, promise).getChannelPromise());
        channel.finishAndReleaseAll();
    }

    @Test
    public void everyEventConstructorRetainsContext() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        ChannelHandlerContext ctx = channel.pipeline().firstContext();
        ChannelPromise promise = channel.newPromise();
        SocketAddress address = new InetSocketAddress(10003);
        Throwable error = new IllegalStateException("test");
        Object payload = new Object();
        List<Class<?>> coveredTypes = new ArrayList<>();

        for (Class<?> eventType : ChannelEvent.class.getDeclaredClasses()) {
            if (!ChannelEvent.class.isAssignableFrom(eventType)) continue;
            coveredTypes.add(eventType);
            for (Constructor<?> constructor : eventType.getConstructors()) {
                Object[] arguments = new Object[constructor.getParameterCount()];
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                for (int i = 0; i < parameterTypes.length; i++) {
                    arguments[i] = argument(parameterTypes[i], ctx, promise, address, error, payload);
                }

                ChannelEvent<?> event = (ChannelEvent<?>) constructor.newInstance(arguments);
                assertSame(eventType.getName(), ctx, event.getCtx());
            }
        }

        assertFalse(coveredTypes.isEmpty());
        channel.finishAndReleaseAll();
    }

    private static Object argument(
            Class<?> type,
            ChannelHandlerContext ctx,
            ChannelPromise promise,
            SocketAddress address,
            Throwable error,
            Object payload) {
        if (type == ChannelHandlerContext.class) return ctx;
        if (type == ChannelPromise.class) return promise;
        if (type == SocketAddress.class) return address;
        if (type == Throwable.class) return error;
        return payload;
    }
}
