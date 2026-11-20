package org.fz.nettyx.serializer.schema;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.schema.type.BasicTypeResolver;
import org.fz.nettyx.serializer.basic.c.signed.cchar;
import org.fz.nettyx.serializer.basic.c.signed.cdouble;
import org.fz.nettyx.serializer.basic.c.signed.cfloat;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.fz.nettyx.serializer.basic.c.signed.clong8;
import org.fz.nettyx.serializer.basic.c.signed.cshort;
import org.fz.nettyx.serializer.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.basic.c.unsigned.cushort;
import org.fz.nettyx.serializer.basic.cpp.cppbool;
import org.junit.Test;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Exercises independent results, flexible arrays, direct buffers, and basic type conversion.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-09-02
 */
public class SchemaSerializerCoverageTest
{

    private static final SchemaContext  CONTEXT = SchemaContext.load(
            "configured/device.xml", "configured/geo.xml");
    private static final SchemaSerializer SERIALIZER = CONTEXT.serializer("device.Device");

    @Test
    public void deserializingAgainKeepsPriorResultsIndependent()
    {
        ByteBuf firstInput = deviceBuffer(1, "first", new byte[]{1, 2});
        ByteBuf secondInput = deviceBuffer(2, "second", new byte[]{3, 4});
        try {
            Map<String, Object> first  = SERIALIZER.doDeserialize(firstInput);
            Map<String, Object> second = SERIALIZER.doDeserialize(secondInput);

            assertNotSame(first, second);
            assertNotSame(first.get("raw"), second.get("raw"));
            assertNotSame(first.get("values"), second.get("values"));
            assertNotSame(first.get("gps"), second.get("gps"));
            assertEquals(List.of("id", "speed", "temperature", "name", "raw", "values", "gps"),
                         List.copyOf(first.keySet()));
            assertEquals(1, first.get("id"));
            assertEquals(2, second.get("id"));
            assertArrayEquals(new byte[]{1, 2}, (byte[]) first.get("raw"));
            assertArrayEquals(new byte[]{3, 4}, (byte[]) second.get("raw"));
            assertEquals(Map.of("longitude", 100, "latitude", 200), first.get("gps"));
        }
        finally {
            firstInput.release();
            secondInput.release();
        }
    }

    @Test
    public void nullMapSerializesAsZeroFilledFixedLengthBytes()
    {
        ByteBuf writing = Unpooled.buffer();
        try {
            SERIALIZER.doSerialize(null, writing);

            assertEquals(CONTEXT.fixedSizeOf(SERIALIZER.getSchema()), writing.readableBytes());
            byte[] actual = new byte[writing.readableBytes()];
            writing.readBytes(actual);
            assertArrayEquals(new byte[actual.length], actual);
        }
        finally {
            writing.release();
        }
    }

    @Test
    public void flexibleArraysRejectZeroSizedSchemaElements()
    {
        SchemaContext context = SchemaContext.load("configured/device.json", "configured/geo.yml");
        Schema.SchemaField field = Schema.SchemaField.schemaArray("empty", "Empty", null, true);
        field.resolvedSchemaRef("device.Empty");
        ByteBuf input = Unpooled.buffer().writeByte(1);

        try {
            SerializeException error = assertThrows(SerializeException.class,
                                                     () -> context.serializer("device.Device").readField(
                                                             field, ByteOrder.BIG_ENDIAN, input));
            assertTrue(error.getMessage(), error.getMessage().contains("empty"));
        }
        finally {
            input.release();
        }
    }

    @Test
    public void flexibleArraysViewsDirectBuffersAndArrayInputsFollowTheirContracts()
    {
        Map<String, Object> first = SchemaSerializer.toMap(CONTEXT, "device.Flexible", flexibleBuffer(11, 22, 33));
        Map<String, Object> second = SchemaSerializer.toMap(CONTEXT, "device.Flexible", flexibleBuffer(44));
        assertEquals(List.of(11, 22, 33), first.get("tail"));
        assertEquals(List.of(44), second.get("tail"));

        SchemaView view = CONTEXT.serializer("device.Device")
                                  .view(deviceBuffer(5, "view", new byte[]{9, 10}));
        assertEquals(5, view.getInt("id"));
        assertThrows(SerializeException.class,
                     () -> CONTEXT.serializer("device.Flexible").view(flexibleBuffer(1)));
        assertThrows(TooLessBytesException.class,
                     () -> CONTEXT.serializer("device.Device").view(Unpooled.wrappedBuffer(new byte[1])));

        ByteBuf direct = Unpooled.directBuffer(4);
        try {
            direct.writeBytes(new byte[]{'o', 'k', 0, 0});
            assertEquals("ok", SERIALIZER.readField(
                    Schema.SchemaField.charField("text", 4, StandardCharsets.UTF_8), ByteOrder.BIG_ENDIAN, direct));
        }
        finally {
            direct.release();
        }

        Schema.SchemaField shortArray = Schema.SchemaField.basicArray("values", cshort.class, 2, false);
        ByteBuf            writing    = Unpooled.buffer();
        SERIALIZER.writeField(shortArray, new LinkedHashSet<>(List.of(1, 2)), ByteOrder.LITTLE_ENDIAN, writing);
        assertArrayEquals(new byte[]{1, 0, 2, 0}, readableBytes(writing));
        writing.clear();
        SERIALIZER.writeField(shortArray, new int[]{3, 4}, ByteOrder.LITTLE_ENDIAN, writing);
        assertArrayEquals(new byte[]{3, 0, 4, 0}, readableBytes(writing));
        assertThrows(SerializeException.class, () -> SERIALIZER.writeField(shortArray, 1, ByteOrder.LITTLE_ENDIAN, Unpooled.buffer()));
    }

    @Test
    public void genericCollectionsUseIterators()
    {
        Collection<Integer> iteratorOnly = new AbstractCollection<>()
        {
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
        SERIALIZER.writeField(Schema.SchemaField.basicArray("values", cshort.class, 2, false), iteratorOnly, ByteOrder.LITTLE_ENDIAN, writing);
        assertArrayEquals(new byte[]{5, 0, 6, 0}, readableBytes(writing));
    }

    @Test
    public void sequentialListsAreWrittenWithoutIndexedAccess()
    {
        List<Integer> values = new LinkedList<>(List.of(5, 6))
        {
            @Override
            public Integer get(int index)
            {
                throw new AssertionError("Sequential lists must be traversed with an iterator");
            }
        };
        Schema.SchemaField field = Schema.SchemaField.basicArray("values", cshort.class, 3, false);
        ByteBuf writing = Unpooled.buffer();
        try {
            SERIALIZER.writeField(field, values, ByteOrder.LITTLE_ENDIAN, writing);
            assertArrayEquals(new byte[]{5, 0, 6, 0, 0, 0}, readableBytes(writing));
        }
        finally {
            writing.release();
        }
    }

    @Test
    public void fieldReaderReportsMissingBytes()
    {
        Schema.SchemaField bytes = Schema.SchemaField.bytesField("bytes", 2);
        assertThrows(TooLessBytesException.class,
                     () -> SERIALIZER.readField(bytes, ByteOrder.BIG_ENDIAN, Unpooled.wrappedBuffer(new byte[]{1})));
        assertThrows(TooLessBytesException.class,
                     () -> SERIALIZER.readField(Schema.SchemaField.charField("text", 2, StandardCharsets.UTF_8), ByteOrder.BIG_ENDIAN,
                                           Unpooled.wrappedBuffer(new byte[]{1})));
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
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{1});
        assertEquals(Boolean.TRUE, BasicTypeResolver.readBasic(cppbool.class, ByteOrder.BIG_ENDIAN, input).value());
    }

    @Test
    public void resolverRejectsUnknownNamesAndConvertsWriterInputs()
    {
        assertThrows(org.fz.nettyx.exception.TypeJudgmentException.class,
                     () -> BasicTypeResolver.resolve("does_not_exist"));

        ByteBuf writing = Unpooled.buffer();
        BasicTypeResolver.valueWriterFor(cint.class).write(writing, ByteOrder.BIG_ENDIAN, "42");
        BasicTypeResolver.valueWriterFor(cshort.class).write(writing, ByteOrder.LITTLE_ENDIAN, "258");
        assertEquals(42, writing.readInt());
        assertEquals((short) 258, writing.readShortLE());
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
