package org.fz.nettyx.util;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author fengbinbin
 * @since 2024
 */
public class HexKitTest {

    @Test
    public void testEncodeSingleByte() {
        assertEquals("FF", HexKit.encode((byte) 0xFF));
        assertEquals("00", HexKit.encode((byte) 0x00));
        assertEquals("0A", HexKit.encode((byte) 0x0A));
        assertEquals("AB", HexKit.encode((byte) 0xAB));
        assertEquals("7F", HexKit.encode((byte) 0x7F));
    }

    @Test
    public void testEncodeMultipleBytes() {
        assertEquals("010203", HexKit.encode((byte) 0x01, (byte) 0x02, (byte) 0x03));
        assertEquals("AABBCC", HexKit.encode((byte) 0xAA, (byte) 0xBB, (byte) 0xCC));
    }

    @Test
    public void testEncodeEmpty() {
        assertEquals("", HexKit.encode(new byte[0]));
    }

    @Test
    public void testEncodeNull() {
        assertNull(HexKit.encode((byte[]) null));
    }

    @Test
    public void testEncodeByteBuf() {
        ByteBuf buf = Unpooled.wrappedBuffer(new byte[]{0x01, 0x02, (byte) 0xFF});
        String hex = HexKit.encode(buf);
        assertNotNull(hex);
        buf.release();
    }

    @Test
    public void testDecodeEvenLength() {
        assertArrayEquals(new byte[]{0x01, 0x02, 0x03}, HexKit.decode("010203"));
        assertArrayEquals(new byte[]{(byte) 0xAA, (byte) 0xBB}, HexKit.decode("AABB"));
        assertArrayEquals(new byte[]{(byte) 0xFF}, HexKit.decode("FF"));
    }

    @Test
    public void testDecodeOddLength() {
        assertArrayEquals(new byte[]{0x0A}, HexKit.decode("A"));
        assertArrayEquals(new byte[]{0x0F, 0x01}, HexKit.decode("F01"));
    }

    @Test
    public void testDecodeEmpty() {
        assertArrayEquals(new byte[0], HexKit.decode(""));
    }

    @Test
    public void testRoundTrip() {
        byte[] original = {0x01, 0x7E, (byte) 0xAB, 0x00, (byte) 0xFF, 0x55};
        String hex = HexKit.encode(original);
        byte[] decoded = HexKit.decode(hex);
        assertArrayEquals(original, decoded);
    }

    @Test
    public void testToByte() {
        assertEquals((byte) 0xFF, HexKit.toByte("FF"));
        assertEquals((byte) 0x00, HexKit.toByte("00"));
        assertEquals((byte) 0x0A, HexKit.toByte("0A"));
    }

    @Test
    public void testDecodeBuf() {
        ByteBuf buf = HexKit.decodeBuf("0102FF");
        assertNotNull(buf);
        assertEquals(3, buf.readableBytes());
        assertEquals((byte) 0x01, buf.readByte());
        assertEquals((byte) 0x02, buf.readByte());
        assertEquals((byte) 0xFF, buf.readByte());
        buf.release();
    }
}
