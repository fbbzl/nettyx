package org.fz.nettyx.serializer.struct.basic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;
import org.fz.nettyx.serializer.struct.basic.c.signed.cshort;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuint;
import org.junit.Test;

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
            assertThrows(TooLessBytesException.class,
                         () -> new cint(Unpooled.wrappedBuffer(new byte[3]), ByteOrder.BIG_ENDIAN));
        }
        finally {
            bigEndian.release();
            littleEndian.release();
        }
    }
}
