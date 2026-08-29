package org.fz.nettyx.serializer.configured;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.configured.codec.ConfiguredStructCodec;
import org.fz.nettyx.serializer.configured.type.BasicTypeResolver;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;
import org.fz.nettyx.serializer.struct.basic.c.signed.cdouble;
import org.fz.nettyx.serializer.struct.basic.c.signed.cfloat;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong8;
import org.fz.nettyx.serializer.struct.basic.c.signed.cshort;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cushort;
import org.fz.nettyx.serializer.struct.basic.cpp.cppbool;
import org.junit.Test;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ConfiguredSerializerCoverageTest {

    private static final StructConfigRegistry REGISTRY = StructConfigRegistry.load(
            "configured/device.xml", "configured/geo.xml");
    private static final ConfiguredStructCodec CODEC = new ConfiguredStructCodec(REGISTRY);

    @Test
    public void reusableStructUpdatesValuesInPlaceAndRegularMapsAreReplaced()
    {
        Map<String, Object> reusable = ConfiguredSerializer.newReusableStruct(REGISTRY, "device.Device");
        ConfiguredSerializer.deserializeInto(REGISTRY, "device.Device", deviceBuffer(1, "first", new byte[]{ 1, 2 }), reusable);

        byte[] raw = (byte[]) reusable.get("raw");
        List<?> values = (List<?>) reusable.get("values");
        Map<?, ?> gps = (Map<?, ?>) reusable.get("gps");

        ConfiguredSerializer.deserializeInto(REGISTRY, "device.Device", deviceBuffer(2, "first", new byte[]{ 3, 4 }), reusable);

        assertEquals(2, reusable.get("id"));
        assertSame(raw, reusable.get("raw"));
        assertSame(values, reusable.get("values"));
        assertSame(gps, reusable.get("gps"));
        assertArrayEquals(new byte[]{ 3, 4 }, (byte[]) reusable.get("raw"));

        reusable.put("marker", true);
        ConfiguredSerializer.deserializeInto(REGISTRY, "device.Device", deviceBuffer(3, "second", new byte[]{ 5, 6 }), reusable);
        assertEquals(3, reusable.get("id"));
        assertTrue(reusable.containsKey("marker"));
        assertEquals(Boolean.TRUE, reusable.remove("marker"));
        reusable.clear();
        assertTrue(reusable.isEmpty());

        Map<String, Object> regular = new LinkedHashMap<>(Map.of("stale", true));
        ConfiguredSerializer.deserializeInto(REGISTRY, "device.Device", deviceBuffer(4, "plain", new byte[]{ 7, 8 }), regular);
        assertFalse(regular.containsKey("stale"));
        assertEquals(4, regular.get("id"));
    }

    @Test
    public void flexibleArraysViewsDirectBuffersAndArrayInputsFollowTheirContracts()
    {
        Map<String, Object> reusable = ConfiguredSerializer.newReusableStruct(REGISTRY, "device.Flexible");
        ConfiguredSerializer.deserializeInto(REGISTRY, "device.Flexible", flexibleBuffer(11, 22, 33), reusable);
        List<?> tail = (List<?>) reusable.get("tail");
        ConfiguredSerializer.deserializeInto(REGISTRY, "device.Flexible", flexibleBuffer(44), reusable);
        assertSame(tail, reusable.get("tail"));
        assertEquals(List.of(44), reusable.get("tail"));

        ConfigStructView view = ConfiguredSerializer.newView(REGISTRY, "device.Device");
        ConfiguredSerializer.viewInto(REGISTRY, "device.Device", deviceBuffer(5, "view", new byte[]{ 9, 10 }), view);
        assertEquals(5, view.get("id"));
        assertThrows(SerializeException.class, () -> ConfiguredSerializer.newView(REGISTRY, "device.Flexible"));
        assertThrows(SerializeException.class, () -> ConfiguredSerializer.viewInto(
                REGISTRY, "device.Device", deviceBuffer(6, "other", new byte[]{ 1, 1 }),
                ConfiguredSerializer.newView(REGISTRY, "device.BenchmarkDevice")));
        assertThrows(TooLessBytesException.class,
                () -> ConfiguredSerializer.viewInto(REGISTRY, "device.Device", Unpooled.wrappedBuffer(new byte[1]),
                        ConfiguredSerializer.newView(REGISTRY, "device.Device")));

        ByteBuf direct = Unpooled.directBuffer(4);
        try {
            direct.writeBytes(new byte[]{ 'o', 'k', 0, 0 });
            assertEquals("ok", CODEC.readField(
                    ConfigField.charField("text", 4, StandardCharsets.UTF_8), ByteOrder.BIG_ENDIAN, direct));
        }
        finally {
            direct.release();
        }

        ConfigField shortArray = ConfigField.basicArray("values", cshort.class, 2, false);
        ByteBuf writing = Unpooled.buffer();
        CODEC.writeArray(shortArray, new LinkedHashSet<>(List.of(1, 2)), ByteOrder.LITTLE_ENDIAN, writing);
        assertArrayEquals(new byte[]{ 1, 0, 2, 0 }, readableBytes(writing));
        writing.clear();
        CODEC.writeArray(shortArray, new int[]{ 3, 4 }, ByteOrder.LITTLE_ENDIAN, writing);
        assertArrayEquals(new byte[]{ 3, 0, 4, 0 }, readableBytes(writing));
        assertThrows(SerializeException.class, () -> CODEC.writeArray(shortArray, 1, ByteOrder.LITTLE_ENDIAN, Unpooled.buffer()));
    }

    @Test
    public void reusableTargetsMustMatchTheirStructAndGenericCollectionsUseIterators()
    {
        Map<String, Object> deviceTarget = ConfiguredSerializer.newReusableStruct(REGISTRY, "device.Device");
        assertThrows(SerializeException.class, () -> ConfiguredSerializer.deserializeInto(
                REGISTRY, "device.Flexible", flexibleBuffer(1), deviceTarget));

        Map<String, Object> wrongNestedTarget = ConfiguredSerializer.newReusableStruct(REGISTRY, "device.Device");
        deviceTarget.put("gps", wrongNestedTarget);
        ConfiguredSerializer.deserializeInto(REGISTRY, "device.Device", deviceBuffer(7, "nested", new byte[]{ 1, 2 }), deviceTarget);
        Map<?, ?> gps = (Map<?, ?>) deviceTarget.get("gps");
        assertNotSame(wrongNestedTarget, gps);
        assertEquals(100, gps.get("longitude"));
        assertEquals(200, gps.get("latitude"));

        Collection<Integer> iteratorOnly = new AbstractCollection<>() {
            @Override
            public Iterator<Integer> iterator()
            {
                return List.of(5, 6).iterator();
            }

            @Override
            public int size()
            {
                return 2;
            }

            @Override
            public Object[] toArray()
            {
                throw new AssertionError("writeArray must iterate generic collections");
            }
        };
        ByteBuf writing = Unpooled.buffer();
        CODEC.writeArray(ConfigField.basicArray("values", cshort.class, 2, false), iteratorOnly, ByteOrder.LITTLE_ENDIAN, writing);
        assertArrayEquals(new byte[]{ 5, 0, 6, 0 }, readableBytes(writing));
    }

    @Test
    public void basicReadersReportUnderflowAndFallbackTypesStillUseTheirConstructors()
    {
        assertUnderflow(BasicTypeResolver.valueReaderFor(cchar.class));
        assertUnderflow(BasicTypeResolver.valueReaderFor(cuchar.class));
        assertUnderflow(BasicTypeResolver.valueReaderFor(cshort.class));
        assertUnderflow(BasicTypeResolver.valueReaderFor(cushort.class));
        assertUnderflow(BasicTypeResolver.valueReaderFor(cint.class));
        assertUnderflow(BasicTypeResolver.valueReaderFor(clong8.class));
        assertUnderflow(BasicTypeResolver.valueReaderFor(cfloat.class));
        assertUnderflow(BasicTypeResolver.valueReaderFor(cdouble.class));

        assertEquals(Boolean.TRUE, BasicTypeResolver.valueBasic(cppbool.class, true).value());
        assertEquals(1, BasicTypeResolver.sizeOf(cppbool.class));
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{ 1 });
        assertEquals(Boolean.TRUE, BasicTypeResolver.readBasic(cppbool.class, ByteOrder.BIG_ENDIAN, input).value());
    }

    private static void assertUnderflow(BasicTypeResolver.BasicValueReader reader)
    {
        assertThrows(TooLessBytesException.class, () -> reader.read(Unpooled.EMPTY_BUFFER, ByteOrder.BIG_ENDIAN));
    }

    private static ByteBuf deviceBuffer(int id, String name, byte[] raw)
    {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeIntLE(id);
        buffer.writeShortLE(0xAABB);
        buffer.writeDoubleLE(36.6D);
        byte[] nameBytes = name.getBytes(StandardCharsets.UTF_8);
        buffer.writeBytes(nameBytes, 0, Math.min(nameBytes.length, 8));
        buffer.writeZero(8 - Math.min(nameBytes.length, 8));
        buffer.writeBytes(raw);
        buffer.writeShortLE(1);
        buffer.writeShortLE(2);
        buffer.writeShortLE(3);
        buffer.writeInt(100);
        buffer.writeInt(200);
        return buffer;
    }

    private static ByteBuf flexibleBuffer(int... values)
    {
        ByteBuf buffer = Unpooled.buffer();
        buffer.writeByte(0x7F);
        for (int value : values) buffer.writeShort(value);
        return buffer;
    }

    private static byte[] readableBytes(ByteBuf buffer)
    {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
    }
}
