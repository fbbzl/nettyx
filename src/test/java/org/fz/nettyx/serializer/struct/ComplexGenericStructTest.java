package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.codec.model.*;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong4;
import org.junit.Test;

import java.util.Arrays;

import static org.fz.nettyx.serializer.struct.StructSerializer.toByteBuf;
import static org.fz.nettyx.serializer.struct.StructSerializer.toStruct;
import static org.junit.Assert.assertNotNull;

public class ComplexGenericStructTest extends AbstractStructSerializerTest {

    @Test
    public void testNestedGeneric() {
        byte[] bytes = new byte[512];
        Arrays.fill(bytes, (byte) 67);
        assertRoundTrip(new TypeReference<NestedGeneric<cint, cchar>>() {}, bytes);
    }

    @Test
    public void testNestedArrayGeneric() {
        byte[] bytes = new byte[512];
        Arrays.fill(bytes, (byte) 67);
        assertRoundTrip(new TypeReference<NestedArrayGeneric<cint, cchar>>() {}, bytes);
    }

    @Test
    public void testDeepNestedParameterized() {
        byte[] bytes = new byte[256];
        Arrays.fill(bytes, (byte) 67);
        assertRoundTrip(new TypeReference<GenericBox<GenericPair<GenericTriple<cint, cchar, clong4>, cchar>>>() {}, bytes);
    }

    @Test
    public void testDerivedGeneric() {
        byte[] bytes = new byte[64];
        Arrays.fill(bytes, (byte) 67);
        assertRoundTrip(new TypeReference<DerivedGeneric<cint, cchar>>() {}, bytes);
    }

    @Test
    public void testStructBoundedGeneric() {
        byte[] bytes = new byte[256];
        Arrays.fill(bytes, (byte) 67);
        assertRoundTrip(new TypeReference<StructBoundedGeneric<Bill>>() {}, bytes);
    }

    @Test
    public void testInterfaceImplGeneric() {
        byte[] bytes = new byte[64];
        Arrays.fill(bytes, (byte) 67);
        assertRoundTrip(new TypeReference<InterfaceImplGeneric<cint>>() {}, bytes);
    }

    @Test
    public void testReusedGenericWithDifferentTypeArgs() {
        byte[] bytes = new byte[128];
        Arrays.fill(bytes, (byte) 67);

        ReusedGeneric<cint> intVersion = toStruct(new TypeReference<ReusedGeneric<cint>>() {}, Unpooled.wrappedBuffer(bytes));
        assertNotNull(intVersion);
        ByteBuf intBytesBuf = Unpooled.buffer();
        toByteBuf(new TypeReference<ReusedGeneric<cint>>() {}, intVersion, intBytesBuf);
        byte[] intBytes = new byte[intBytesBuf.readableBytes()];
        intBytesBuf.readBytes(intBytes);

        ReusedGeneric<cchar> charVersion = toStruct(new TypeReference<ReusedGeneric<cchar>>() {}, Unpooled.wrappedBuffer(bytes));
        assertNotNull(charVersion);
        ByteBuf charBytesBuf = Unpooled.buffer();
        toByteBuf(new TypeReference<ReusedGeneric<cchar>>() {}, charVersion, charBytesBuf);
        byte[] charBytes = new byte[charBytesBuf.readableBytes()];
        charBytesBuf.readBytes(charBytes);

        assertNotNull(intBytes);
        assertNotNull(charBytes);
    }

}
