package org.fz.nettyx.event;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.util.AttributeKey;
import org.junit.Test;

import java.net.InetSocketAddress;

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
}
