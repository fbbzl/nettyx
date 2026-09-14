package org.fz.nettyx.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import org.fz.nettyx.handler.MessageFilter.InboundFilter;
import org.fz.nettyx.handler.MessageFilter.OutboundFilter;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author fengbinbin
 * @since 2024
 */
public class MessageFilterTest {

    @Test
    public void testInboundFilterAllowsMatching() {
        // Only allow strings containing "keep"
        InboundFilter<String> filter = new InboundFilter<>(msg -> msg.contains("keep"));
        EmbeddedChannel channel = new EmbeddedChannel(filter);

        assertTrue(channel.writeInbound("keep_this_message"));

        String result = channel.readInbound();
        assertEquals("keep_this_message", result);

        assertFalse(channel.finish());
    }

    @Test
    public void testInboundFilterBlocksNonMatching() {
        InboundFilter<String> filter = new InboundFilter<>(msg -> msg.contains("keep"));
        EmbeddedChannel channel = new EmbeddedChannel(filter);

        assertFalse(channel.writeInbound("discard_this"));

        String result = channel.readInbound();
        assertNull(result);

        assertFalse(channel.finish());
    }

    @Test
    public void testInboundFilterMixedMessages() {
        InboundFilter<String> filter = new InboundFilter<>(msg -> msg.length() > 3);
        EmbeddedChannel channel = new EmbeddedChannel(filter);

        assertTrue(channel.writeInbound("long"));
        assertEquals("long", channel.readInbound());

        assertFalse(channel.writeInbound("sh"));

        assertNull(channel.readInbound());

        assertFalse(channel.finish());
    }

    @Test
    public void testOutboundFilterAllowsMatching() {
        OutboundFilter<String> filter = new OutboundFilter<>(msg -> msg.startsWith("send"));
        EmbeddedChannel channel = new EmbeddedChannel(filter);

        assertTrue(channel.writeOutbound("send_data"));

        String result = channel.readOutbound();
        assertEquals("send_data", result);

        assertFalse(channel.finish());
    }

    @Test
    public void testOutboundFilterBlocksNonMatching() {
        OutboundFilter<String> filter = new OutboundFilter<>(msg -> msg.startsWith("send"));
        EmbeddedChannel channel = new EmbeddedChannel(filter);

        assertFalse(channel.writeOutbound("drop_data"));

        String result = channel.readOutbound();
        assertNull(result);

        assertFalse(channel.finish());
    }

    @Test
    public void testInboundFilterAllMatch() {
        InboundFilter<Object> filter = new InboundFilter<>(msg -> true);
        EmbeddedChannel channel = new EmbeddedChannel(filter);

        assertTrue(channel.writeInbound("msg1"));
        assertTrue(channel.writeInbound(42));
        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{1, 2})));

        assertEquals("msg1", channel.readInbound());
        assertEquals(Integer.valueOf(42), channel.readInbound());
        assertNotNull(channel.readInbound());

        assertFalse(channel.finish());
    }

    @Test
    public void testInboundFilterNoneMatch() {
        InboundFilter<Object> filter = new InboundFilter<>(msg -> false);
        EmbeddedChannel channel = new EmbeddedChannel(filter);

        assertFalse(channel.writeInbound("msg1"));
        assertFalse(channel.writeInbound(42));

        assertNull(channel.readInbound());

        assertFalse(channel.finish());
    }

    @Test
    public void predicateFailuresAreReportedAndPromisesFail() throws Exception {
        EmbeddedChannel inbound = new EmbeddedChannel(new InboundFilter<>(msg -> {
            throw new IllegalStateException("inbound");
        }), new ChannelInboundHandlerAdapter() {
            @Override
            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            }
        });
        assertFalse(inbound.writeInbound("message"));
        assertNotNull(inbound.pipeline().firstContext());
        inbound.finishAndReleaseAll();

        OutboundFilter<String> outboundFilter = new OutboundFilter<>(msg -> {
            throw new IllegalStateException("outbound");
        });
        EmbeddedChannel outbound = new EmbeddedChannel(outboundFilter);
        io.netty.channel.ChannelPromise outboundPromise = outbound.newPromise();
        outboundFilter.write(outbound.pipeline().context(outboundFilter), "message", outboundPromise);
        assertFalse(outboundPromise.isSuccess());
        outbound.finishAndReleaseAll();
    }
}
