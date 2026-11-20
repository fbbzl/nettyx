package org.fz.nettyx.serializer.schema;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.basic.c.signed.cchar;
import org.fz.nettyx.serializer.basic.c.signed.cdouble;
import org.fz.nettyx.serializer.basic.c.signed.cfloat;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.fz.nettyx.serializer.basic.c.signed.clong4;
import org.fz.nettyx.serializer.basic.c.signed.clong8;
import org.fz.nettyx.serializer.basic.c.signed.cshort;
import org.fz.nettyx.serializer.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.basic.c.unsigned.cushort;
import org.junit.Test;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Verifies schema based serialization, type resolution, and configuration validation.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-16
 */
public class SchemaSerializerTest
{

    static final SchemaContext  CONTEXT = SchemaContext.load(
            "configured/device.xml", "configured/geo.xml");
    static final SchemaSerializer SERIALIZER = CONTEXT.serializer("device.Device");

    @Test
    public void testSchemaSerializer()
    {
        byte[] bytes = Arrays.copyOf(new byte[]{
                0x44, 0x33, 0x22, 0x11,
                (byte) 0xBB, (byte) 0xAA,
                (byte) 0xCD, (byte) 0xCC, (byte) 0xCC, (byte) 0xCC, (byte) 0xCC, 0x4C, 0x42, 0x40,
                'n', 'e', 't', 't', 'y', 0, 0, 0,
                0x11, 0x22,
                1, 0, 2, 0, 3, 0,
                0, 0, 0, 100,
                0, 0, 0, (byte) 200
        }, 122);
        ByteBuf reading = Unpooled.wrappedBuffer(bytes);
        ByteBuf writing = Unpooled.buffer(bytes.length);

        assertEquals(122, bytes.length);
        Map<String, Object> message = SchemaSerializer.toMap(CONTEXT, "device.BenchmarkDevice", reading);
        assertEquals(0x11223344, message.get("id"));
        assertEquals("netty", message.get("name"));
        SchemaSerializer.toByteBuf(CONTEXT, "device.BenchmarkDevice", message, writing);
        assertEquals(bytes.length, writing.readableBytes());

        assertArrayEquals(bytes, writing.array());
    }

    @Test
    public void testSchemaSerializerView()
    {
        ByteBuf reading = Unpooled.wrappedBuffer(new byte[]{
                0x44, 0x33, 0x22, 0x11,
                (byte) 0xBB, (byte) 0xAA,
                (byte) 0xCD, (byte) 0xCC, (byte) 0xCC, (byte) 0xCC, (byte) 0xCC, 0x4C, 0x42, 0x40,
                'n', 'e', 't', 't', 'y', 0, 0, 0,
                0x11, 0x22,
                1, 0, 2, 0, 3, 0,
                0, 0, 0, 100,
                0, 0, 0, (byte) 200
        });
        SchemaSerializer serializer = CONTEXT.serializer("device.Device");
        SchemaView       view       = serializer.view(reading);

        assertEquals(0x11223344, view.getInt("id"));
        assertEquals("netty", view.getString("name"));
        assertEquals(0, reading.readableBytes());
    }

    @Test
    public void testSchemaValuesSerializeByNameForDifferentSchemas()
    {
        ByteBuf reading = Unpooled.buffer();
        reading.writeIntLE(0x11223344);
        reading.writeShortLE(0xAABB);
        reading.writeDoubleLE(36.6D);
        reading.writeBytes(new byte[]{'n', 'e', 't', 't', 'y', 0, 0, 0});
        reading.writeBytes(new byte[]{0x11, 0x22});
        reading.writeShortLE(1);
        reading.writeShortLE(2);
        reading.writeShortLE(3);
        reading.writeInt(100);
        reading.writeInt(200);

        Map<String, Object> device  = SchemaSerializer.toMap(CONTEXT, "device.Device", reading);
        ByteBuf             writing = Unpooled.buffer();

        SchemaSerializer.toByteBuf(CONTEXT, "device.Track", device, writing);

        byte[] actual = new byte[writing.readableBytes()];
        writing.readBytes(actual);
        assertArrayEquals(new byte[17], actual);
    }

    @Test
    public void testSchemaValuesSerializeWithoutChangingBytes()
    {
        byte[] expected = {
                0x44, 0x33, 0x22, 0x11,
                (byte) 0xBB, (byte) 0xAA,
                (byte) 0xCD, (byte) 0xCC, (byte) 0xCC, (byte) 0xCC, (byte) 0xCC, 0x4C, 0x42, 0x40,
                'n', 'e', 't', 't', 'y', 0, 0, 0,
                0x11, 0x22,
                1, 0, 2, 0, 3, 0,
                0, 0, 0, 100,
                0, 0, 0, (byte) 200
        };
        Map<String, Object> device = SchemaSerializer.toMap(
                CONTEXT, "device.Device", Unpooled.wrappedBuffer(expected));
        ByteBuf writing = Unpooled.buffer(expected.length);

        SchemaSerializer.toByteBuf(CONTEXT, "device.Device", device, writing);

        byte[] actual = new byte[writing.readableBytes()];
        writing.readBytes(actual);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testBasicValueWriterPreservesWireValuesAndBounds()
    {
        ByteBuf writing = Unpooled.buffer();

        SERIALIZER.writeField(Schema.SchemaField.basicField("v", cchar.class), -1, ByteOrder.BIG_ENDIAN, writing);
        SERIALIZER.writeField(Schema.SchemaField.basicField("v", cuchar.class), 0xFF, ByteOrder.BIG_ENDIAN, writing);
        SERIALIZER.writeField(Schema.SchemaField.basicField("v", cshort.class), 0x1234, ByteOrder.LITTLE_ENDIAN, writing);
        SERIALIZER.writeField(Schema.SchemaField.basicField("v", cushort.class), 0xABCD, ByteOrder.BIG_ENDIAN, writing);
        SERIALIZER.writeField(Schema.SchemaField.basicField("v", cint.class), 0x10203040, ByteOrder.LITTLE_ENDIAN, writing);
        SERIALIZER.writeField(Schema.SchemaField.basicField("v", clong4.class), 0x50607080, ByteOrder.BIG_ENDIAN, writing);
        SERIALIZER.writeField(Schema.SchemaField.basicField("v", clong8.class), 0x0102030405060708L, ByteOrder.LITTLE_ENDIAN, writing);
        SERIALIZER.writeField(Schema.SchemaField.basicField("v", cfloat.class), 1.0F, ByteOrder.BIG_ENDIAN, writing);
        SERIALIZER.writeField(Schema.SchemaField.basicField("v", cdouble.class), 1.0D, ByteOrder.LITTLE_ENDIAN, writing);

        byte[] actual = new byte[writing.readableBytes()];
        writing.readBytes(actual);
        assertArrayEquals(new byte[]{
                (byte) 0xFF, (byte) 0xFF,
                0x34, 0x12,
                (byte) 0xAB, (byte) 0xCD,
                0x40, 0x30, 0x20, 0x10,
                0x50, 0x60, 0x70, (byte) 0x80,
                0x08, 0x07, 0x06, 0x05, 0x04, 0x03, 0x02, 0x01,
                0x3F, (byte) 0x80, 0, 0,
                0, 0, 0, 0, 0, 0, (byte) 0xF0, 0x3F
        }, actual);

        assertThrows(IllegalArgumentException.class,
                     () -> SERIALIZER.writeField(Schema.SchemaField.basicField("v", cuchar.class), 0x100, ByteOrder.BIG_ENDIAN, Unpooled.buffer()));
        assertThrows(IllegalArgumentException.class,
                     () -> SERIALIZER.writeField(Schema.SchemaField.basicField("v", cushort.class), 0x1_0000, ByteOrder.BIG_ENDIAN, Unpooled.buffer()));
    }

    @Test
    public void testDeserialize()
    {
        ByteBuf buf = Unpooled.buffer();
        buf.writeIntLE(0x11223344);
        buf.writeShortLE(0xAABB);
        buf.writeDoubleLE(36.6D);
        buf.writeBytes(new byte[]{'n', 'e', 't', 't', 'y', 0, 0, 0});
        buf.writeBytes(new byte[]{0x11, 0x22});
        buf.writeShortLE(1);
        buf.writeShortLE(2);
        buf.writeShortLE(3);
        buf.writeInt(100);
        buf.writeInt(200);

        Map<String, Object> device = SchemaSerializer.toMap(CONTEXT, "device.Device", buf);

        assertEquals(0x11223344, device.get("id"));
        assertEquals(0xAABB, device.get("speed"));
        assertEquals(36.6D, (Double) device.get("temperature"), 0.0D);
        assertEquals("netty", device.get("name"));
        assertArrayEquals(new byte[]{0x11, 0x22}, (byte[]) device.get("raw"));
        assertEquals(List.of((short) 1, (short) 2, (short) 3), device.get("values"));

        Object gps = device.get("gps");
        assertTrue(gps instanceof Map);
        assertEquals(100, ((Map<?, ?>) gps).get("longitude"));
        assertEquals(200, ((Map<?, ?>) gps).get("latitude"));
        assertEquals(0, buf.readableBytes());
    }

    @Test
    public void testSerializeRoundTrip()
    {
        Map<String, Object> gps = new LinkedHashMap<>();
        gps.put("longitude", 100);
        gps.put("latitude", 200);

        Map<String, Object> device = new LinkedHashMap<>();
        device.put("id", 0x11223344);
        device.put("speed", 0xAABB);
        device.put("temperature", 36.6D);
        device.put("name", "netty");
        device.put("raw", new byte[]{0x11, 0x22});
        device.put("values", Arrays.asList((short) 1, (short) 2, (short) 3));
        device.put("gps", gps);

        ByteBuf buf = Unpooled.buffer();
        SchemaSerializer.toByteBuf(CONTEXT, "device.Device", device, buf);

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        assertEquals(4 + 2 + 8 + 8 + 2 + 6 + 8, bytes.length);

        Map<String, Object> deserialized = SchemaSerializer.toMap(CONTEXT, "device.Device", Unpooled.wrappedBuffer(bytes));
        assertEquals(device.get("id"), deserialized.get("id"));
        assertEquals(device.get("speed"), deserialized.get("speed"));
        assertEquals(device.get("temperature"), deserialized.get("temperature"));
        assertEquals(device.get("name"), deserialized.get("name"));
        assertArrayEquals((byte[]) device.get("raw"), (byte[]) deserialized.get("raw"));
        assertEquals(device.get("values"), deserialized.get("values"));
        assertEquals(gps, deserialized.get("gps"));
    }

    @Test
    public void testSerializeWithMissingFields()
    {
        ByteBuf buf = Unpooled.buffer();
        SchemaSerializer.toByteBuf(CONTEXT, "device.Device", new LinkedHashMap<>(), buf);

        assertEquals(4 + 2 + 8 + 8 + 2 + 6 + 8, buf.readableBytes());

        Map<String, Object> device = SchemaSerializer.toMap(CONTEXT, "device.Device", buf);
        assertEquals(0, device.get("id"));
        assertEquals("", device.get("name"));
        assertEquals(List.of((short) 0, (short) 0, (short) 0), device.get("values"));
    }

    @Test
    public void testFlexibleArray()
    {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(0x7F);
        buf.writeShort(11);
        buf.writeShort(22);
        buf.writeShort(33);

        Map<String, Object> flexible = SchemaSerializer.toMap(CONTEXT, "device.Flexible", buf);

        assertEquals((short) 0x7F, flexible.get("head"));
        assertEquals(List.of(11, 22, 33), flexible.get("tail"));
        assertEquals(0, buf.readableBytes());

        ByteBuf writing = Unpooled.buffer();
        SchemaSerializer.toByteBuf(CONTEXT, "device.Flexible", flexible, writing);

        byte[] written = new byte[writing.readableBytes()];
        writing.readBytes(written);
        assertArrayEquals(new byte[]{0x7F, 0, 11, 0, 22, 0, 33}, written);
    }

    @Test
    public void testBareNameResolution()
    {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(1);
        buf.writeInt(2);

        Map<String, Object> gps = SchemaSerializer.toMap(CONTEXT, "GpsPoint", buf);
        assertEquals(1, gps.get("longitude"));
        assertEquals(2, gps.get("latitude"));
    }

    @Test
    public void testSameNamespaceReference()
    {
        SchemaContext context = SchemaContext.load("configured/sibling.xml");

        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(0xCAFE);

        Map<String, Object> packet = SchemaSerializer.toMap(context, "sibling.Packet", buf);
        assertEquals(Map.of("magic", 0xCAFEL), packet.get("header"));
    }

    @Test(expected = StructDefinitionException.class)
    public void testCycleDetection()
    {
        SchemaContext.load("configured/cycle.xml");
    }

    @Test(expected = StructDefinitionException.class)
    public void testUnknownSchema()
    {
        CONTEXT.require("device.NotExists");
    }

    @Test(expected = SerializeException.class)
    public void testSerializeNonMap()
    {
        CONTEXT.serializer("device.Device").doSerialize(new Object(), Unpooled.buffer());
    }

    @Test
    public void testCharset()
    {
        ByteBuf buf      = Unpooled.buffer();
        byte[]  gbkTitle = "\u4e32\u53e3".getBytes(Charset.forName("GBK"));
        buf.writeBytes(gbkTitle);
        buf.writeZero(8 - gbkTitle.length);
        buf.writeBytes("abc".getBytes(StandardCharsets.UTF_8));
        buf.writeZero(5);

        Map<String, Object> msg = SchemaSerializer.toMap(CONTEXT, "device.TextMsg", buf);
        assertEquals("\u4e32\u53e3", msg.get("title"));
        assertEquals("abc", msg.get("note"));

        ByteBuf writing = Unpooled.buffer();
        SchemaSerializer.toByteBuf(CONTEXT, "device.TextMsg", msg, writing);
        assertEquals(16, writing.readableBytes());

        Map<String, Object> roundTrip = SchemaSerializer.toMap(CONTEXT, "device.TextMsg", writing);
        assertEquals(msg, roundTrip);
    }

    @Test
    public void testUtf8CharFieldPreservesPaddingAndTruncation()
    {
        byte[] padded = serializeText("12345678", "netty");
        byte[] expectedPaddedNote = Arrays.copyOf("netty".getBytes(StandardCharsets.UTF_8), 8);
        assertArrayEquals(expectedPaddedNote, Arrays.copyOfRange(padded, 8, 16));

        String exact = "netty\u4E32";
        byte[] exactBytes = serializeText("12345678", exact);
        assertArrayEquals(exact.getBytes(StandardCharsets.UTF_8), Arrays.copyOfRange(exactBytes, 8, 16));

        String oversized = "1234567\u4E2D";
        byte[] truncated = serializeText("12345678", oversized);
        byte[] expectedTruncatedNote = Arrays.copyOf(oversized.getBytes(StandardCharsets.UTF_8), 8);
        assertArrayEquals(expectedTruncatedNote, Arrays.copyOfRange(truncated, 8, 16));
    }

    @Test
    public void testUtf8CharFieldPreservesCharactersAfterAnUnpairedHighSurrogate()
    {
        byte[] serialized = serializeText("12345678", "\uD800\u4E2D");
        assertArrayEquals(new byte[]{0x3F, (byte) 0xE4, (byte) 0xB8, (byte) 0xAD, 0, 0, 0, 0},
                          Arrays.copyOfRange(serialized, 8, 16));
    }

    private byte[] serializeText(String title, String note)
    {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("title", title);
        message.put("note", note);

        ByteBuf writing = Unpooled.buffer(16);
        try {
            SchemaSerializer.toByteBuf(CONTEXT, "device.TextMsg", message, writing);
            assertEquals(16, writing.readableBytes());
            byte[] bytes = new byte[writing.readableBytes()];
            writing.readBytes(bytes);
            return bytes;
        }
        finally {
            writing.release();
        }
    }

    @Test(expected = StructDefinitionException.class)
    public void testCharsetOnNonCharFieldRejected()
    {
        SchemaContext.load("configured/invalid-charset.xml");
    }

    @Test(expected = TooLessBytesException.class)
    public void testTooLessBytesOnBasicField()
    {
        SchemaSerializer.toMap(CONTEXT, "device.Device", Unpooled.wrappedBuffer(new byte[2]));
    }

    @Test(expected = TooLessBytesException.class)
    public void testTooLessBytesOnCharField()
    {
        ByteBuf buf = Unpooled.buffer();
        buf.writeIntLE(1);
        buf.writeShortLE(2);
        buf.writeDoubleLE(3.0D);
        buf.writeBytes(new byte[4]);

        SchemaSerializer.toMap(CONTEXT, "device.Device", buf);
    }

    @Test(expected = StructDefinitionException.class)
    public void testAmbiguousBareNameRejected()
    {
        SchemaContext context = SchemaContext.load("configured/ambiguous-a.xml", "configured/ambiguous-b.xml");
        context.require("Point");
    }

    @Test(expected = StructDefinitionException.class)
    public void testUnknownBasicTypeRejected()
    {
        SchemaContext.load("configured/unknown-type.xml");
    }

    @Test(expected = StructDefinitionException.class)
    public void testInvalidCharsetValueRejected()
    {
        SchemaContext.load("configured/invalid-charset-value.xml");
    }

    @Test(expected = StructDefinitionException.class)
    public void testCharMissingLengthRejected()
    {
        SchemaContext.load("configured/invalid-char-nolength.xml");
    }

    @Test(expected = StructDefinitionException.class)
    public void testTypeAndSchemaReferenceBothDeclaredRejected()
    {
        SchemaContext.load("configured/invalid-both.xml");
    }

    @Test
    public void testFixedSchemaArrayRoundTrip()
    {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(2);
        buf.writeInt(11);
        buf.writeInt(12);
        buf.writeInt(21);
        buf.writeInt(22);

        Map<String, Object> track = SchemaSerializer.toMap(CONTEXT, "device.Track", buf);

        assertEquals((short) 2, track.get("count"));
        List<?> points = (List<?>) track.get("points");
        assertEquals(2, points.size());
        assertEquals(11, ((Map<?, ?>) points.get(0)).get("longitude"));
        assertEquals(12, ((Map<?, ?>) points.get(0)).get("latitude"));
        assertEquals(21, ((Map<?, ?>) points.get(1)).get("longitude"));
        assertEquals(22, ((Map<?, ?>) points.get(1)).get("latitude"));

        ByteBuf writing = Unpooled.buffer();
        SchemaSerializer.toByteBuf(CONTEXT, "device.Track", track, writing);

        Map<String, Object> roundTrip = SchemaSerializer.toMap(CONTEXT, "device.Track", writing);
        assertEquals(track, roundTrip);
    }

    @Test
    public void testFlexibleSchemaArray()
    {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(9);
        buf.writeInt(1);
        buf.writeInt(2);
        buf.writeInt(3);
        buf.writeInt(4);

        Map<String, Object> stream = SchemaSerializer.toMap(CONTEXT, "device.Stream", buf);

        assertEquals((short) 9, stream.get("head"));
        List<?> points = (List<?>) stream.get("points");
        assertEquals(2, points.size());
        assertEquals(3, ((Map<?, ?>) points.get(1)).get("longitude"));
        assertEquals(0, buf.readableBytes());

        ByteBuf writing = Unpooled.buffer();
        SchemaSerializer.toByteBuf(CONTEXT, "device.Stream", stream, writing);

        byte[] written = new byte[writing.readableBytes()];
        writing.readBytes(written);
        assertArrayEquals(new byte[]{9, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4}, written);
    }

    @Test
    public void testDefaultEndianIsBigEndian()
    {
        SchemaContext context = SchemaContext.load("configured/default-endian.xml");

        Map<String, Object> map = SchemaSerializer.toMap(
                context, "defs.NoEndian", Unpooled.wrappedBuffer(new byte[]{0x11, 0x22, 0x33, 0x44}));
        assertEquals(0x11223344, map.get("v"));
    }

    @Test
    public void testFilePathLoading() throws Exception
    {
        Path tempFile = Files.createTempFile("struct-config", ".xml");
        try {
            Files.writeString(tempFile, """
                    <?xml version="1.0" encoding="UTF-8"?>
                    <structs namespace="temp">
                        <struct name="T" endian="BE">
                            <field name="v" type="cint"/>
                        </struct>
                    </structs>
                    """);

            SchemaContext context = SchemaContext.load(tempFile.toAbsolutePath().toString());
            Map<String, Object> map = SchemaSerializer.toMap(
                    context, "temp.T", Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 7}));
            assertEquals(7, map.get("v"));
        }
        finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void testJsonAndYamlConfigLoading()
    {
        SchemaContext context = SchemaContext.load("configured/device.json", "configured/geo.yml");

        ByteBuf flexibleBuffer = Unpooled.buffer();
        flexibleBuffer.writeByte(0x7F);
        flexibleBuffer.writeShort(11);
        flexibleBuffer.writeShort(22);
        Map<String, Object> flexible = SchemaSerializer.toMap(context, "device.Flexible", flexibleBuffer);
        assertEquals(List.of(11, 22), flexible.get("tail"));

        ByteBuf flexibleWriting = Unpooled.buffer();
        SchemaSerializer.toByteBuf(context, "device.Flexible", flexible, flexibleWriting);
        byte[] flexibleBytes = new byte[flexibleWriting.readableBytes()];
        flexibleWriting.readBytes(flexibleBytes);
        assertArrayEquals(new byte[]{0x7F, 0, 11, 0, 22}, flexibleBytes);

        ByteBuf gpsBuffer = Unpooled.buffer();
        gpsBuffer.writeInt(100);
        gpsBuffer.writeInt(200);
        assertEquals(Map.of("longitude", 100, "latitude", 200),
                     SchemaSerializer.toMap(context, "geo.GpsPoint", gpsBuffer));
        assertEquals(Map.of(), SchemaSerializer.toMap(context, "device.Empty", Unpooled.EMPTY_BUFFER));
    }

    @Test
    public void testUnknownJsonSchemaFieldRejected()
    {
        assertThrows(StructDefinitionException.class, () -> SchemaContext.load("configured/invalid-property.json"));
    }

    @Test
    public void testDuplicateJsonSchemaFieldRejected()
    {
        assertThrows(StructDefinitionException.class, () -> SchemaContext.load("configured/duplicate-property.json"));
    }

    @Test
    public void testDuplicateYamlSchemaFieldRejected()
    {
        assertThrows(StructDefinitionException.class, () -> SchemaContext.load("configured/duplicate-property.yml"));
    }

    @Test
    public void testTrailingJsonRootRejected()
    {
        assertThrows(StructDefinitionException.class, () -> SchemaContext.load("configured/trailing-root.json"));
    }

    @Test
    public void testTrailingYamlDocumentRejected()
    {
        assertThrows(StructDefinitionException.class, () -> SchemaContext.load("configured/trailing-document.yml"));
    }

    @Test
    public void testExternalDtdBlockedAndIgnored()
    {
        SchemaContext context = SchemaContext.load("configured/xxe-external-dtd.xml");

        Map<String, Object> map = SchemaSerializer.toMap(
                context, "xxe.X", Unpooled.wrappedBuffer(new byte[]{0x7F}));
        assertEquals((short) 0x7F, map.get("v"));
    }

    @Test
    public void testNestedEndianIndependent()
    {
        ByteBuf buf = Unpooled.buffer();
        buf.writeIntLE(1);
        buf.writeShortLE(2);
        buf.writeDoubleLE(3.0D);
        buf.writeZero(8);
        buf.writeZero(2);
        buf.writeZero(6);
        buf.writeInt(7);
        buf.writeInt(8);

        Map<String, Object> device = SchemaSerializer.toMap(CONTEXT, "device.Device", buf);
        Map<?, ?>           gps    = (Map<?, ?>) device.get("gps");
        assertEquals(7, gps.get("longitude"));
        assertEquals(8, gps.get("latitude"));
    }
}
