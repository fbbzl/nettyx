package org.fz.nettyx.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author fengbinbin
 * @since 2024
 */
public class BinsTest {

    @Test
    public void testFromByte() {
        Bins bins = Bins.fromByte(5);
        // 5 in 8-bit binary: 00000101
        assertEquals(8, bins.length());
        assertEquals(1, bins.get(0));
        assertEquals(0, bins.get(1));
        assertEquals(1, bins.get(2));
        assertEquals(0, bins.get(3));
    }

    @Test
    public void testFromByteZero() {
        Bins bins = Bins.fromByte(0);
        assertEquals(8, bins.length());
        assertEquals("00000000", bins.toString());
    }

    @Test
    public void testFromByteMax() {
        Bins bins = Bins.fromByte(255);
        assertEquals(8, bins.length());
        assertEquals("11111111", bins.toString());
    }

    @Test
    public void testFromShort() {
        Bins bins = Bins.fromShort(3);
        // 3 in 16-bit binary
        assertEquals(16, bins.length());
    }

    @Test
    public void testFromInt() {
        Bins bins = Bins.fromInt(10);
        assertEquals(32, bins.length());
    }

    @Test
    public void testFromLong() {
        Bins bins = Bins.fromLong(1L);
        assertEquals(64, bins.length());
    }

    @Test
    public void testToString() {
        Bins bins = Bins.fromByte(5);
        assertEquals("00000101", bins.toString());
    }

    @Test
    public void testGet() {
        Bins bins = Bins.fromByte(0x0F); // 00001111
        assertEquals(1, bins.get(3));
        assertEquals(1, bins.get(2));
        assertEquals(1, bins.get(1));
        assertEquals(1, bins.get(0));
    }

    @Test
    public void testGetByte() {
        Bins bins = Bins.fromInt(0x12345678);
        // Extract bits by position
        int bits24_31 = bins.getByte(24, 8);
        assertEquals(0x12, bits24_31 & 0xFF);
    }

    @Test
    public void testGetShort() {
        Bins bins = Bins.fromInt(0x12345678);
        int bits16_31 = bins.getShort(16, 16);
        assertEquals(0x1234, bits16_31 & 0xFFFF);
    }

    @Test
    public void testSet1() {
        Bins bins = Bins.fromByte(0);
        bins.set1(0); // Set LSB
        assertEquals(1, bins.get(0));
    }

    @Test
    public void testSet0() {
        Bins bins = Bins.fromByte(0xFF);
        bins.set0(0); // Clear LSB
        assertEquals(0, bins.get(0));
    }

    @Test
    public void testSet1Range() {
        Bins bins = Bins.fromByte(0);
        bins.set1(0, 4); // Set lower 4 bits
        assertEquals(1, bins.get(3));
        assertEquals(1, bins.get(2));
        assertEquals(1, bins.get(1));
        assertEquals(1, bins.get(0));
    }

    @Test
    public void testLength() {
        assertEquals(8, Bins.fromByte(0).length());
        assertEquals(16, Bins.fromShort(0).length());
        assertEquals(32, Bins.fromInt(0).length());
        assertEquals(64, Bins.fromLong(0).length());
    }
}
