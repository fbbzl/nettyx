package org.fz.nettyx.serializer.schema.type;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.coveragebasic.MissingConstructorBasic;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.serializer.basic.Basic;
import org.fz.nettyx.serializer.basic.c.unsigned.cuchar;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.fz.nettyx.serializer.basic.c.signed.clong4;
import org.fz.nettyx.serializer.basic.c.stdint.signed.cint32_t;
import org.fz.nettyx.serializer.basic.cpp.signed.cppint;
import org.fz.nettyx.serializer.basic.cpp.signed.cpplong4;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Verifies integer-family value readers and writers preserve the four-byte wire format.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-19
 */
public class BasicTypeResolverIntegerSubtypeTest
{

    private static final List<Class<? extends Basic<?>>> INTEGER_TYPES = List.of(
            cint.class, clong4.class, cint32_t.class, cppint.class, cpplong4.class);
    private static final int[] VALUES = {Integer.MIN_VALUE, -1, 0, 0x12345678, Integer.MAX_VALUE};

    @Test
    public void integerSubtypesUseTheSameWireEncodingInBothByteOrders()
    {
        for (Class<? extends Basic<?>> type : INTEGER_TYPES) {
            for (ByteOrder byteOrder : List.of(ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN)) {
                for (int value : VALUES) {
                    ByteBuf buffer = Unpooled.buffer(Integer.BYTES);
                    try {
                        BasicTypeResolver.valueWriterFor(type).write(buffer, byteOrder, value);

                        byte[] expected = ByteBuffer.allocate(Integer.BYTES).order(byteOrder).putInt(value).array();
                        byte[] actual = new byte[Integer.BYTES];
                        buffer.getBytes(buffer.readerIndex(), actual);
                        assertArrayEquals(type.getName() + " " + byteOrder + " " + value, expected, actual);
                        assertEquals(type.getName() + " " + byteOrder + " " + value, value,
                                     BasicTypeResolver.valueReaderFor(type).read(buffer, byteOrder));
                    }
                    finally {
                        buffer.release();
                    }
                }
            }
        }
    }

    @Test
    public void writeOnlyOverrideStillUsesTheCustomType()
    {
        ByteBuf buffer = Unpooled.buffer(1);
        try {
            buffer.writeByte(-2);
            assertEquals(-2, BasicTypeResolver.valueReaderFor(CustomCint.class).read(buffer, ByteOrder.BIG_ENDIAN));

            SerializeException error = assertThrows(SerializeException.class,
                    () -> BasicTypeResolver.valueWriterFor(CustomCint.class).write(buffer, ByteOrder.BIG_ENDIAN, -2));
            assertEquals("no value constructor found on basic [" + CustomCint.class.getName() + "]", error.getMessage());
            assertNull(error.getCause());
            assertEquals(1, buffer.writerIndex());
        }
        finally {
            buffer.release();
        }
    }

    @Test
    public void constructorErrorsIdentifyTheTypeAndPreserveTheCause()
    {
        SerializeException readerError = assertThrows(SerializeException.class,
                () -> BasicTypeResolver.readerFor(MissingConstructorBasic.class));
        assertEquals("basic [" + MissingConstructorBasic.class.getName() + "] reader initialization failed",
                     readerError.getMessage());
        assertTrue(readerError.getCause() instanceof NoSuchMethodException);

        SerializeException valueError = assertThrows(SerializeException.class,
                () -> BasicTypeResolver.valueBasic(cuchar.class, -1));
        assertEquals("basic [" + cuchar.class.getName() + "] instantiate failed with value [-1]", valueError.getMessage());
        assertTrue(valueError.getCause() instanceof InvocationTargetException);
        assertTrue(valueError.getCause().getCause() instanceof IllegalArgumentException);
    }

    @Test
    public void customWriterWorksWithoutAReaderConstructor()
    {
        ByteBuf buffer = Unpooled.buffer(1);
        try {
            buffer.writeByte(42);
            BasicTypeResolver.valueWriterFor(MissingConstructorBasic.class).write(buffer, ByteOrder.BIG_ENDIAN, -2);
            assertEquals(1, buffer.writerIndex());
            assertEquals(42, buffer.getByte(0));
        }
        finally {
            buffer.release();
        }
    }

    public static final class CustomCint extends cint
    {

        public CustomCint(ByteBuf buffer)
        {
            super((int) buffer.readByte());
        }

        public CustomCint(ByteBuf buffer, ByteOrder byteOrder)
        {
            this(buffer);
        }

        @Override
        public int size()
        {
            return 1;
        }

        @Override
        public void write(ByteBuf buffer, ByteOrder byteOrder)
        {
            buffer.writeByte(value());
        }
    }
}
