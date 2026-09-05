package org.fz.nettyx.serializer.type;

import cn.hutool.core.lang.TypeReference;
import org.fz.nettyx.codec.model.GenericBox;
import org.fz.nettyx.codec.model.GenericPair;
import org.fz.nettyx.codec.model.GenericTriple;
import org.fz.nettyx.serializer.type.basic.c.signed.cchar;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;
import org.fz.nettyx.serializer.type.basic.c.signed.clong4;
import org.junit.Test;

import java.util.Arrays;

public class GenericStructTest extends AbstractStructSerializerTest {

    @Test
    public void testGenericBox() {
        byte[] bytes = new byte[4];
        Arrays.fill(bytes, (byte) 67);
        assertRoundTrip(new TypeReference<GenericBox<cint>>() {}, bytes);
    }

    @Test
    public void testGenericPair() {
        byte[] bytes = new byte[5];
        Arrays.fill(bytes, (byte) 67);
        assertRoundTrip(new TypeReference<GenericPair<cint, cchar>>() {}, bytes);
    }

    @Test
    public void testGenericTriple() {
        byte[] bytes = new byte[9];
        Arrays.fill(bytes, (byte) 67);
        assertRoundTrip(new TypeReference<GenericTriple<cint, cchar, clong4>>() {}, bytes);
    }

}
