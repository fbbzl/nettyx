package org.fz.nettyx.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;

import static org.junit.Assert.*;

public class ChannelInterceptorTest {

    @Test
    public void defaultInboundAndOutboundInterceptorsPassMessagesThrough() {
        ChannelInterceptor.InboundInterceptor<Object> inbound =
                new ChannelInterceptor.InboundInterceptor<>();
        ChannelInterceptor.OutboundInterceptor outbound =
                new ChannelInterceptor.OutboundInterceptor();
        EmbeddedChannel channel = new EmbeddedChannel(inbound, outbound);

        assertTrue(channel.writeInbound("inbound"));
        assertEquals("inbound", channel.readInbound());
        assertTrue(channel.writeOutbound("outbound"));
        assertEquals("outbound", channel.readOutbound());

        channel.pipeline().fireUserEventTriggered("event");
        channel.pipeline().fireChannelReadComplete();
        channel.pipeline().fireChannelWritabilityChanged();
        channel.flushOutbound();
        channel.finishAndReleaseAll();
    }

    @Test
    public void freeBypassesOverriddenInboundInterceptionAndResetRestoresIt() {
        BlockingInbound interceptor = new BlockingInbound();
        EmbeddedChannel channel = new EmbeddedChannel(interceptor);

        assertFalse(channel.writeInbound("blocked"));
        assertEquals(1, interceptor.interceptedReads);

        interceptor.free();
        assertTrue(interceptor.isFreed());
        assertFalse(interceptor.isNotFreed());
        assertTrue(channel.writeInbound("forwarded"));
        assertEquals("forwarded", channel.readInbound());
        assertEquals(1, interceptor.interceptedReads);

        interceptor.reset();
        assertFalse(channel.writeInbound("blocked-again"));
        assertEquals(2, interceptor.interceptedReads);
        channel.finishAndReleaseAll();
    }

    @Test
    public void lookupAndBulkStateOperationsWorkAcrossPipeline() {
        ChannelInterceptor.InboundInterceptor<Object> inbound =
                new ChannelInterceptor.InboundInterceptor<>();
        ChannelInterceptor.OutboundInterceptor outbound =
                new ChannelInterceptor.OutboundInterceptor();
        EmbeddedChannel channel = new EmbeddedChannel(new ChannelInboundHandlerAdapter());
        channel.pipeline().addFirst("outbound-interceptor", outbound);
        channel.pipeline().addFirst("inbound-interceptor", inbound);
        ChannelHandlerContext context = channel.pipeline().context("inbound-interceptor");

        assertSame(inbound, ChannelInterceptor.getInterceptor(channel.pipeline(), "INBOUND-INTERCEPTOR"));
        assertSame(inbound, ChannelInterceptor.getInterceptor(channel.pipeline(),
                                                               ChannelInterceptor.InboundInterceptor.class));
        assertNull(ChannelInterceptor.getInterceptor(channel.pipeline(), "missing"));

        List<ChannelInterceptor> fromChannel = ChannelInterceptor.getInterceptors(channel);
        List<ChannelInterceptor> fromContext = ChannelInterceptor.getInterceptors(context);
        assertEquals(2, fromChannel.size());
        assertEquals(2, fromContext.size());

        ChannelInterceptor.free(channel, "inbound-interceptor");
        ChannelInterceptor.free(context, ChannelInterceptor.OutboundInterceptor.class);
        assertTrue(inbound.isFreed());
        assertTrue(outbound.isFreed());

        ChannelInterceptor.reset(channel, ChannelInterceptor.InboundInterceptor.class);
        ChannelInterceptor.reset(context, "outbound-interceptor");
        assertTrue(inbound.isNotFreed());
        assertTrue(outbound.isNotFreed());

        ChannelInterceptor.freeAll(channel);
        assertTrue(inbound.isFreed());
        assertTrue(outbound.isFreed());
        ChannelInterceptor.resetAll(context);
        assertTrue(inbound.isNotFreed());
        assertTrue(outbound.isNotFreed());
        channel.finishAndReleaseAll();
    }

    @Test
    public void allInboundStateAndForwardingHelpersAreExecutable() {
        ExposedInbound interceptor = new ExposedInbound();
        EmbeddedChannel channel = new EmbeddedChannel(interceptor, new ExceptionSink());
        ChannelHandlerContext ctx = channel.pipeline().context(interceptor);

        interceptor.exercise(ctx);

        assertTrue(interceptor.isNotFreed());
        assertEquals("free-read", channel.readInbound());
        assertEquals("reset-read", channel.readInbound());
        channel.finishAndReleaseAll();
    }

    @Test
    public void allOutboundStateAndForwardingHelpersAreExecutable() {
        CapturingOutbound capture = new CapturingOutbound();
        ExposedOutbound interceptor = new ExposedOutbound();
        EmbeddedChannel channel = new EmbeddedChannel(capture, interceptor);
        ChannelHandlerContext ctx = channel.pipeline().context(interceptor);
        int initialCalls = capture.calls;

        interceptor.exercise(ctx);

        assertTrue(interceptor.isNotFreed());
        assertEquals(16, capture.calls - initialCalls);
        channel.finishAndReleaseAll();
    }

    @Test
    public void staticOperationsCoverEveryOverloadAndMissingTargets() {
        ChannelInterceptor.InboundInterceptor<Object> inbound = new ChannelInterceptor.InboundInterceptor<>();
        ChannelInterceptor.OutboundInterceptor outbound = new ChannelInterceptor.OutboundInterceptor();
        EmbeddedChannel channel = new EmbeddedChannel();
        channel.pipeline().addLast("plain", new ChannelInboundHandlerAdapter());
        channel.pipeline().addLast("inbound", inbound);
        channel.pipeline().addLast("outbound", outbound);
        ChannelHandlerContext context = channel.pipeline().context("inbound");

        assertNull(ChannelInterceptor.getInterceptor(channel.pipeline(), "plain"));
        assertNull(ChannelInterceptor.getInterceptor(channel.pipeline(), BlockingInbound.class));
        assertThrows(RuntimeException.class,
                     () -> ChannelInterceptor.getInterceptor(channel.pipeline(), String.class));

        ChannelInterceptor.free(channel, ChannelInterceptor.InboundInterceptor.class);
        ChannelInterceptor.free(context, "outbound");
        ChannelInterceptor.free(channel.pipeline(), "missing");
        ChannelInterceptor.free(channel.pipeline(), BlockingInbound.class);
        assertTrue(inbound.isFreed());
        assertTrue(outbound.isFreed());

        ChannelInterceptor.reset(context, ChannelInterceptor.InboundInterceptor.class);
        ChannelInterceptor.reset(channel, "outbound");
        ChannelInterceptor.reset(channel.pipeline(), "missing");
        ChannelInterceptor.reset(channel.pipeline(), BlockingInbound.class);
        assertTrue(inbound.isNotFreed());
        assertTrue(outbound.isNotFreed());

        ChannelInterceptor.freeAll(context);
        assertTrue(inbound.isFreed());
        assertTrue(outbound.isFreed());
        ChannelInterceptor.resetAll(channel);
        assertTrue(inbound.isNotFreed());
        assertTrue(outbound.isNotFreed());
        channel.finishAndReleaseAll();
    }

    private static final class BlockingInbound extends ChannelInterceptor.InboundInterceptor<Object> {
        private int interceptedReads;

        @Override
        protected void preChannelRead(ChannelHandlerContext ctx, Object msg) {
            interceptedReads++;
        }
    }

    private static final class ExposedInbound extends ChannelInterceptor.InboundInterceptor<Object> {
        void exercise(ChannelHandlerContext ctx) {
            Throwable error = new IllegalStateException("test");
            freeAndFireRegistered(ctx);
            resetAndFireRegistered(ctx);
            freeAndFireUnregistered(ctx);
            resetAndFireUnregistered(ctx);
            freeAndFireActive(ctx);
            resetAndFireActive(ctx);
            freeAndFireInactive(ctx);
            resetAndFireInactive(ctx);
            freeAndFireRead(ctx, "free-read");
            resetAndFireRead(ctx, "reset-read");
            freeAndFireReadComplete(ctx);
            resetAndFireReadComplete(ctx);
            freeAndFireUserEventTriggered(ctx, "free-event");
            resetAndFireUserEventTriggered(ctx, "reset-event");
            freeAndFireWritabilityChanged(ctx);
            resetAndFireWritabilityChanged(ctx);
            freeAndFireExceptionCaught(ctx, error);
            resetAndFireExceptionCaught(ctx, error);
        }
    }

    private static final class ExceptionSink extends ChannelInboundHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {}
    }

    private static final class ExposedOutbound extends ChannelInterceptor.OutboundInterceptor {
        void exercise(ChannelHandlerContext ctx) {
            SocketAddress local = new InetSocketAddress(10001);
            SocketAddress remote = new InetSocketAddress(10002);
            freeAndBind(ctx, local, ctx.newPromise());
            resetAndBind(ctx, local, ctx.newPromise());
            freeAndConnect(ctx, remote, local, ctx.newPromise());
            resetAndConnect(ctx, remote, local, ctx.newPromise());
            freeAndDisconnect(ctx, ctx.newPromise());
            resetAndDisconnect(ctx, ctx.newPromise());
            freeAndClose(ctx, ctx.newPromise());
            resetAndClose(ctx, ctx.newPromise());
            freeAndDeregister(ctx, ctx.newPromise());
            resetAndDeregister(ctx, ctx.newPromise());
            freeAndRead(ctx);
            resetAndRead(ctx);
            freeAndWrite(ctx, "free-write", ctx.newPromise());
            resetAndWrite(ctx, "reset-write", ctx.newPromise());
            freeAndFlush(ctx);
            resetAndFlush(ctx);
        }
    }

    private static final class CapturingOutbound extends ChannelOutboundHandlerAdapter {
        private int calls;

        @Override
        public void bind(ChannelHandlerContext ctx, SocketAddress localAddress, ChannelPromise promise) {
            calls++;
            promise.setSuccess();
        }

        @Override
        public void connect(ChannelHandlerContext ctx, SocketAddress remoteAddress,
                            SocketAddress localAddress, ChannelPromise promise) {
            calls++;
            promise.setSuccess();
        }

        @Override
        public void disconnect(ChannelHandlerContext ctx, ChannelPromise promise) {
            calls++;
            promise.setSuccess();
        }

        @Override
        public void close(ChannelHandlerContext ctx, ChannelPromise promise) {
            calls++;
            promise.setSuccess();
        }

        @Override
        public void deregister(ChannelHandlerContext ctx, ChannelPromise promise) {
            calls++;
            promise.setSuccess();
        }

        @Override
        public void read(ChannelHandlerContext ctx) {
            calls++;
        }

        @Override
        public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
            calls++;
            promise.setSuccess();
        }

        @Override
        public void flush(ChannelHandlerContext ctx) {
            calls++;
        }
    }
}
