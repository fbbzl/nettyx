package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.TypeReference;
import org.fz.nettyx.beanmodel.generic.GenericStructArrayBean;
import org.fz.nettyx.beanmodel.generic.deep.NestedGenericFixtures.Base;
import org.fz.nettyx.beanmodel.generic.deep.NestedGenericFixtures.ConcreteHierarchy;
import org.fz.nettyx.beanmodel.generic.deep.NestedGenericFixtures.FixedArray;
import org.fz.nettyx.beanmodel.generic.deep.NestedGenericFixtures.Hierarchy;
import org.fz.nettyx.beanmodel.generic.deep.NestedGenericFixtures.Middle;
import org.fz.nettyx.beanmodel.generic.deep.NestedGenericFixtures.WrappedArray;
import org.fz.nettyx.codec.model.GenericBox;
import org.fz.nettyx.codec.model.GenericPair;
import org.fz.nettyx.codec.model.GenericTriple;
import org.fz.nettyx.serializer.basic.c.signed.cchar;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.fz.nettyx.serializer.basic.c.signed.clong4;
import org.fz.nettyx.serializer.struct.annotation.ToArray.ToArrayHandler;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Checks that repeated resolution returns the complete type of a deeply nested generic field.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-11
 */
@RunWith(Parameterized.class)
public class DeepGenericTypeResolutionTest {

    @Parameterized.Parameters(name = "{0}")
    public static List<Object[]> cases() throws Exception {
        return List.of(
                fieldCase("four nested boxes", new TypeReference<
                        GenericBox<GenericBox<GenericBox<GenericBox<cint>>>>>() {},
                          GenericBox.class, "value", new TypeReference<GenericBox<GenericBox<GenericBox<cint>>>>() {}),
                fieldCase("pair first nested branch", new TypeReference<
                        GenericPair<GenericBox<GenericPair<cint, cchar>>, GenericTriple<cchar, cint, clong4>>>() {},
                          GenericPair.class, "first", new TypeReference<GenericBox<GenericPair<cint, cchar>>>() {}),
                fieldCase("pair second nested branch", new TypeReference<
                        GenericPair<GenericBox<GenericPair<cint, cchar>>, GenericTriple<cchar, cint, clong4>>>() {},
                          GenericPair.class, "second", new TypeReference<GenericTriple<cchar, cint, clong4>>() {}),
                fieldCase("pair first swapped", new TypeReference<
                        GenericPair<GenericBox<cchar>, GenericBox<cint>>>() {},
                          GenericPair.class, "first", new TypeReference<GenericBox<cchar>>() {}),
                fieldCase("pair second swapped", new TypeReference<
                        GenericPair<GenericBox<cchar>, GenericBox<cint>>>() {},
                          GenericPair.class, "second", new TypeReference<GenericBox<cint>>() {}),
                fieldCase("triple first", new TypeReference<
                        GenericTriple<GenericBox<cchar>, GenericPair<cint, cchar>, GenericBox<GenericBox<clong4>>>>()
                        {}, GenericTriple.class, "first", new TypeReference<GenericBox<cchar>>() {}),
                fieldCase("triple second", new TypeReference<
                        GenericTriple<GenericBox<cchar>, GenericPair<cint, cchar>, GenericBox<GenericBox<clong4>>>>()
                        {}, GenericTriple.class, "second", new TypeReference<GenericPair<cint, cchar>>() {}),
                fieldCase("triple third", new TypeReference<
                        GenericTriple<GenericBox<cchar>, GenericPair<cint, cchar>, GenericBox<GenericBox<clong4>>>>()
                        {}, GenericTriple.class, "third", new TypeReference<GenericBox<GenericBox<clong4>>>() {}),
                fieldCase("fixed generic array", new TypeReference<
                        GenericStructArrayBean<GenericBox<GenericPair<cint, cchar>>>>() {},
                          GenericStructArrayBean.class, "fixed",
                          new TypeReference<GenericBox<GenericPair<cint, cchar>>[]>() {}),
                fieldCase("flexible generic array", new TypeReference<
                        GenericStructArrayBean<GenericBox<GenericPair<cint, cchar>>>>() {},
                          GenericStructArrayBean.class, "flexible",
                          new TypeReference<GenericBox<GenericPair<cint, cchar>>[]>() {}),
                fieldCase("inherited nested first", new TypeReference<
                        Hierarchy<GenericBox<cchar>, GenericBox<cint>>>() {}, Base.class, "first",
                          new TypeReference<GenericPair<GenericBox<GenericBox<cint>>, GenericBox<cchar>>>() {}),
                fieldCase("inherited nested second", new TypeReference<
                        Hierarchy<GenericBox<cchar>, GenericBox<cint>>>() {}, Base.class, "second",
                          new TypeReference<GenericBox<GenericBox<cint>>>() {}),
                fieldCase("concrete inherited first", ConcreteHierarchy.class, Base.class, "first",
                          new TypeReference<GenericPair<GenericBox<cint>, cchar>>() {}),
                fieldCase("concrete inherited second", ConcreteHierarchy.class, Base.class, "second",
                          new TypeReference<GenericBox<cint>>() {}),
                fieldCase("inherited generic array", new TypeReference<WrappedArray<GenericPair<cint, cchar>>>() {},
                          FixedArray.class, "values", new TypeReference<GenericBox<GenericPair<cint, cchar>>[]>() {}),
                fieldCase("array of generic arrays", new TypeReference<FixedArray<FixedArray<GenericBox<cint>>>>()
                        {}, FixedArray.class, "values", new TypeReference<FixedArray<GenericBox<cint>>[]>() {}),
                fieldCase("nested upper wildcard captured", new TypeReference<GenericBox<
                        GenericPair<? extends GenericBox<cint>, cchar>>>() {}, GenericBox.class, "value",
                          new TypeReference<GenericPair<? extends GenericBox<cint>, cchar>>() {}),
                fieldCase("nested lower wildcard preserved", new TypeReference<GenericBox<
                        GenericPair<cint, ? super GenericBox<cchar>>>>() {}, GenericBox.class, "value",
                          new TypeReference<GenericPair<cint, ? super GenericBox<cchar>>>() {}),
                fieldCase("upper capture and lower wildcard bounds", new TypeReference<GenericBox<
                        GenericPair<? extends GenericBox<cint>, ? super GenericBox<cchar>>>>() {},
                          GenericBox.class, "value", new TypeReference<
                        GenericPair<? extends GenericBox<cint>, ? super GenericBox<cchar>>>() {}),
                fieldCase("array as nested argument preserved", new TypeReference<
                        GenericBox<GenericPair<cint[], cchar>>>() {}, GenericBox.class, "value",
                          new TypeReference<GenericPair<cint[], cchar>>() {}),
                fieldCase("array argument inside generic array", new TypeReference<
                        FixedArray<GenericBox<cint[]>>>() {}, FixedArray.class, "values",
                          new TypeReference<GenericBox<cint[]>[]>() {}),
                fieldCase("repeated inherited argument", new TypeReference<
                        Middle<GenericBox<cint>, GenericBox<cchar>>>() {}, Base.class, "first",
                          new TypeReference<GenericPair<GenericBox<cint>, GenericBox<cchar>>>() {}),
                fieldCase("type variables with same name stay scoped", new TypeReference<
                        Middle<GenericBox<cint>, GenericBox<cchar>>>() {}, Base.class, "second",
                          new TypeReference<GenericBox<cint>>() {}),
                fieldCase("inherited branch as type argument", new TypeReference<
                        Hierarchy<FixedArray<cchar>, GenericPair<cint, cchar>>>() {}, Base.class, "second",
                          new TypeReference<GenericBox<GenericPair<cint, cchar>>>() {})
        );
    }

    private final Type root;
    private final Type declared;
    private final Type expected;

    public DeepGenericTypeResolutionTest(String name, Type root, Type declared, Type expected) {
        this.root = root;
        this.declared = declared;
        this.expected = expected;
    }

    @Test
    public void resolvesTheFullNestedTypeAndKeepsItStableOnRepeatedLookup() {
        assertResolvedType(StructContext.resolveType(root, declared));
        assertResolvedType(StructContext.resolveType(root, declared));
        if (expected instanceof GenericArrayType array) {
            assertEquals(array.getGenericComponentType(), ToArrayHandler.getComponentType(root, declared));
        }
    }

    private void assertResolvedType(Type actual) {
        if (expected instanceof ParameterizedType pair
            && pair.getActualTypeArguments()[0] instanceof WildcardType wildcard
            && wildcard.getLowerBounds().length == 0) {
            // Upper wildcards become capture variables; their generated names carry no type semantics.
            assertTrue(actual instanceof ParameterizedType);
            ParameterizedType resolved = (ParameterizedType) actual;
            assertEquals(pair.getRawType(), resolved.getRawType());
            assertEquals(pair.getOwnerType(), resolved.getOwnerType());
            Type[] arguments = resolved.getActualTypeArguments();
            assertEquals(pair.getActualTypeArguments().length, arguments.length);
            assertTrue(arguments[0] instanceof TypeVariable<?>);
            assertArrayEquals(wildcard.getUpperBounds(), ((TypeVariable<?>) arguments[0]).getBounds());
            assertEquals(pair.getActualTypeArguments()[1], arguments[1]);
        }
        else {
            assertEquals(expected, actual);
        }
    }

    private static Object[] fieldCase(String name, Type root, Class<?> owner, String field, Type expected)
            throws NoSuchFieldException {
        return new Object[]{name, unwrap(root), owner.getDeclaredField(field).getGenericType(), unwrap(expected)};
    }

    private static Type unwrap(Type type) {
        return type instanceof TypeReference<?> reference ? reference.getType() : type;
    }
}