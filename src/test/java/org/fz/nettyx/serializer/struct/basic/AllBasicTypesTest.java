package org.fz.nettyx.serializer.struct.basic;

import cn.hutool.core.lang.ClassScanner;
import cn.hutool.core.util.ClassUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuint;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cushort;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.cppchar16_t;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.cppchar32_t;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.cppchar8_t;
import org.fz.nettyx.serializer.struct.basic.cpp.unsigned.cppuchar;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.List;

import static org.junit.Assert.*;

public class AllBasicTypesTest {

    @Test
    public void everyConcreteBasicTypeRoundTripsBothByteOrders() throws Exception {
        List<Class<?>> basicTypes = ClassScanner.scanAllPackage(
                        "org.fz.nettyx.serializer.struct.basic",
                        type -> type != Basic.class
                                && Basic.class.isAssignableFrom(type)
                                && !type.isAnonymousClass()
                                && !type.isMemberClass()
                                && type.getEnclosingClass() == null
                                && ClassUtil.isNormalClass(type))
                .stream()
                .sorted(Comparator.comparing(Class::getName))
                .toList();
        assertTrue(basicTypes.size() >= 35);

        for (Class<?> rawType : basicTypes) {
            Class<? extends Basic<?>> basicType = (Class<? extends Basic<?>>) rawType;
            Constructor<? extends Basic<?>> bufferConstructor =
                    basicType.getConstructor(ByteBuf.class, ByteOrder.class);
            for (ByteOrder byteOrder : new ByteOrder[]{ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN}) {
                int size = instantiateForSize(bufferConstructor, byteOrder).size();
                byte[] bytes = bytes(size);
                ByteBuf input = Unpooled.wrappedBuffer(bytes);
                ByteBuf output = Unpooled.buffer(size);
                try {
                    Basic<?> value = bufferConstructor.newInstance(input, byteOrder);
                    assertEquals(size, value.size());
                    assertNotNull(value.value());
                    assertNotNull(value.toString());
                    assertEquals(value, value);
                    assertEquals(value.hashCode(), value.hashCode());
                    assertEquals(0, compare(value, value));
                    value.hasSigned();
                    value.write(output, byteOrder);
                    byte[] actual = new byte[output.readableBytes()];
                    output.readBytes(actual);
                    assertArrayEquals(basicType.getName(), bytes, actual);
                }
                finally {
                    input.release();
                    output.release();
                }
            }

            Constructor<?> valueConstructor = findValueConstructor(basicType);
            Object sample = sampleValue(valueConstructor.getParameterTypes()[0]);
            Basic<?> fromValue = (Basic<?>) valueConstructor.newInstance(sample);
            assertNotNull(fromValue.value());
            for (ByteOrder byteOrder : new ByteOrder[]{ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN}) {
                ByteBuf output = Unpooled.buffer(fromValue.size());
                try {
                    fromValue.write(output, byteOrder);
                    assertEquals(fromValue.size(), output.readableBytes());
                }
                finally {
                    output.release();
                }
            }
        }
    }

    @Test
    public void unsignedBasicTypesRejectInvalidConstructionAndWriteRanges() {
        assertThrows(IllegalArgumentException.class, () -> new cushort(null));
        assertThrows(IllegalArgumentException.class, () -> new cushort(-1));
        assertWriteRejected(new cushort(65536));
        cushort invalidShort = new cushort(1);
        invalidShort.value = null;
        assertWriteRejected(invalidShort);
        invalidShort.value = -1;
        assertWriteRejected(invalidShort);

        assertThrows(IllegalArgumentException.class, () -> new cuint(null));
        assertThrows(IllegalArgumentException.class, () -> new cuint(-1L));
        assertWriteRejected(new cuint(0x1_0000_0000L));
        cuint invalidInt = new cuint(1L);
        invalidInt.value = null;
        assertWriteRejected(invalidInt);
        invalidInt.value = -1L;
        assertWriteRejected(invalidInt);

        assertThrows(IllegalArgumentException.class, () -> new cuchar(null));
        assertThrows(IllegalArgumentException.class, () -> new cuchar(-1));
        assertThrows(IllegalArgumentException.class, () -> new cuchar(256));

        cuchar invalid = new cuchar(1);
        invalid.value = null;
        assertWriteRejected(invalid);
        invalid.value = (short) -1;
        assertWriteRejected(invalid);
        invalid.value = (short) 256;
        assertWriteRejected(invalid);
    }

    @Test
    public void characterBasicTypesRenderNullAndNonNullValues() {
        cchar cChar = new cchar(0);
        cChar.value = null;
        cuchar cuChar = new cuchar(0);
        cuChar.value = null;
        cppuchar cppUChar = new cppuchar(0);
        cppUChar.value = null;

        assertEquals("", cChar.toString());
        assertEquals("", cuChar.toString());
        assertEquals("", cppUChar.toString());
        assertEquals("65", new cchar(65).toString());
        assertEquals("65", new cuchar(65).toString());
        assertEquals("", new cppchar16_t(null).toString());
        assertEquals("", new cppchar32_t(null).toString());
        assertEquals("A", new cppchar16_t('A').toString());
        assertEquals("65", new cppchar32_t(65L).toString());
    }

    @Test
    public void characterTypesDoNotExposeCharsetConversion() {
        Class<?>[] characterTypes = {
                cchar.class,
                cuchar.class,
                cppchar8_t.class,
                cppchar16_t.class,
                cppchar32_t.class
        };

        for (Class<?> characterType : characterTypes) {
            assertThrows(NoSuchMethodException.class,
                         () -> characterType.getMethod("toString", Charset.class));
        }
    }

    @Test
    public void cppCharacterTypesAreClassifiedAsUnsigned() {
        String unsignedPackage = "org.fz.nettyx.serializer.struct.basic.cpp.unsigned";

        assertEquals(unsignedPackage, cppchar8_t.class.getPackageName());
        assertEquals(unsignedPackage, cppchar16_t.class.getPackageName());
        assertEquals(unsignedPackage, cppchar32_t.class.getPackageName());
    }

    @Test
    public void char8TypeUsesUnsignedValueAndMetadata() {
        cppchar8_t value = new cppchar8_t(0xFF);

        assertFalse(value.hasSigned());
        assertEquals(0xFF, value.value().intValue());
    }

    @Test
    public void char16TypeUsesUnsignedMetadata() {
        assertFalse(new cppchar16_t(Character.MAX_VALUE).hasSigned());
    }

    @Test
    public void char32TypePreservesUnsignedValuesInBothByteOrders() {
        long expected = 0xFEDCBA98L;
        assertEquals(expected, new cppchar32_t(expected).value().longValue());

        for (ByteOrder byteOrder : new ByteOrder[]{ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN}) {
            ByteBuf buffer = Unpooled.buffer(Integer.BYTES);
            try {
                if (byteOrder == ByteOrder.LITTLE_ENDIAN) buffer.writeIntLE((int) expected);
                else                                      buffer.writeInt((int) expected);

                cppchar32_t value = new cppchar32_t(buffer, byteOrder);
                assertFalse(value.hasSigned());
                assertEquals(expected, value.value().longValue());
            }
            finally {
                buffer.release();
            }
        }
    }

    @Test
    public void char32TypeAcceptsTheFullUnsignedRangeThroughLongConstructor() {
        cppchar32_t value = new cppchar32_t(0xFFFF_FFFFL);
        assertEquals(0xFFFF_FFFFL, value.value().longValue());
    }

    private static Basic<?> instantiateForSize(
            Constructor<? extends Basic<?>> constructor,
            ByteOrder byteOrder) throws ReflectiveOperationException {
        ByteBuf input = Unpooled.wrappedBuffer(new byte[16]);
        try {
            return constructor.newInstance(input, byteOrder);
        }
        finally {
            input.release();
        }
    }

    private static byte[] bytes(int size) {
        byte[] bytes = new byte[size];
        for (int i = 0; i < size; i++) bytes[i] = (byte) (i + 1);
        return bytes;
    }

    private static Constructor<?> findValueConstructor(Class<?> basicType) {
        for (Constructor<?> constructor : basicType.getConstructors()) {
            if (constructor.getParameterCount() == 1
                    && constructor.getParameterTypes()[0] != ByteBuf.class) {
                return constructor;
            }
        }
        throw new AssertionError("value constructor not found: " + basicType);
    }

    private static Object sampleValue(Class<?> type) {
        if (type == Byte.class) return (byte) 1;
        if (type == Short.class) return (short) 1;
        if (type == Integer.class) return 1;
        if (type == Long.class) return 1L;
        if (type == Float.class) return 1.0f;
        if (type == Double.class) return 1.0d;
        if (type == Character.class) return 'A';
        if (type == Boolean.class) return true;
        if (type == BigInteger.class) return BigInteger.ONE;
        throw new AssertionError("unsupported value constructor type: " + type);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static int compare(Basic<?> left, Basic<?> right) {
        return ((Basic) left).compareTo((Basic) right);
    }

    private static void assertWriteRejected(Basic<?> value) {
        ByteBuf output = Unpooled.buffer();
        try {
            assertThrows(IllegalArgumentException.class,
                         () -> value.write(output, ByteOrder.BIG_ENDIAN));
        }
        finally {
            output.release();
        }
    }
}
