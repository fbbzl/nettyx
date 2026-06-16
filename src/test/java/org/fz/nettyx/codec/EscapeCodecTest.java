package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.fz.nettyx.codec.EscapeCodec.EscapeMap;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author fengbinbin
 * @since 2024
 */
public class EscapeCodecTest {

    @Test
    public void testSimpleEscape() {
        EscapeMap map = new EscapeMap();
        map.putHex("7E", "7D5E");  // 0x7E -> 0x7D 0x5E

        EmbeddedChannel channel = new EmbeddedChannel(new EscapeCodec(map));

        // Encode: 0x7E 0x01 -> 0x7D 0x5E 0x01
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{0x7E, 0x01});
        assertTrue(channel.writeOutbound(input));

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);
        assertEquals(3, encoded.readableBytes());
        assertEquals((byte) 0x7D, encoded.readByte());
        assertEquals((byte) 0x5E, encoded.readByte());
        assertEquals((byte) 0x01, encoded.readByte());
        encoded.release();

        // Decode: 0x7D 0x5E 0x01 -> 0x7E 0x01
        ByteBuf encodedInput = Unpooled.wrappedBuffer(new byte[]{0x7D, 0x5E, 0x01});
        assertTrue(channel.writeInbound(encodedInput));

        ByteBuf decoded = channel.readInbound();
        assertNotNull(decoded);
        assertEquals(2, decoded.readableBytes());
        assertEquals((byte) 0x7E, decoded.readByte());
        assertEquals((byte) 0x01, decoded.readByte());
        decoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testNoEscapeNeeded() {
        EscapeMap map = new EscapeMap();
        map.putHex("7E", "7D5E");

        EmbeddedChannel channel = new EmbeddedChannel(new EscapeCodec(map));

        // Data without escape characters passes through
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{0x01, 0x02, 0x03});
        assertTrue(channel.writeOutbound(input));

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);
        assertEquals(3, encoded.readableBytes());
        assertEquals((byte) 0x01, encoded.readByte());
        assertEquals((byte) 0x02, encoded.readByte());
        assertEquals((byte) 0x03, encoded.readByte());
        encoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testMultipleEscapes() {
        EscapeMap map = new EscapeMap();
        map.putHex("7E", "7D5E");

        EmbeddedChannel channel = new EmbeddedChannel(new EscapeCodec(map));

        // 0x7E 0x7E 0x02 -> 0x7D 0x5E 0x7D 0x5E 0x02
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{0x7E, 0x7E, 0x02});
        assertTrue(channel.writeOutbound(input));

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);
        assertEquals(5, encoded.readableBytes());
        assertEquals((byte) 0x7D, encoded.readByte());
        assertEquals((byte) 0x5E, encoded.readByte());
        assertEquals((byte) 0x7D, encoded.readByte());
        assertEquals((byte) 0x5E, encoded.readByte());
        assertEquals((byte) 0x02, encoded.readByte());
        encoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testSingleByteEscape() {
        EscapeMap map = new EscapeMap();
        // single byte: 0x7D -> 0x7D 0x01
        map.putHex("7D", "7D01");

        EmbeddedChannel channel = new EmbeddedChannel(new EscapeCodec(map));

        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{0x7D, 0x02});
        assertTrue(channel.writeOutbound(input));

        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);
        assertEquals(3, encoded.readableBytes());
        byte[] encodedBytes = new byte[3];
        encoded.readBytes(encodedBytes);
        assertArrayEquals(new byte[]{0x7D, 0x01, 0x02}, encodedBytes);
        encoded.release();

        ByteBuf decodeInput = Unpooled.wrappedBuffer(new byte[]{0x7D, 0x01, 0x02});
        assertTrue(channel.writeInbound(decodeInput));

        ByteBuf decoded = channel.readInbound();
        assertNotNull(decoded);
        assertEquals(2, decoded.readableBytes());
        assertEquals((byte) 0x7D, decoded.readByte());
        assertEquals((byte) 0x02, decoded.readByte());
        decoded.release();

        assertFalse(channel.finish());
    }

    @Test
    public void testRoundTrip() {
        EscapeMap map = new EscapeMap();
        map.putHex("7E", "7D5E");
        map.putHex("7D", "7D5D");

        EmbeddedChannel channel = new EmbeddedChannel(new EscapeCodec(map));

        byte[] original = {0x01, 0x7E, 0x02, 0x7D, 0x03};

        // Encode
        assertTrue(channel.writeOutbound(Unpooled.wrappedBuffer(original)));
        ByteBuf encoded = channel.readOutbound();
        assertNotNull(encoded);

        // Decode
        assertTrue(channel.writeInbound(encoded.retain()));
        ByteBuf decoded = channel.readInbound();
        assertNotNull(decoded);

        byte[] result = new byte[decoded.readableBytes()];
        decoded.readBytes(result);
        assertArrayEquals(original, result);

        encoded.release();
        decoded.release();

        assertFalse(channel.finish());
    }
}
