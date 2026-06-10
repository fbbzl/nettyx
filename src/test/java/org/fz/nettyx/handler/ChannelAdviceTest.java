package org.fz.nettyx.handler;

import io.netty.channel.embedded.EmbeddedChannel;
import org.fz.nettyx.handler.ChannelAdvice.InboundAdvice;
import org.fz.nettyx.handler.ChannelAdvice.OutboundAdvice;
import org.junit.Test;

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
}
