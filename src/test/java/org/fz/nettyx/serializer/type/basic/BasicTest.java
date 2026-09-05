package org.fz.nettyx.serializer.type.basic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.coveragebasic.CcharProbe;
import org.fz.nettyx.coveragebasic.Culong8Probe;
import org.fz.nettyx.coveragebasic.UnexpectedIndexBasic;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.type.basic.c.signed.cchar;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;
import org.fz.nettyx.serializer.type.basic.c.signed.cshort;
import org.fz.nettyx.serializer.type.basic.c.unsigned.cuint;
import org.fz.nettyx.serializer.type.basic.c.unsigned.culong8;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.ByteOrder;

import static org.junit.Assert.*;

public class BasicTest {

    @Test
    public void equalityHashCodeAndComparisonUseValueSizeAndSignedness() {
        cint fortyTwo = new cint(42);
        cint same = new cint(42);

        assertEquals(fortyTwo, same);
        assertEquals(fortyTwo.hashCode(), same.hashCode());
        assertEquals(0, fortyTwo.compareTo(same));
        assertTrue(fortyTwo.compareTo(new cint(41)) > 0);
        assertNotEquals(fortyTwo, new cshort(42));
        assertNotEquals(fortyTwo, new cuint(42L));
        assertNotEquals(fortyTwo, null);
        assertNotEquals(fortyTwo, 42);
        assertEquals("42", fortyTwo.toString());
    }

    @Test
    public void nullValuesHaveStableEqualityButCannotBeCompared() {
        cint first = new cint((Integer) null);
        cint second = new cint((Integer) null);

        assertEquals(first, second);
        assertEquals(0, first.hashCode());
        assertThrows(IllegalArgumentException.class, () -> first.compareTo(new cint(1)));
        assertThrows(IllegalArgumentException.class, () -> new cint(1).compareTo(second));
    }

    @Test
    public void readsAndWritesBothByteOrdersAndRejectsShortBuffers() {
        ByteBuf bigEndian = Unpooled.buffer();
        ByteBuf littleEndian = Unpooled.buffer();
        try {
            new cint(0x01020304).write(bigEndian, ByteOrder.BIG_ENDIAN);
            new cint(0x01020304).write(littleEndian, ByteOrder.LITTLE_ENDIAN);

            assertEquals(Integer.valueOf(0x01020304),
                         new cint(bigEndian, ByteOrder.BIG_ENDIAN).value());
            assertEquals(Integer.valueOf(0x01020304),
                         new cint(littleEndian, ByteOrder.LITTLE_ENDIAN).value());

            ByteBuf shortBuffer = Unpooled.wrappedBuffer(new byte[3]);
            try {
                assertThrows(TooLessBytesException.class,
                             () -> new cint(shortBuffer, ByteOrder.BIG_ENDIAN));
                assertEquals(0, shortBuffer.readerIndex());
                assertThrows(TooLessBytesException.class,
                             () -> new cchar(Unpooled.EMPTY_BUFFER, ByteOrder.BIG_ENDIAN));
            }
            finally {
                shortBuffer.release();
            }
        }
        finally {
            bigEndian.release();
            littleEndian.release();
        }
    }

    @Test
    public void unsigned64RoundTripsFullRangeInBothByteOrders() {
        BigInteger twoTo63 = BigInteger.ONE.shiftLeft(63);
        BigInteger max = BigInteger.ONE.shiftLeft(64).subtract(BigInteger.ONE);
        BigInteger[] values = {
                BigInteger.ZERO,
                BigInteger.ONE,
                BigInteger.valueOf(Long.MAX_VALUE),
                twoTo63,
                max
        };

        for (ByteOrder byteOrder : new ByteOrder[]{ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN}) {
            for (BigInteger value : values) {
                ByteBuf buffer = Unpooled.buffer(8);
                try {
                    new culong8(value).write(buffer, byteOrder);
                    assertEquals(8, buffer.readableBytes());
                    assertEquals(value, new culong8(buffer, byteOrder).value());
                }
                finally {
                    buffer.release();
                }
            }
        }

        assertThrows(IllegalArgumentException.class, () -> new culong8(BigInteger.valueOf(-1)));
        assertThrows(IllegalArgumentException.class, () -> {
            ByteBuf buffer = Unpooled.buffer(8);
            try {
                new culong8(BigInteger.ONE.shiftLeft(64)).write(buffer, ByteOrder.BIG_ENDIAN);
            }
            finally {
                buffer.release();
            }
        });
    }

    @Test
    public void unsigned64LazyValuePreservesRawBytesAndValueSemantics() {
        BigInteger value = new BigInteger("fedcba9876543210", 16);
        culong8 expected = new culong8(value);

        for (ByteOrder byteOrder : new ByteOrder[]{ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN}) {
            ByteBuf encoded = Unpooled.buffer(8);
            ByteBuf copied = Unpooled.buffer(8);
            try {
                expected.write(encoded, byteOrder);
                byte[] expectedBytes = new byte[8];
                encoded.getBytes(encoded.readerIndex(), expectedBytes);

                culong8 decoded = new culong8(encoded, byteOrder);
                decoded.write(copied, byteOrder);

                byte[] actualBytes = new byte[8];
                copied.readBytes(actualBytes);
                assertArrayEquals(expectedBytes, actualBytes);
                assertEquals(expected, decoded);
                assertEquals(expected.hashCode(), decoded.hashCode());
                assertEquals(value.toString(), decoded.toString());
            }
            finally {
                encoded.release();
                copied.release();
            }
        }
    }

    @Test
    public void unsigned64RejectsNullNegativeAndShortInput() {
        assertThrows(IllegalArgumentException.class, () -> new culong8(null));

        culong8 invalid = new culong8(BigInteger.ONE);
        invalid.value = null;
        assertWriteRejected(invalid);
        invalid.value = BigInteger.valueOf(-1);
        assertWriteRejected(invalid);

        for (ByteOrder order : new ByteOrder[]{ByteOrder.BIG_ENDIAN, ByteOrder.LITTLE_ENDIAN}) {
            ByteBuf shortInput = Unpooled.wrappedBuffer(new byte[7]);
            try {
                assertThrows(TooLessBytesException.class, () -> new culong8(shortInput, order));
            }
            finally {
                shortInput.release();
            }
        }
    }

    @Test
    public void basicConstructorRethrowsUnexpectedIndexErrors() {
        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{1});
        try {
            assertThrows(IndexOutOfBoundsException.class,
                         () -> new UnexpectedIndexBasic(input, ByteOrder.BIG_ENDIAN));
        }
        finally {
            input.release();
        }
    }

    @Test
    public void optimizedBasicConstructorsRetainReadImplementations() {
        ByteBuf charInput = Unpooled.wrappedBuffer(new byte[]{65});
        ByteBuf unsignedLongInput = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 0, 0, 0, 0, 1});
        try {
            assertEquals(Byte.valueOf((byte) 65), new CcharProbe().readProbe(charInput, ByteOrder.BIG_ENDIAN));
            assertEquals(BigInteger.ONE,
                         new Culong8Probe().readProbe(unsignedLongInput, ByteOrder.BIG_ENDIAN));
        }
        finally {
            charInput.release();
            unsignedLongInput.release();
        }
    }

    private static void assertWriteRejected(culong8 value) {
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
