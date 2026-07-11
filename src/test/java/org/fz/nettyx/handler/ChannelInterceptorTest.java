package org.fz.nettyx.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

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

    private static final class BlockingInbound extends ChannelInterceptor.InboundInterceptor<Object> {
        private int interceptedReads;

        @Override
        protected void preChannelRead(ChannelHandlerContext ctx, Object msg) {
            interceptedReads++;
        }
    }
}
