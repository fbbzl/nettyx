package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.*;

public class StringMessageCodecTest {

    @Test
    public void encodesAndDecodesUsingConfiguredCharset() {
        EmbeddedChannel channel = new EmbeddedChannel(new StringMessageCodec(StandardCharsets.UTF_16LE));
        String message = "Nettyx\u4E32";

        assertTrue(channel.writeOutbound(message));
        ByteBuf encoded = channel.readOutbound();
        byte[] actual = new byte[encoded.readableBytes()];
        encoded.readBytes(actual);
        encoded.release();
        assertArrayEquals(message.getBytes(StandardCharsets.UTF_16LE), actual);

        assertTrue(channel.writeInbound(io.netty.buffer.Unpooled.wrappedBuffer(actual)));
        assertEquals(message, channel.readInbound());
        channel.finishAndReleaseAll();
    }
}
