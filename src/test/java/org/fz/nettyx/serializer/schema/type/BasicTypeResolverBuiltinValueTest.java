package org.fz.nettyx.serializer.schema.type;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.buffer.UnpooledByteBufAllocator;
import io.netty.buffer.UnpooledHeapByteBuf;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.basic.Basic;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

/**
 * Verifies direct value handlers for every built-in basic wire type.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-19
 */
public class BasicTypeResolverBuiltinValueTest
{

    private static final List<String> BUILTIN_TYPES = List.of(
            "cchar", "cint8_t", "cuchar", "cppchar8_t", "cppuchar", "cuint8_t",
            "cshort", "cint16_t", "cppshort", "cushort", "cuint16_t", "cppushort",
            "cint", "clong4", "cint32_t", "cppint", "cpplong4",
            "cuint", "culong4", "cuint32_t", "cppuint", "cppulong4", "cppchar32_t",
            "clong8", "clonglong", "cint64_t", "cpplong8", "cpplonglong",
            "culong8", "culonglong", "cuint64_t", "cppulong8", "cppulonglong",
            "cfloat", "cppfloat", "cdouble", "cppdouble", "cppbool", "cppchar16_t");

    @Test
    public void duplicateRegistrationKeepsTheFirstType()
    {
        Class<? extends Basic<?>> first = BasicTypeResolver.resolve("cint");
        Class<? extends Basic<?>> incoming = BasicTypeResolver.resolve("cshort");
        // Seed a collision without changing the shared registry or adding a fixture class.
        Map<String, Class<? extends Basic<?>>> types = new HashMap<>(Map.of(incoming.getSimpleName(), first));
        BasicTypeResolver.registerBasicTypes(types, Set.of(incoming));
        assertSame(first, types.get(incoming.getSimpleName()));
    }

    @Test
    public void everyBuiltInTypeHasADirectValueHandler()
    {
        assertNull(BasicTypeResolver.valueWireTypeFor(null));
        assertEquals(39, BUILTIN_TYPES.size());
        for (String name : BUILTIN_TYPES) {
            Class<? extends Basic<?>> basicClass = BasicTypeResolver.resolve(name);
            assertNotNull(name, BasicTypeResolver.valueWireTypeFor(basicClass));
        }
    }

    @Test
    public void directHandlersMatchEachBuiltInBasicWireImplementation() throws Exception
    {
        for (String name : BUILTIN_TYPES) {
            Class<? extends Basic<?>> basicClass = BasicTypeResolver.resolve(name);
            Constructor<?> valueConstructor = findValueConstructor(basicClass);
            Object sample = sampleValue(valueConstructor.getParameterTypes()[0]);
            Basic<?> wrapped = (Basic<?>) valueConstructor.newInstance(sample);

            for (ByteOrder byteOrder : List.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)) {
                ByteBuf expected = Unpooled.buffer(wrapped.size());
                ByteBuf actual = Unpooled.buffer(wrapped.size());
                try {
                    wrapped.write(expected, byteOrder);
                    BasicTypeResolver.valueWriterFor(basicClass).write(actual, byteOrder, sample);

                    byte[] expectedBytes = readableBytes(expected);
                    assertArrayEquals(name + " " + byteOrder, expectedBytes, readableBytes(actual));

                    ByteBuf input = Unpooled.wrappedBuffer(expectedBytes);
                    try {
                        assertEquals(name + " " + byteOrder, wrapped.value(),
                                     BasicTypeResolver.valueReaderFor(basicClass).read(input, byteOrder));
                    }
                    finally {
                        input.release();
                    }
                }
                finally {
                    expected.release();
                    actual.release();
                }
            }
        }
    }

    @Test
    public void readersKeepShortInputErrorsForEveryBuiltInType()
    {
        for (String name : BUILTIN_TYPES) {
            Class<? extends Basic<?>> basicClass = BasicTypeResolver.resolve(name);
            int size = BasicTypeResolver.sizeOf(basicClass);
            for (ByteOrder byteOrder : List.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)) {
                ByteBuf input = Unpooled.buffer(size - 1);
                try {
                    input.writeZero(size - 1);
                    TooLessBytesException error = assertThrows(TooLessBytesException.class,
                            () -> BasicTypeResolver.valueReaderFor(basicClass).read(input, byteOrder));
                    assertEquals(size, error.getExpectLength());
                    assertEquals(size - 1, error.getProvideLength());
                }
                finally {
                    input.release();
                }
            }
        }
    }

    @Test
    public void unsignedWritersPreserveNumericConversionAndRejectOutOfRangeValues()
    {
        assertInvalid("cuchar", null);
        assertInvalid("cuchar", -1);
        assertInvalid("cuchar", 256);
        assertInvalid("cppchar8_t", 0x1_0000_0000L);
        assertInvalid("cppuchar", BigInteger.ONE.shiftLeft(64));
        assertInvalid("cuint8_t", null);

        assertInvalid("cushort", null);
        assertInvalid("cushort", -1);
        assertInvalid("cushort", 65536);
        assertInvalid("cppushort", 0x1_0000_0000L);
        assertInvalid("cuint16_t", null);

        assertInvalid("cuint", null);
        assertInvalid("cuint", -1L);
        assertInvalid("cuint", 0x1_0000_0000L);
        assertInvalid("cppuint", BigInteger.ONE.shiftLeft(64));
        assertInvalid("cppchar32_t", null);

        assertInvalid("culong8", null);
        assertInvalid("culong8", BigInteger.valueOf(-1));
        assertInvalid("cppulonglong", BigInteger.ONE.shiftLeft(64));
    }

    @Test
    public void unsignedDecimalConversionIsBoundedBeforeTruncation()
    {
        BigDecimal hugeExponent = new BigDecimal("1E+1000000000");
        assertEncoded("cuchar", new BigDecimal("255.9"), ByteOrder.BIG_ENDIAN, new byte[]{(byte) 0xFF});
        assertEncoded("cuchar", new BigDecimal("-0.5"), ByteOrder.BIG_ENDIAN, new byte[]{0});
        assertEncoded("cuint", new BigDecimal("4294967295.9"), ByteOrder.BIG_ENDIAN,
                      ByteBuffer.allocate(Integer.BYTES).putInt(-1).array());
        assertEncoded("culong8", new BigDecimal("18446744073709551615.9"), ByteOrder.BIG_ENDIAN,
                      ByteBuffer.allocate(Long.BYTES).putLong(-1L).array());
        assertInvalid("cuchar", new BigDecimal("-1.0"));
        assertInvalid("cuchar", hugeExponent);
        assertInvalid("cuint", hugeExponent);
        assertInvalid("culong8", hugeExponent);
    }

    @Test
    public void numericWritersAcceptTextAndCheckUnsignedBoundariesBeforeWriting()
    {
        ByteOrder order = ByteOrder.LITTLE_ENDIAN;
        assertEncoded("clong8", "42", order, ByteBuffer.allocate(Long.BYTES).order(order).putLong(42).array());
        assertEncoded("cfloat", "1.5", order, ByteBuffer.allocate(Float.BYTES).order(order).putFloat(1.5F).array());
        assertEncoded("cdouble", "1.5", order, ByteBuffer.allocate(Double.BYTES).order(order).putDouble(1.5).array());
        assertEncoded("cuchar", "255.9", order, new byte[]{(byte) 0xFF});
        assertEncoded("cuchar", BigInteger.valueOf(255), order, new byte[]{(byte) 0xFF});
        assertEncoded("culong8", 42L, order, ByteBuffer.allocate(Long.BYTES).order(order).putLong(42).array());

        assertInvalid("cuchar", BigInteger.valueOf(-1));
        assertEncoded("cuchar", "", order, new byte[]{0});
        assertInvalid("culong8", null);
        assertInvalid("culong8", new BigDecimal("-1.0"));
    }

    @Test
    public void readerReportsIndexErrorsWhenTheBackingBufferWasShrunk()
    {
        // The heap buffer checks its backing array even when the slice retains its original indices.
        ByteBuf backing = new UnpooledHeapByteBuf(UnpooledByteBufAllocator.DEFAULT, Integer.BYTES, Integer.BYTES);
        try {
            backing.writeInt(42);
            ByteBuf view = backing.slice();
            backing.capacity(0);
            TooLessBytesException error = assertThrows(TooLessBytesException.class,
                    () -> BasicTypeResolver.valueReaderFor(BasicTypeResolver.resolve("cint"))
                            .read(view, ByteOrder.BIG_ENDIAN));
            assertEquals(Integer.BYTES, error.getExpectLength());
            assertEquals(Integer.BYTES, error.getProvideLength());
            assertEquals(0, view.readerIndex());
        }
        finally {
            backing.release();
        }
    }

    @Test
    public void unsigned32And64BitHandlersPreserveMaximumValues()
    {
        BigInteger maxUnsigned64 = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);
        for (ByteOrder byteOrder : List.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)) {
            for (String name : List.of("cuchar", "cppchar8_t", "cppuchar", "cuint8_t")) {
                assertRoundTrip(name, (short) 0, byteOrder, new byte[]{0});
                assertRoundTrip(name, (short) 0xFF, byteOrder, new byte[]{(byte) 0xFF});
            }
            for (String name : List.of("cushort", "cuint16_t", "cppushort")) {
                assertRoundTrip(name, 0, byteOrder,
                                ByteBuffer.allocate(Short.BYTES).order(byteOrder).putShort((short) 0).array());
                assertRoundTrip(name, 0xFFFF, byteOrder,
                                ByteBuffer.allocate(Short.BYTES).order(byteOrder).putShort((short) -1).array());
            }
            for (String name : List.of("cuint", "culong4", "cuint32_t", "cppuint", "cppulong4", "cppchar32_t"))
                assertRoundTrip(name, 0xFFFF_FFFFL, byteOrder,
                                ByteBuffer.allocate(Integer.BYTES).order(byteOrder).putInt(-1).array());

            byte[] maxBytes = ByteBuffer.allocate(Long.BYTES).order(byteOrder).putLong(-1L).array();
            for (String name : List.of("culong8", "culonglong", "cuint64_t", "cppulong8", "cppulonglong"))
                assertRoundTrip(name, maxUnsigned64, byteOrder, maxBytes);

            assertRoundTrip("cppbool", true, byteOrder, new byte[]{1});
            ByteBuf nonZeroBoolean = Unpooled.wrappedBuffer(new byte[]{(byte) 0x80});
            try {
                assertEquals(Boolean.TRUE, BasicTypeResolver.valueReaderFor(BasicTypeResolver.resolve("cppbool"))
                        .read(nonZeroBoolean, byteOrder));
            }
            finally {
                nonZeroBoolean.release();
            }

            for (char value : new char[]{0, '\uD800', Character.MAX_VALUE})
                assertRoundTrip("cppchar16_t", value, byteOrder,
                                ByteBuffer.allocate(Character.BYTES).order(byteOrder).putChar(value).array());
        }
    }

    private static void assertRoundTrip(String typeName, Object value, ByteOrder byteOrder, byte[] expectedBytes)
    {
        Class<? extends Basic<?>> basicClass = BasicTypeResolver.resolve(typeName);
        ByteBuf output = Unpooled.buffer(expectedBytes.length);
        ByteBuf input = Unpooled.wrappedBuffer(expectedBytes);
        try {
            BasicTypeResolver.valueWriterFor(basicClass).write(output, byteOrder, value);
            assertArrayEquals(typeName + " " + byteOrder, expectedBytes, readableBytes(output));
            assertEquals(value, BasicTypeResolver.valueReaderFor(basicClass).read(input, byteOrder));
        }
        finally {
            output.release();
            input.release();
        }
    }

    private static void assertInvalid(String typeName, Object value)
    {
        Class<? extends Basic<?>> basicClass = BasicTypeResolver.resolve(typeName);
        ByteBuf output = Unpooled.buffer();
        try {
            assertThrows(IllegalArgumentException.class,
                         () -> BasicTypeResolver.valueWriterFor(basicClass).write(output, ByteOrder.BIG_ENDIAN, value));
            assertEquals(0, output.writerIndex());
        }
        finally {
            output.release();
        }
    }

    private static void assertEncoded(String typeName, Object value, ByteOrder byteOrder, byte[] expectedBytes)
    {
        Class<? extends Basic<?>> basicClass = BasicTypeResolver.resolve(typeName);
        ByteBuf output = Unpooled.buffer(expectedBytes.length);
        try {
            BasicTypeResolver.valueWriterFor(basicClass).write(output, byteOrder, value);
            assertArrayEquals(typeName, expectedBytes, readableBytes(output));
        }
        finally {
            output.release();
        }
    }

    private static Constructor<?> findValueConstructor(Class<?> basicClass)
    {
        for (Constructor<?> constructor : basicClass.getConstructors())
            if (constructor.getParameterCount() == 1) return constructor;
        throw new AssertionError("value constructor not found: " + basicClass.getName());
    }

    private static Object sampleValue(Class<?> type)
    {
        if (type == Byte.class) return (byte) 1;
        if (type == Short.class) return (short) 1;
        if (type == Integer.class) return 1;
        if (type == Long.class) return 1L;
        if (type == Float.class) return 1.0F;
        if (type == Double.class) return 1.0D;
        if (type == Character.class) return 'A';
        if (type == Boolean.class) return true;
        if (type == BigInteger.class) return BigInteger.ONE;
        throw new AssertionError("unsupported value constructor type: " + type);
    }

    private static byte[] readableBytes(ByteBuf buffer)
    {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
    }
}
