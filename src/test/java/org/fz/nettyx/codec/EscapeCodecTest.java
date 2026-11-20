package org.fz.nettyx.codec;

import io.netty.buffer.AbstractByteBufAllocator;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledHeapByteBuf;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.DecoderException;
import org.fz.nettyx.codec.EscapeCodec.EscapeMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Verifies byte escaping, round trips, and rejection of invalid or ambiguous mappings.
 *
 * @author fengbinbin
 * @version 1.0
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

    @Test
    public void escapeMapSupportsBuffersAndRejectsInvalidMappings()
    {
        EscapeMap map = new EscapeMap();
        ByteBuf real = Unpooled.wrappedBuffer(new byte[]{0x01});
        ByteBuf replacement = Unpooled.wrappedBuffer(new byte[]{0x02});
        map.putBuf(real, replacement);
        assertEquals(1, real.readerIndex());
        assertEquals(1, replacement.readerIndex());
        assertEquals(1, map.size());
        assertEquals(1, map.getInverse().size());
        assertThrows(IllegalArgumentException.class, () -> map.putHex("", "01"));
        assertEquals(1, map.size());
        assertThrows(IllegalArgumentException.class, () -> map.putHex("03", ""));
        assertEquals(1, map.size());
        assertThrows(IllegalArgumentException.class, () -> map.putBuf(Unpooled.EMPTY_BUFFER, replacement));
        assertEquals(1, map.size());
        real.release();
        replacement.release();
        assertThrows(IllegalArgumentException.class, () -> {
            EscapeMap conflicting = new EscapeMap();
            conflicting.putHex("01", "02");
            conflicting.putHex("03", "01");
        });
    }

    @Test
    public void escapeMapRejectsInvalidMappingsAtConstructionAndPut()
    {
        assertFalse(Map.class.isAssignableFrom(EscapeMap.class));

        EscapeMap map = new EscapeMap();
        assertThrows(IllegalArgumentException.class, () -> map.put(new byte[0], new byte[]{1}));
        assertTrue(map.isEmpty());

        Map<byte[], byte[]> invalid = new HashMap<>();
        invalid.put(new byte[]{1}, new byte[]{2});
        invalid.put(new byte[]{3}, new byte[]{1});
        assertThrows(IllegalArgumentException.class, () -> new EscapeMap(invalid));
    }

    @Test
    public void escapeReplacementsMustBeUniqueByContent()
    {
        EscapeMap validated = new EscapeMap();
        validated.put(new byte[]{1}, new byte[]{(byte) 0xAA});
        assertThrows(IllegalArgumentException.class,
                     () -> validated.put(new byte[]{2}, new byte[]{(byte) 0xAA}));
        assertEquals(1, validated.size());

        assertThrows(IllegalArgumentException.class, () -> validated.putHex("02", "AA"));
        assertEquals(1, validated.size());
    }

    @Test
    public void constructorRejectsDuplicateByteContentOnEitherSide()
    {
        Map<byte[], byte[]> invalid = new HashMap<>();
        invalid.put(new byte[]{1}, new byte[]{(byte) 0xAA});
        invalid.put(new byte[]{1}, new byte[]{(byte) 0xBB});
        assertThrows(IllegalArgumentException.class, () -> new EscapeMap(invalid));

        invalid.clear();
        invalid.put(new byte[]{1}, new byte[]{(byte) 0xAA});
        invalid.put(new byte[]{2}, new byte[]{(byte) 0xAA});
        assertThrows(IllegalArgumentException.class, () -> new EscapeMap(invalid));

        Map<byte[], byte[]> valid = new HashMap<>();
        valid.put(new byte[]{1}, new byte[]{(byte) 0xAA});
        valid.put(new byte[]{2}, new byte[]{(byte) 0xBB});
        assertEquals(2, new EscapeMap(valid).getInverse().size());
    }

    @Test
    public void escapeReplacementsMustNotOverlapByPrefix()
    {
        EscapeMap shorterFirst = new EscapeMap();
        shorterFirst.put(new byte[]{1}, new byte[]{(byte) 0xAA});
        assertThrows(IllegalArgumentException.class,
                     () -> shorterFirst.put(new byte[]{2}, new byte[]{(byte) 0xAA, (byte) 0xBB}));
        assertEquals(1, shorterFirst.size());

        EscapeMap longerFirst = new EscapeMap();
        longerFirst.put(new byte[]{1}, new byte[]{(byte) 0xAA, (byte) 0xBB});
        assertThrows(IllegalArgumentException.class,
                     () -> longerFirst.put(new byte[]{2}, new byte[]{(byte) 0xAA}));
        assertEquals(1, longerFirst.size());

        Map<byte[], byte[]> invalid = new HashMap<>();
        invalid.put(new byte[]{1}, new byte[]{(byte) 0xAA});
        invalid.put(new byte[]{2}, new byte[]{(byte) 0xAA, (byte) 0xBB});
        assertThrows(IllegalArgumentException.class, () -> new EscapeMap(invalid));
    }

    @Test
    public void rejectsContainedOriginalsWhenConstructingOrAddingMappings()
    {
        EscapeMap map = new EscapeMap();
        map.putHex("0102", "42");
        assertThrows(IllegalArgumentException.class, () -> map.putHex("01", "41"));
        assertThrows(IllegalArgumentException.class, () -> map.putHex("00010203", "43"));
        assertEquals(1, map.size());

        Map<byte[], byte[]> entries = Map.of(new byte[]{1, 2, 3}, new byte[]{0x42},
                                            new byte[]{2}, new byte[]{0x41});
        assertThrows(IllegalArgumentException.class, () -> new EscapeMap(entries));
    }

    @Test
    public void rejectsReplacementsContainedInAnyOriginal()
    {
        EscapeMap map = new EscapeMap();
        map.putHex("0102", "42");
        assertThrows(IllegalArgumentException.class, () -> map.putHex("03", "01"));
        assertThrows(IllegalArgumentException.class, () -> map.putHex("034204", "43"));
        assertThrows(IllegalArgumentException.class, () -> map.putHex("03", "42"));
        assertEquals(1, map.size());

        Map<byte[], byte[]> entries = Map.of(new byte[]{1, 2, 3}, new byte[]{2});
        assertThrows(IllegalArgumentException.class, () -> new EscapeMap(entries));
    }

    @Test
    public void putReplacesRealByContentAndKeepsConfigurationAfterRejectedUpdate()
    {
        EscapeMap map = new EscapeMap();
        map.putHex("01", "AA");
        map.putHex("02", "BB");
        assertArrayEquals(new byte[]{(byte) 0xAA}, map.put(new byte[]{1}, new byte[]{(byte) 0xCC}));
        assertEquals(2, map.size());
        assertThrows(IllegalArgumentException.class, () -> map.put(new byte[]{1}, new byte[]{(byte) 0xBB}));
        assertEquals(2, map.size());

        EmbeddedChannel channel = new EmbeddedChannel(new EscapeCodec(map));
        try {
            assertTrue(channel.writeOutbound(Unpooled.wrappedBuffer(new byte[]{1, 2})));
            ByteBuf encoded = channel.readOutbound();
            try {
                assertEquals(0xCC, encoded.readUnsignedByte());
                assertEquals(0xBB, encoded.readUnsignedByte());
                assertFalse(encoded.isReadable());
            } finally {
                encoded.release();
            }
            assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{(byte) 0xCC, (byte) 0xBB})));
            ByteBuf decoded = channel.readInbound();
            try {
                assertEquals(1, decoded.readUnsignedByte());
                assertEquals(2, decoded.readUnsignedByte());
                assertFalse(decoded.isReadable());
            } finally {
                decoded.release();
            }
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void longEscapeMatchesAllBytesInBothDirections()
    {
        EscapeMap map = new EscapeMap();
        map.putHex("010203", "414243");
        EmbeddedChannel channel = new EmbeddedChannel(new EscapeCodec(map));
        try {
            assertTrue(channel.writeOutbound(Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("01040301020301"))));
            ByteBuf encoded = channel.readOutbound();
            try {
                assertEquals("01040341424301", ByteBufUtil.hexDump(encoded));
            } finally {
                encoded.release();
            }

            assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(ByteBufUtil.decodeHexDump("41444341424341"))));
            ByteBuf decoded = channel.readInbound();
            try {
                assertEquals("41444301020341", ByteBufUtil.hexDump(decoded));
            } finally {
                decoded.release();
            }
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    public void decoderReleasesOutputWhenExpansionFails()
    {
        EscapeMap map = new EscapeMap();
        map.putHex("0102", "42");
        EmbeddedChannel channel = new EmbeddedChannel(new EscapeCodec(map));
        ByteBuf output = Unpooled.buffer(1, 1);
        AbstractByteBufAllocator allocator = new AbstractByteBufAllocator(false) {
            @Override
            protected ByteBuf newHeapBuffer(int initialCapacity, int maxCapacity)
            {
                return output;
            }

            @Override
            protected ByteBuf newDirectBuffer(int initialCapacity, int maxCapacity)
            {
                return Unpooled.directBuffer(initialCapacity, maxCapacity);
            }

            @Override
            public boolean isDirectBufferPooled()
            {
                return false;
            }
        };
        try {
            ByteBuf input = new UnpooledHeapByteBuf(allocator, 1, 1).writeByte(0x42);
            DecoderException error = assertThrows(DecoderException.class, () -> channel.writeInbound(input));
            assertTrue(error.getCause() instanceof IndexOutOfBoundsException);
            assertNull(channel.readInbound());
            assertEquals(0, output.refCnt());
        } finally {
            channel.finishAndReleaseAll();
            if (output.refCnt() > 0) output.release();
        }
    }

    @Test
    public void duplexCodecRejectsAnEmptyMappingOnEitherSide()
    {
        EscapeMap empty = new EscapeMap();
        EscapeMap configured = new EscapeMap();
        configured.putHex("01", "42");
        assertThrows(IllegalArgumentException.class, () -> new EscapeCodec(empty));
        assertThrows(IllegalArgumentException.class, () ->
                new EscapeCodec(new EscapeCodec.EscapeDecoder(configured), new EscapeCodec.EscapeEncoder(empty)));
    }

    @Test
    public void rejectsNullMappingsWithoutChangingConfiguration()
    {
        byte[] real = {1}, replacement = {0x42};
        EscapeMap map = new EscapeMap();
        map.put(real, replacement);
        assertThrows(IllegalArgumentException.class, () -> map.put(null, replacement));
        assertThrows(IllegalArgumentException.class, () -> map.put(real, null));
        assertEquals(1, map.size());
        assertArrayEquals(replacement, map.put(real, new byte[]{0x43}));

        Map<byte[], byte[]> entries = new HashMap<>();
        entries.put(null, replacement);
        assertThrows(IllegalArgumentException.class, () -> new EscapeMap(entries));
        entries.clear();
        entries.put(real, null);
        assertThrows(IllegalArgumentException.class, () -> new EscapeMap(entries));
    }

    @Test
    public void codecKeepsIndependentValidatedConfiguration()
    {
        byte[] real = {1}, replacement = {2};
        EscapeMap map = new EscapeMap();
        map.put(real, replacement);
        EscapeCodec.EscapeEncoder encoder = new EscapeCodec.EscapeEncoder(map);
        EscapeCodec.EscapeDecoder decoder = new EscapeCodec.EscapeDecoder(map);
        EmbeddedChannel channel = new EmbeddedChannel(new EscapeCodec(decoder, encoder));
        try {
            real[0] = 3;
            replacement[0] = 4;
            map.putHex("03", "04");
            encoder.getMap().putHex("05", "06");
            decoder.getMap().putHex("06", "05");

            assertTrue(channel.writeOutbound(Unpooled.wrappedBuffer(new byte[]{1})));
            ByteBuf encoded = channel.readOutbound();
            try {
                assertEquals(2, encoded.readByte());
            } finally {
                encoded.release();
            }
            assertTrue(channel.writeInbound(Unpooled.wrappedBuffer(new byte[]{2})));
            ByteBuf decoded = channel.readInbound();
            try {
                assertEquals(1, decoded.readByte());
            } finally {
                decoded.release();
            }
        } finally {
            channel.finishAndReleaseAll();
        }
    }
}
