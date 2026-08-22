package org.fz.nettyx.serializer.configured;

import cn.hutool.core.date.StopWatch;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.junit.Test;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * @author fengbinbin
 * @since 2026-08-16
 */
public class ConfiguredSerializerTest {

    private static final InternalLogger log = InternalLoggerFactory.getInstance(ConfiguredSerializerTest.class);

    static final StructConfigRegistry REGISTRY = StructConfigRegistry.load(
            "configured/device.xml", "configured/geo.xml");

    @Test
    public void testConfiguredSerializerView()
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
        ConfiguredSerializer serializer = new ConfiguredSerializer(REGISTRY, "device.BenchmarkDevice");
        ConfigStructView view = serializer.newView();
        ByteBuf buffer = Unpooled.wrappedBuffer(bytes);

        assertEquals(122, bytes.length);

        // Keep JIT compilation and profile collection outside the reported throughput.
        for (int j = 0; j < 2_000_000; j++) {
            buffer.readerIndex(0);
            serializer.viewIntoUnchecked(buffer, view);
        }

        for (int i = 0; i < 10; i++) {
            StopWatch stopWatch = StopWatch.create("XML零拷贝视图任务");
            stopWatch.start();
            for (int j = 0; j < 1_000_000; j++) {
                buffer.readerIndex(0);
                serializer.viewIntoUnchecked(buffer, view);
            }
            stopWatch.stop();
            log.info(stopWatch.prettyPrint(TimeUnit.MILLISECONDS));
        }
        assertEquals(0x11223344, view.get("id"));
        assertEquals("netty", view.get("name"));
        assertEquals(bytes.length, buffer.readerIndex());
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
