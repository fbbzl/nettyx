package org.fz.nettyx.serializer.configured;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;
import org.fz.nettyx.serializer.struct.basic.c.signed.cdouble;
import org.fz.nettyx.serializer.struct.basic.c.signed.cfloat;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong4;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong8;
import org.fz.nettyx.serializer.struct.basic.c.signed.cshort;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cushort;
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
 * @author fengbinbin
 * @since 2026-08-16
 */
public class ConfiguredSerializerTest {

    static final StructConfigRegistry REGISTRY = StructConfigRegistry.load(
            "configured/device.xml", "configured/geo.xml");

    @Test
    public void testConfiguredSerializer()
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
        Map<String, Object> message = ConfiguredSerializer.toStruct(REGISTRY, "device.BenchmarkDevice", reading);
        assertEquals(0x11223344, message.get("id"));
        assertEquals("netty", message.get("name"));
        ConfiguredSerializer.toByteBuf(REGISTRY, "device.BenchmarkDevice", message, writing);
        assertEquals(bytes.length, writing.readableBytes());

        assertArrayEquals(bytes, writing.array());
    }

    @Test
    public void testConfiguredSerializerView()
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
        ConfiguredSerializer serializer = new ConfiguredSerializer(REGISTRY, "device.Device");
        ConfigStructView view = serializer.newView();

        serializer.viewInto(reading, view);

        assertEquals(0x11223344, view.get("id"));
        assertEquals("netty", view.get("name"));
        assertEquals(0, reading.readableBytes());
    }

    @Test
    public void testConfiguredStructMapSerializesByNameForDifferentStruct()
    {
        ByteBuf reading = Unpooled.buffer();
        reading.writeIntLE(0x11223344);
        reading.writeShortLE(0xAABB);
        reading.writeDoubleLE(36.6D);
        reading.writeBytes(new byte[]{ 'n', 'e', 't', 't', 'y', 0, 0, 0 });
        reading.writeBytes(new byte[]{ 0x11, 0x22 });
        reading.writeShortLE(1);
        reading.writeShortLE(2);
        reading.writeShortLE(3);
        reading.writeInt(100);
        reading.writeInt(200);

        Map<String, Object> device = ConfiguredSerializer.toStruct(REGISTRY, "device.Device", reading);
        ByteBuf writing = Unpooled.buffer();

        ConfiguredSerializer.toByteBuf(REGISTRY, "device.Track", device, writing);

        byte[] actual = new byte[writing.readableBytes()];
        writing.readBytes(actual);
        assertArrayEquals(new byte[17], actual);
    }

    @Test
    public void testConfiguredStructMapSerializesSameStructWithoutChangingBytes()
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
        Map<String, Object> device = ConfiguredSerializer.toStruct(
                REGISTRY, "device.Device", Unpooled.wrappedBuffer(expected));
        ByteBuf writing = Unpooled.buffer(expected.length);

        ConfiguredSerializer.toByteBuf(REGISTRY, "device.Device", device, writing);

        byte[] actual = new byte[writing.readableBytes()];
        writing.readBytes(actual);
        assertArrayEquals(expected, actual);
    }

    @Test
    public void testBasicValueWriterPreservesWireValuesAndBounds()
    {
        ConfiguredSerializer serializer = new ConfiguredSerializer(REGISTRY, "device.Device");
        ByteBuf writing = Unpooled.buffer();

        serializer.writeField(ConfigField.basicField("v", cchar.class), -1, ByteOrder.BIG_ENDIAN, writing);
        serializer.writeField(ConfigField.basicField("v", cuchar.class), 0xFF, ByteOrder.BIG_ENDIAN, writing);
        serializer.writeField(ConfigField.basicField("v", cshort.class), 0x1234, ByteOrder.LITTLE_ENDIAN, writing);
        serializer.writeField(ConfigField.basicField("v", cushort.class), 0xABCD, ByteOrder.BIG_ENDIAN, writing);
        serializer.writeField(ConfigField.basicField("v", cint.class), 0x10203040, ByteOrder.LITTLE_ENDIAN, writing);
        serializer.writeField(ConfigField.basicField("v", clong4.class), 0x50607080, ByteOrder.BIG_ENDIAN, writing);
        serializer.writeField(ConfigField.basicField("v", clong8.class), 0x0102030405060708L, ByteOrder.LITTLE_ENDIAN, writing);
        serializer.writeField(ConfigField.basicField("v", cfloat.class), 1.0F, ByteOrder.BIG_ENDIAN, writing);
        serializer.writeField(ConfigField.basicField("v", cdouble.class), 1.0D, ByteOrder.LITTLE_ENDIAN, writing);

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
                () -> serializer.writeField(ConfigField.basicField("v", cuchar.class), 0x100, ByteOrder.BIG_ENDIAN, Unpooled.buffer()));
        assertThrows(IllegalArgumentException.class,
                () -> serializer.writeField(ConfigField.basicField("v", cushort.class), 0x1_0000, ByteOrder.BIG_ENDIAN, Unpooled.buffer()));
    }

    @Test
    public void testDeserialize()
    {
        ByteBuf buf = Unpooled.buffer();
        buf.writeIntLE(0x11223344);
        buf.writeShortLE(0xAABB);
        buf.writeDoubleLE(36.6D);
        buf.writeBytes(new byte[]{ 'n', 'e', 't', 't', 'y', 0, 0, 0 });
        buf.writeBytes(new byte[]{ 0x11, 0x22 });
        buf.writeShortLE(1);
        buf.writeShortLE(2);
        buf.writeShortLE(3);
        buf.writeInt(100);
        buf.writeInt(200);

        Map<String, Object> device = ConfiguredSerializer.toStruct(REGISTRY, "device.Device", buf);

        assertEquals(0x11223344, device.get("id"));
        assertEquals(0xAABB, device.get("speed"));
        assertEquals(36.6D, (Double) device.get("temperature"), 0.0D);
        assertEquals("netty", device.get("name"));
        assertArrayEquals(new byte[]{ 0x11, 0x22 }, (byte[]) device.get("raw"));
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
        device.put("raw", new byte[]{ 0x11, 0x22 });
        device.put("values", Arrays.asList((short) 1, (short) 2, (short) 3));
        device.put("gps", gps);

        ByteBuf buf = Unpooled.buffer();
        ConfiguredSerializer.toByteBuf(REGISTRY, "device.Device", device, buf);

        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        assertEquals(4 + 2 + 8 + 8 + 2 + 6 + 8, bytes.length);

        Map<String, Object> deserialized = ConfiguredSerializer.toStruct(REGISTRY, "device.Device", Unpooled.wrappedBuffer(bytes));
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
        ConfiguredSerializer.toByteBuf(REGISTRY, "device.Device", new LinkedHashMap<>(), buf);

        assertEquals(4 + 2 + 8 + 8 + 2 + 6 + 8, buf.readableBytes());

        Map<String, Object> device = ConfiguredSerializer.toStruct(REGISTRY, "device.Device", buf);
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

        Map<String, Object> flexible = ConfiguredSerializer.toStruct(REGISTRY, "device.Flexible", buf);

        assertEquals((short) 0x7F, flexible.get("head"));
        assertEquals(List.of(11, 22, 33), flexible.get("tail"));
        assertEquals(0, buf.readableBytes());

        ByteBuf writing = Unpooled.buffer();
        ConfiguredSerializer.toByteBuf(REGISTRY, "device.Flexible", flexible, writing);

        byte[] written = new byte[writing.readableBytes()];
        writing.readBytes(written);
        assertArrayEquals(new byte[]{ 0x7F, 0, 11, 0, 22, 0, 33 }, written);
    }

    @Test
    public void testBareNameResolution()
    {
        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(1);
        buf.writeInt(2);

        Map<String, Object> gps = ConfiguredSerializer.toStruct(REGISTRY, "GpsPoint", buf);
        assertEquals(1, gps.get("longitude"));
        assertEquals(2, gps.get("latitude"));
    }

    @Test
    public void testSameNamespaceReference()
    {
        StructConfigRegistry registry = StructConfigRegistry.load("configured/sibling.xml");

        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(0xCAFE);

        Map<String, Object> packet = ConfiguredSerializer.toStruct(registry, "sibling.Packet", buf);
        assertEquals(Map.of("magic", 0xCAFEL), packet.get("header"));
    }

    @Test(expected = StructDefinitionException.class)
    public void testCycleDetection()
    {
        StructConfigRegistry.load("configured/cycle.xml");
    }

    @Test(expected = StructDefinitionException.class)
    public void testUnknownStruct()
    {
        REGISTRY.require("device.NotExists");
    }

    @Test(expected = SerializeException.class)
    public void testSerializeNonMap()
    {
        new ConfiguredSerializer(REGISTRY, "device.Device").doSerialize(new Object(), Unpooled.buffer());
    }

    @Test
    public void testCharset()
    {
        ByteBuf buf = Unpooled.buffer();
        byte[] gbkTitle = "串口".getBytes(Charset.forName("GBK"));
        buf.writeBytes(gbkTitle);
        buf.writeZero(8 - gbkTitle.length);
        buf.writeBytes("abc".getBytes(StandardCharsets.UTF_8));
        buf.writeZero(5);

        Map<String, Object> msg = ConfiguredSerializer.toStruct(REGISTRY, "device.TextMsg", buf);
        assertEquals("串口", msg.get("title"));
        assertEquals("abc", msg.get("note"));

        ByteBuf writing = Unpooled.buffer();
        ConfiguredSerializer.toByteBuf(REGISTRY, "device.TextMsg", msg, writing);
        assertEquals(16, writing.readableBytes());

        Map<String, Object> roundTrip = ConfiguredSerializer.toStruct(REGISTRY, "device.TextMsg", writing);
        assertEquals(msg, roundTrip);
    }

    @Test(expected = StructDefinitionException.class)
    public void testCharsetOnNonCharFieldRejected()
    {
        StructConfigRegistry.load("configured/invalid-charset.xml");
    }

    @Test(expected = TooLessBytesException.class)
    public void testTooLessBytesOnBasicField()
    {
        ConfiguredSerializer.toStruct(REGISTRY, "device.Device", Unpooled.wrappedBuffer(new byte[2]));
    }

    @Test(expected = TooLessBytesException.class)
    public void testTooLessBytesOnCharField()
    {
        ByteBuf buf = Unpooled.buffer();
        buf.writeIntLE(1);
        buf.writeShortLE(2);
        buf.writeDoubleLE(3.0D);
        buf.writeBytes(new byte[4]);

        ConfiguredSerializer.toStruct(REGISTRY, "device.Device", buf);
    }

    @Test(expected = StructDefinitionException.class)
    public void testAmbiguousBareNameRejected()
    {
        StructConfigRegistry registry = StructConfigRegistry.load("configured/ambiguous-a.xml", "configured/ambiguous-b.xml");
        registry.require("Point");
    }

    @Test(expected = StructDefinitionException.class)
    public void testUnknownBasicTypeRejected()
    {
        StructConfigRegistry.load("configured/unknown-type.xml");
    }

    @Test(expected = StructDefinitionException.class)
    public void testInvalidCharsetValueRejected()
    {
        StructConfigRegistry.load("configured/invalid-charset-value.xml");
    }

    @Test(expected = StructDefinitionException.class)
    public void testCharMissingLengthRejected()
    {
        StructConfigRegistry.load("configured/invalid-char-nolength.xml");
    }

    @Test(expected = StructDefinitionException.class)
    public void testTypeAndStructBothDeclaredRejected()
    {
        StructConfigRegistry.load("configured/invalid-both.xml");
    }

    @Test
    public void testStructArrayFixedRoundTrip()
    {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(2);
        buf.writeInt(11);
        buf.writeInt(12);
        buf.writeInt(21);
        buf.writeInt(22);

        Map<String, Object> track = ConfiguredSerializer.toStruct(REGISTRY, "device.Track", buf);

        assertEquals((short) 2, track.get("count"));
        List<?> points = (List<?>) track.get("points");
        assertEquals(2, points.size());
        assertEquals(11, ((Map<?, ?>) points.get(0)).get("longitude"));
        assertEquals(12, ((Map<?, ?>) points.get(0)).get("latitude"));
        assertEquals(21, ((Map<?, ?>) points.get(1)).get("longitude"));
        assertEquals(22, ((Map<?, ?>) points.get(1)).get("latitude"));

        ByteBuf writing = Unpooled.buffer();
        ConfiguredSerializer.toByteBuf(REGISTRY, "device.Track", track, writing);

        Map<String, Object> roundTrip = ConfiguredSerializer.toStruct(REGISTRY, "device.Track", writing);
        assertEquals(track, roundTrip);
    }

    @Test
    public void testStructArrayFlexible()
    {
        ByteBuf buf = Unpooled.buffer();
        buf.writeByte(9);
        buf.writeInt(1);
        buf.writeInt(2);
        buf.writeInt(3);
        buf.writeInt(4);

        Map<String, Object> stream = ConfiguredSerializer.toStruct(REGISTRY, "device.Stream", buf);

        assertEquals((short) 9, stream.get("head"));
        List<?> points = (List<?>) stream.get("points");
        assertEquals(2, points.size());
        assertEquals(3, ((Map<?, ?>) points.get(1)).get("longitude"));
        assertEquals(0, buf.readableBytes());

        ByteBuf writing = Unpooled.buffer();
        ConfiguredSerializer.toByteBuf(REGISTRY, "device.Stream", stream, writing);

        byte[] written = new byte[writing.readableBytes()];
        writing.readBytes(written);
        assertArrayEquals(new byte[]{ 9, 0, 0, 0, 1, 0, 0, 0, 2, 0, 0, 0, 3, 0, 0, 0, 4 }, written);
    }

    @Test
    public void testDefaultEndianIsBigEndian()
    {
        StructConfigRegistry registry = StructConfigRegistry.load("configured/default-endian.xml");

        Map<String, Object> map = ConfiguredSerializer.toStruct(
                registry, "defs.NoEndian", Unpooled.wrappedBuffer(new byte[]{ 0x11, 0x22, 0x33, 0x44 }));
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

            StructConfigRegistry registry = StructConfigRegistry.load(tempFile.toAbsolutePath().toString());
            Map<String, Object> map = ConfiguredSerializer.toStruct(
                    registry, "temp.T", Unpooled.wrappedBuffer(new byte[]{ 0, 0, 0, 7 }));
            assertEquals(7, map.get("v"));
        }
        finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void testExternalDtdBlockedAndIgnored()
    {
        StructConfigRegistry registry = StructConfigRegistry.load("configured/xxe-external-dtd.xml");

        Map<String, Object> map = ConfiguredSerializer.toStruct(
                registry, "xxe.X", Unpooled.wrappedBuffer(new byte[]{ 0x7F }));
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

        Map<String, Object> device = ConfiguredSerializer.toStruct(REGISTRY, "device.Device", buf);
        Map<?, ?> gps = (Map<?, ?>) device.get("gps");
        assertEquals(7, gps.get("longitude"));
        assertEquals(8, gps.get("latitude"));
    }
}
