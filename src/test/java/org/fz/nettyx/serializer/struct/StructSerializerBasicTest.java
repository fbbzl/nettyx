package org.fz.nettyx.serializer.struct;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.signed.*;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.culong8;
import org.fz.nettyx.serializer.struct.basic.cpp.cppbool;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class StructSerializerBasicTest {

    @Test
    public void testCcharValue() {
        cchar val = new cchar(0x42);
        assertEquals(Byte.valueOf((byte) 0x42), val.read());
        assertEquals(1, val.getSize());
    }

    @Test
    public void testCcharBytes() {
        cchar val = new cchar(0x42);
        ByteBuf buf = Unpooled.buffer(val.getSize());
        val.write(buf);
        assertEquals(1, buf.writerIndex());
        assertEquals(0x42, buf.getByte(0) & 0xFF);
        buf.release();
    }

    @Test
    public void testCintValue() {
        cint val = new cint(12345);
        assertEquals(Integer.valueOf(12345), val.read());
        assertEquals(4, val.getSize());
    }

    @Test
    public void testCintGetBytesNotEmpty() {
        cint val = new cint(6789);
        ByteBuf buf = Unpooled.buffer(val.getSize());
        val.write(buf);
        assertEquals(4, buf.writerIndex());
        buf.release();
    }

    @Test
    public void testCshortValue() {
        cshort val = new cshort(0x0102);
        assertEquals(Short.valueOf((short) 0x0102), val.read());
        assertEquals(2, val.getSize());
    }

    @Test
    public void testCppboolTrue() {
        cppbool val = new cppbool(true);
        ByteBuf buf = Unpooled.buffer(val.getSize());
        val.write(buf);
        assertEquals(1, buf.writerIndex());
        assertNotEquals(0, buf.getByte(0));
        buf.release();
    }

    @Test
    public void testCppboolFalse() {
        cppbool val = new cppbool(false);
        ByteBuf buf = Unpooled.buffer(val.getSize());
        val.write(buf);
        assertEquals(1, buf.writerIndex());
        assertEquals(0, buf.getByte(0));
        buf.release();
    }

    @Test
    public void testCdoubleValue() {
        cdouble val = new cdouble(3.14159);
        assertEquals(3.14159, (double) val.read(), 0.0001);
        assertEquals(8, val.getSize());
    }

    @Test
    public void testCfloatValue() {
        cfloat val = new cfloat(2.5f);
        assertEquals(2.5f, (float) val.read(), 0.0001f);
        assertEquals(4, val.getSize());
    }

    @Test
    public void testCulong8Value() {
        culong8 val = new culong8(new BigInteger("18446744073709551615"));
        assertEquals(new BigInteger("18446744073709551615"), val.read());
        assertEquals(8, val.getSize());
    }
}
