package org.fz.nettyx.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.schema.SchemaRegistry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SchemaCodecTest
{

    private static final SchemaRegistry REGISTRY = SchemaRegistry.load(
            "configured/device.xml", "configured/geo.xml");

    private static final Map<String, Object> TRACK = Map.of(
            "count", (short) 2,
            "points", List.of(
                    Map.of("longitude", 11, "latitude", 12),
                    Map.of("longitude", 21, "latitude", 22)));

    private static final byte[] TRACK_BYTES = {
            2,
            0, 0, 0, 11,
            0, 0, 0, 12,
            0, 0, 0, 21,
            0, 0, 0, 22
    };

    @Test
    public void defaultCodecEncodesAndSkipsTrailingBytes()
    {
        SchemaCodec codec   = new SchemaCodec(REGISTRY, "device.Track");
        ByteBuf     encoded = Unpooled.buffer();

        codec.encode(null, TRACK, encoded);
        assertArrayEquals(TRACK_BYTES, readableBytes(encoded));
        assertEquals("device.Track", codec.getSchema().fqName());
        assertTrue(codec.isSkipLeftBytes());

        encoded.writeByte(0x55);
        assertEquals(TRACK, decode(codec, encoded));
        assertEquals(0, encoded.readableBytes());
        encoded.release();
    }

    @Test
    public void configuredCodecCanKeepTrailingBytes()
    {
        SchemaCodec codec = new SchemaCodec(REGISTRY, "device.Track", false);
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{
                2,
                0, 0, 0, 11,
                0, 0, 0, 12,
                0, 0, 0, 21,
                0, 0, 0, 22,
                0x55
        });

        assertEquals(TRACK, decode(codec, input));
        assertFalse(codec.isSkipLeftBytes());
        assertEquals(1, input.readableBytes());
        assertEquals(0x55, input.readUnsignedByte());
        input.release();
    }

    private static Map<String, Object> decode(SchemaCodec codec, ByteBuf input)
    {
        List<Object> output = new ArrayList<>();
        codec.decode(null, input, output);
        assertEquals(1, output.size());
        return (Map<String, Object>) output.get(0);
    }

    private static byte[] readableBytes(ByteBuf buffer)
    {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
    }
}
