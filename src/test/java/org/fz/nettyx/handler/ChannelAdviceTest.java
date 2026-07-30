package org.fz.nettyx.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.fz.nettyx.handler.ChannelAdvice.InboundAdvice;
import org.fz.nettyx.handler.ChannelAdvice.OutboundAdvice;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class ChannelAdviceTest {

    @Test
    public void testInboundAdviceConstruction() {
        EmbeddedChannel channel = new EmbeddedChannel();
        InboundAdvice advice = new InboundAdvice(channel);
        assertNotNull(advice);
        assertFalse(channel.finish());
    }

    @Test
    public void testOutboundAdviceConstruction() {
        EmbeddedChannel channel = new EmbeddedChannel();
        OutboundAdvice advice = new OutboundAdvice(channel);
        assertNotNull(advice);
        assertFalse(channel.finish());
    }

    @Test
    public void testInboundAdviceCanBeAddedToPipeline() {
        EmbeddedChannel channel = new EmbeddedChannel();
        InboundAdvice advice = new InboundAdvice(channel);
        channel.pipeline().addFirst(advice);
        channel.writeInbound("test");
        assertEquals("test", channel.readInbound());
        assertFalse(channel.finish());
    }

    @Test
    public void inboundAdviceInvokesAllConfiguredActions() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel();
        AtomicInteger calls = new AtomicInteger();
        InboundAdvice advice = new InboundAdvice(channel)
                .whenChannelRegister(ctx -> calls.incrementAndGet())
                .whenChannelUnRegister(ctx -> calls.incrementAndGet())
                .whenChannelActive(ctx -> calls.incrementAndGet())
                .whenChannelInactive(ctx -> calls.incrementAndGet())
                .whenChannelRead((ctx, msg) -> calls.incrementAndGet())
                .whenChannelReadComplete(ctx -> calls.incrementAndGet())
                .whenWritabilityChanged(ctx -> calls.incrementAndGet())
                .whenExceptionCaught((ctx, error) -> calls.incrementAndGet());
        channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {}
        });
        channel.pipeline().addFirst("advice", advice);
        ChannelHandlerContext ctx = channel.pipeline().context(advice);

        advice.channelRegistered(ctx);
        advice.channelUnregistered(ctx);
        advice.channelActive(ctx);
        advice.channelInactive(ctx);
        advice.channelRead(ctx, "message");
        advice.channelReadComplete(ctx);
        advice.channelWritabilityChanged(ctx);
        advice.exceptionCaught(ctx, new IllegalStateException("test"));

        assertEquals(8, calls.get());
        assertSame(advice, advice.whenReadIdle(10, ignored -> {})
                                 .whenReadTimeout(10, (ignored, error) -> {}));
        assertSame(advice, advice.whenReadTimeout(10, false, (ignored, error) -> {}));
        channel.finishAndReleaseAll();
    }

    @Test
    public void outboundAdviceInvokesAllConfiguredActions() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelOutboundHandlerAdapter());
        AtomicInteger calls = new AtomicInteger();
        OutboundAdvice advice = new OutboundAdvice(channel)
                .whenBind((ctx, address, promise) -> calls.incrementAndGet())
                .whenConnect((ctx, remote, local, promise) -> calls.incrementAndGet())
                .whenDisconnect((ctx, promise) -> calls.incrementAndGet())
                .whenClose((ctx, promise) -> calls.incrementAndGet())
                .whenDeregister((ctx, promise) -> calls.incrementAndGet())
                .whenRead(ctx -> calls.incrementAndGet())
                .whenWrite((ctx, msg, promise) -> calls.incrementAndGet())
                .whenFlush(ctx -> calls.incrementAndGet());
        channel.pipeline().addLast("advice", advice);
        ChannelHandlerContext ctx = channel.pipeline().context(advice);
        InetSocketAddress address = new InetSocketAddress(10001);

        advice.bind(ctx, address, channel.newPromise());
        advice.connect(ctx, address, address, channel.newPromise());
        advice.disconnect(ctx, channel.newPromise());
        advice.close(ctx, channel.newPromise());
        advice.deregister(ctx, channel.newPromise());
        advice.read(ctx);
        advice.write(ctx, "message", channel.newPromise());
        advice.flush(ctx);

        assertEquals(8, calls.get());
        assertSame(advice, advice.whenWriteIdle(10, ignored -> {}));
        assertSame(advice, advice.whenWriteTimeout(10, false, (ignored, error) -> {}));
        assertThrows(UnsupportedOperationException.class,
                     () -> advice.whenReadTimeout(10, (ignored, error) -> {}));
        channel.finishAndReleaseAll();
    }

    @Test
    public void outboundFailureListenerOnlyInvokesActionForFailure() throws Exception {
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelOutboundHandlerAdapter());
        ChannelHandlerContext ctx = channel.pipeline().firstContext();
        AtomicInteger failures = new AtomicInteger();

        OutboundAdvice.SimpleOutboundExceptionHandler.failureListener(ctx,
                (actual, error) -> failures.incrementAndGet()).operationComplete(channel.newSucceededFuture());
        OutboundAdvice.SimpleOutboundExceptionHandler.failureListener(ctx,
                (actual, error) -> failures.incrementAndGet()).operationComplete(
                channel.newFailedFuture(new IllegalStateException("test")));

        assertEquals(1, failures.get());
        assertNotNull(new OutboundAdvice.SimpleOutboundExceptionHandler());
        assertNotNull(new OutboundAdvice.SimpleOutboundExceptionHandler((actual, error) -> {}));
        channel.finishAndReleaseAll();
    }
}
