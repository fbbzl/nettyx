package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.TypeReference;
import org.fz.nettyx.codec.model.GenericBox;
import org.fz.nettyx.codec.model.GenericPair;
import org.fz.nettyx.codec.model.User;
import org.fz.nettyx.codec.model.Wife;
import org.fz.nettyx.codec.model.Son;
import org.fz.nettyx.codec.model.Bill;
import org.fz.nettyx.codec.model.GirlFriend;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.fz.nettyx.serializer.basic.c.signed.clong4;
import org.fz.nettyx.serializer.basic.c.unsigned.culong8;
import org.fz.nettyx.serializer.struct.annotation.ToArray.ToArrayHandler;
import org.junit.Test;

import java.lang.reflect.Type;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;

/**
 * Checks cache reuse, isolation between root types, and behavior when type resolution fails.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-11
 */
public class GenericTypeResolutionCacheTest {

    @Test
    public void reusesEquivalentResolutionAndSeparatesRoots() throws Exception {
        Type firstRoot = new TypeReference<GenericBox<GenericPair<String, cint>>>() {}.getType();
        Type secondRoot = new TypeReference<GenericBox<GenericPair<cint, String>>>() {}.getType();
        Type fieldType = GenericBox.class.getDeclaredField("value").getGenericType();

        Type first = StructContext.resolveType(firstRoot, fieldType);
        Type repeated = StructContext.resolveType(firstRoot, fieldType);
        Type second = StructContext.resolveType(secondRoot, fieldType);

        assertSame(first, repeated);
        assertNotSame(first, second);
        assertEquals(new TypeReference<GenericPair<String, cint>>() {}.getType(), first);
        assertEquals(new TypeReference<GenericPair<cint, String>>() {}.getType(), second);
    }

    @Test
    public void preservesUnboundTypeVariables() throws Exception {
        Type fieldType = GenericBox.class.getDeclaredField("value").getGenericType();

        assertSame(fieldType, StructContext.resolveType(GenericBox.class, fieldType));
    }

    @Test
    public void reusesNestedArrayComponentResolution() throws Exception {
        Type root = new TypeReference<User<clong4, Wife<culong8, Son<clong4, Bill>>, GirlFriend>>() {}.getType();
        Type fieldType = User.class.getDeclaredField("bs2d").getGenericType();
        Type componentType = ToArrayHandler.getComponentType(root, fieldType);

        assertSame(componentType, ToArrayHandler.getComponentType(root, fieldType));
    }

    @Test
    public void doesNotCacheResolutionFailures() {
        Type fieldType = GenericBox.class.getTypeParameters()[0];

        NullPointerException first = assertThrows(NullPointerException.class,
                () -> StructContext.resolveType(null, fieldType));
        NullPointerException repeated = assertThrows(NullPointerException.class,
                () -> StructContext.resolveType(null, fieldType));

        assertEquals(first.getMessage(), repeated.getMessage());
    }
}
