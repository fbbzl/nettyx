package org.fz.nettyx.util;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

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
}
