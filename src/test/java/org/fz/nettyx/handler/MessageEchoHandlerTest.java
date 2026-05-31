package org.fz.nettyx.handler;

import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.*;

public class MessageEchoHandlerTest {

    @Test
    public void testEchoWritesMessageBack() {
        EmbeddedChannel channel = new EmbeddedChannel(new MessageEchoHandler());
        channel.writeInbound("hello");
        String echoed = channel.readOutbound();
        assertEquals("hello", echoed);
        assertFalse(channel.finish());
    }

    @Test
    public void testEchoMultipleMessages() {
        EmbeddedChannel channel = new EmbeddedChannel(new MessageEchoHandler());
        channel.writeInbound("msg1");
        channel.writeInbound("msg2");
        assertEquals("msg1", channel.readOutbound());
        assertEquals("msg2", channel.readOutbound());
        assertNull(channel.readOutbound());
        assertFalse(channel.finish());
    }
}
