package org.fz.nettyx.util;

import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * @author fengbinbin
 * @since 2024
 */
public class EndianKitTest {

    // ---- BE tests ----

    @Test
    public void testBEShort() {
        short val = 0x1234;
        byte[] bytes = EndianKit.BE.fromShort(val);
        assertArrayEquals(new byte[]{0x12, 0x34}, bytes);
        assertEquals(val, EndianKit.BE.toShort(bytes));
    }

    @Test
    public void testBEInt() {
        int val = 0x12345678;
        byte[] bytes = EndianKit.BE.fromInt(val);
        assertArrayEquals(new byte[]{0x12, 0x34, 0x56, 0x78}, bytes);
        assertEquals(val, EndianKit.BE.toInt(bytes));
    }

    @Test
    public void testBELong() {
        long val = 0x1234567890ABCDEFL;
        byte[] bytes = EndianKit.BE.fromLong(val);
        assertEquals(val, EndianKit.BE.toLong(bytes));
    }

    @Test
    public void testBEFloat() {
        float val = 3.14f;
        byte[] bytes = EndianKit.BE.fromFloat(val);
        assertEquals(val, EndianKit.BE.toFloat(bytes), 0.0001f);
    }

    @Test
    public void testBEDouble() {
        double val = 3.1415926535;
        byte[] bytes = EndianKit.BE.fromDouble(val);
        assertEquals(val, EndianKit.BE.toDouble(bytes), 0.0000001);
    }

    // ---- LE tests ----

    @Test
    public void testLEShort() {
        short val = 0x1234;
        byte[] bytes = EndianKit.LE.fromShort(val);
        assertArrayEquals(new byte[]{0x34, 0x12}, bytes);
        assertEquals(val, EndianKit.LE.toShort(bytes));
    }

    @Test
    public void testLEInt() {
        int val = 0x12345678;
        byte[] bytes = EndianKit.LE.fromInt(val);
        assertArrayEquals(new byte[]{0x78, 0x56, 0x34, 0x12}, bytes);
        assertEquals(val, EndianKit.LE.toInt(bytes));
    }

    @Test
    public void testLELong() {
        long val = 0x1234567890ABCDEFL;
        byte[] bytes = EndianKit.LE.fromLong(val);
        assertEquals(val, EndianKit.LE.toLong(bytes));
    }

    @Test
    public void testLEFloat() {
        float val = 3.14f;
        byte[] bytes = EndianKit.LE.fromFloat(val);
        assertEquals(val, EndianKit.LE.toFloat(bytes), 0.0001f);
    }

    @Test
    public void testLEDouble() {
        double val = 3.1415926535;
        byte[] bytes = EndianKit.LE.fromDouble(val);
        assertEquals(val, EndianKit.LE.toDouble(bytes), 0.0000001);
    }

    // ---- Cross-endian tests ----

    @Test
    public void testBEtoLEShort() {
        short val = 0x1234;
        byte[] beBytes = EndianKit.BE.fromShort(val);
        short leVal = EndianKit.LE.toShort(beBytes);
        assertEquals(0x3412, leVal);
    }

    @Test
    public void testLEtoBEInt() {
        int val = 0x12345678;
        byte[] leBytes = EndianKit.LE.fromInt(val);
        int beVal = EndianKit.BE.toInt(leBytes);
        assertEquals(0x78563412, beVal);
    }

    // ---- Byte tests (same for both ends) ----

    @Test
    public void testByteValue() {
        byte val = 0x7F;
        byte[] bytes = EndianKit.BE.fromByteValue(val);
        assertEquals(1, bytes.length);
        assertEquals(val, bytes[0]);
        assertEquals(val, EndianKit.BE.toByteValue(bytes));
    }

    // ---- Unsigned tests ----

    @Test
    public void testUnsignedShort() {
        int val = 0xFFFF;
        byte[] bytes = EndianKit.BE.fromUnsignedShort(val);
        assertEquals(val, EndianKit.BE.toUnsignedShort(bytes));
    }

    @Test
    public void testUnsignedInt() {
        long val = 0xFFFFFFFFL;
        byte[] bytes = EndianKit.BE.fromUnsignedInt(val);
        assertEquals(val, EndianKit.BE.toUnsignedInt(bytes));
    }

    @Test
    public void testUnsignedLong() {
        BigInteger mask = BigInteger.ONE.shiftLeft(Long.SIZE).subtract(BigInteger.ONE);
        BigInteger[] values = {
                BigInteger.ZERO,
                new BigInteger("1234567890ABCDEF", 16),
                BigInteger.ONE.shiftLeft(Long.SIZE - 1),
                mask,
                BigInteger.ONE.shiftLeft(Long.SIZE).add(BigInteger.valueOf(0x1234))
        };

        for (EndianKit endian : EndianKit.values()) {
            for (BigInteger value : values) {
                byte[] bytes = endian.fromUnsignedLong(value);
                byte[] original = bytes.clone();
                assertEquals(Long.BYTES, bytes.length);
                assertEquals(value.and(mask), endian.toUnsignedLong(bytes));
                assertArrayEquals(original, bytes);
            }
        }

        BigInteger value = new BigInteger("0123456789ABCDEF", 16);
        assertArrayEquals(new byte[]{0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD,
                                     (byte) 0xEF}, EndianKit.BE.fromUnsignedLong(value));
        assertArrayEquals(new byte[]{(byte) 0xEF, (byte) 0xCD, (byte) 0xAB, (byte) 0x89, 0x67, 0x45, 0x23,
                                     0x01}, EndianKit.LE.fromUnsignedLong(value));
    }

    @Test
    public void testUnsignedLongIgnoresTrailingBytes() {
        byte[] beBytes = {0x01, 0x23, 0x45, 0x67, (byte) 0x89, (byte) 0xAB, (byte) 0xCD, (byte) 0xEF,
                          0x55};
        byte[] leBytes = {(byte) 0xEF, (byte) 0xCD, (byte) 0xAB, (byte) 0x89, 0x67, 0x45, 0x23, 0x01,
                          0x55};
        BigInteger expected = new BigInteger("0123456789ABCDEF", 16);

        assertEquals(expected, EndianKit.BE.toUnsignedLong(beBytes));
        assertEquals(expected, EndianKit.LE.toUnsignedLong(leBytes));
    }

    // ---- Reverse tests ----

    @Test
    public void testReverseShort() {
        short val = 0x1234;
        short reversed = EndianKit.BE.reverseShort(val);
        assertEquals(EndianKit.LE.toShort(EndianKit.BE.fromShort(val)), reversed);
    }

    @Test
    public void testReverseInt() {
        int val = 0x12345678;
        int reversed = EndianKit.BE.reverseInt(val);
        assertEquals(EndianKit.LE.toInt(EndianKit.BE.fromInt(val)), reversed);
    }

    @Test
    public void testReverseLong() {
        long val = 0x1234567890ABCDEFL;
        long reversed = EndianKit.BE.reverseLong(val);
        assertEquals(EndianKit.LE.toLong(EndianKit.BE.fromLong(val)), reversed);
    }

    // ---- fromNumber tests ----

    @Test
    public void testFromNumberBE() {
        byte[] bytes = EndianKit.BE.fromNumber(0x12345678);
        assertEquals(4, bytes.length);
        assertEquals(0x12345678, EndianKit.BE.toInt(bytes));
    }

    @Test
    public void testFromNumberWithAssignLength_BE() {
        // Pad to 8 bytes from 4-byte int
        byte[] bytes = EndianKit.BE.fromNumber(0x12345678, 8);
        assertEquals(8, bytes.length);
        // Big endian: zeros at front, value at end
        assertEquals(0x12, bytes[4]);
        assertEquals(0x34, bytes[5]);
        assertEquals(0x56, bytes[6]);
        assertEquals(0x78, bytes[7]);
    }

    @Test
    public void testFromNumberWithAssignLength_LE() {
        byte[] bytes = EndianKit.LE.fromNumber(0x12345678, 8);
        assertEquals(8, bytes.length);
        // Little endian: value at front, zeros at end
        assertEquals(0x78, bytes[0]);
        assertEquals(0x56, bytes[1]);
        assertEquals(0x34, bytes[2]);
        assertEquals(0x12, bytes[3]);
    }

    @Test
    public void charsAndUnsignedValuesRoundTripInBothOrders() {
        for (EndianKit endian : EndianKit.values()) {
            assertEquals('\uABCD', endian.toChar(endian.fromChar('\uABCD')));
            assertEquals(0xFF, endian.toUnsignedByte(endian.fromUnsignedByte((short) 0xFF)));
            assertEquals(0xFEDC, endian.toUnsignedShort(endian.fromUnsignedShort(0xFEDC)));
            assertEquals(0xFEDCBA98L, endian.toUnsignedInt(endian.fromUnsignedInt(0xFEDCBA98L)));
        }
    }

    @Test
    public void allReverseOperationsWorkInBothDirections() {
        BigInteger unsignedLong = new BigInteger("FEDCBA9876543210", 16);
        for (EndianKit endian : EndianKit.values()) {
            EndianKit other = endian == EndianKit.BE ? EndianKit.LE : EndianKit.BE;
            assertEquals(other.toUnsignedByte(endian.fromUnsignedByte((short) 0xAB)),
                         endian.reverseUnsignedByte((short) 0xAB));
            assertEquals(other.toShort(endian.fromShort((short) 0x8123)), endian.reverseShort((short) 0x8123));
            assertEquals(other.toUnsignedShort(endian.fromUnsignedShort(0xFEDC)),
                         endian.reverseUnsignedShort(0xFEDC));
            assertEquals(other.toInt(endian.fromInt(0x81234567)), endian.reverseInt(0x81234567));
            assertEquals(other.toUnsignedInt(endian.fromUnsignedInt(0xFEDCBA98L)),
                         endian.reverseUnsignedInt(0xFEDCBA98L));
            assertEquals(other.toLong(endian.fromLong(0x8123456789ABCDEFL)),
                         endian.reverseLong(0x8123456789ABCDEFL));
            assertEquals(other.toUnsignedLong(endian.fromUnsignedLong(unsignedLong)),
                         endian.reverseUnsignedLong(unsignedLong));
            assertEquals(other.toFloat(endian.fromFloat(123.25f)), endian.reverseFloat(123.25f), 0.0f);
            assertEquals(other.toDouble(endian.fromDouble(123.25)), endian.reverseDouble(123.25), 0.0);
        }
    }

    @Test
    public void fromNumberSupportsEveryPrimitiveWrapper() {
        Number[] values = {(byte) 1, (short) 0x0203, 0x04050607, 0x08090A0B0C0D0E0FL, 1.5f, 2.5d};
        int[] lengths = {1, 2, 4, 8, 4, 8};
        for (EndianKit endian : EndianKit.values()) {
            for (int i = 0; i < values.length; i++) {
                assertEquals(lengths[i], endian.fromNumber(values[i]).length);
            }
            assertThrows(UnsupportedOperationException.class, () -> endian.fromNumber(BigInteger.ONE));
        }
    }

    @Test
    public void assignedLengthsCanTruncateAndPad() {
        assertArrayEquals(new byte[]{0x56, 0x78}, EndianKit.BE.fromNumber(0x12345678, 2));
        assertArrayEquals(new byte[]{0, 0x12, 0x34}, EndianKit.BE.fromNumber((short) 0x1234, 3));
        assertArrayEquals(new byte[]{0x78, 0x56}, EndianKit.LE.fromNumber(0x12345678, 2));
        assertArrayEquals(new byte[]{0x34, 0x12, 0}, EndianKit.LE.fromNumber((short) 0x1234, 3));
    }

    @Test
    public void conversionsRejectNullAndShortArrays() {
        for (EndianKit endian : EndianKit.values()) {
            assertThrows(IllegalArgumentException.class, () -> endian.toByteValue(null));
            assertThrows(IllegalArgumentException.class, () -> endian.toShort(new byte[1]));
            assertThrows(IllegalArgumentException.class, () -> endian.toChar(new byte[1]));
            assertThrows(IllegalArgumentException.class, () -> endian.toInt(new byte[3]));
            assertThrows(IllegalArgumentException.class, () -> endian.toLong(new byte[7]));
            assertThrows(IllegalArgumentException.class, () -> endian.toFloat(new byte[3]));
            assertThrows(IllegalArgumentException.class, () -> endian.toDouble(new byte[7]));
        }
    }
}
