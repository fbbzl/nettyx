package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author fengbinbin
 * @since 2024
 */
public class ByteArrayCodecTest {

    @Test
    public void testEncodeDecode() {
        EmbeddedChannel channel = new EmbeddedChannel(new ByteArrayCodec());

        byte[] original = {1, 2, 3, 4, 5};
        assertTrue(channel.writeOutbound(original));

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);
        byte[] encodedBytes = new byte[encoded.readableBytes()];
        encoded.readBytes(encodedBytes);
        assertArrayEquals(original, encodedBytes);
        encoded.release();

        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(original)));
        byte[] decoded = channel.readInbound();
        assertNotNull(decoded);
        assertArrayEquals(original, decoded);

        assertFalse(channel.finish());
    }

    @Test
    public void testEncodeEmptyArray() {
        EmbeddedChannel channel = new EmbeddedChannel(new ByteArrayCodec());

        byte[] empty = {};
        assertTrue(channel.writeOutbound(empty));

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);
        assertEquals(0, encoded.readableBytes());
        encoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testEncodeSingleByte() {
        EmbeddedChannel channel = new EmbeddedChannel(new ByteArrayCodec());

        byte[] single = {42};
        assertTrue(channel.writeOutbound(single));

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);
        assertEquals(1, encoded.readableBytes());
        assertEquals(42, encoded.readByte());
        encoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testDecodeSingleByte() {
        EmbeddedChannel channel = new EmbeddedChannel(new ByteArrayCodec());

        byte[] data = {99};
        assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(data)));

        byte[] decoded = channel.readInbound();
        assertNotNull(decoded);
        assertArrayEquals(data, decoded);

        assertFalse(channel.finish());
    }

    @Test
    public void testMultipleEncodeDecode() {
        EmbeddedChannel channel = new EmbeddedChannel(new ByteArrayCodec());

        for (int i = 0; i < 5; i++) {
            byte[] data = {(byte) i, (byte) (i + 10)};
            assertTrue(channel.writeOutbound(data));
            ByteBuf encoded = channel.readOutbound();
            assertNotNull(encoded);
            byte[] result = new byte[encoded.readableBytes()];
            encoded.readBytes(result);
            assertArrayEquals(data, result);
            encoded.release();
        }

        assertFalse(channel.finish());
    }
}
