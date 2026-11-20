package org.fz.nettyx.beanmodel.generic.deep;

import lombok.Getter;
import lombok.Setter;
import org.fz.nettyx.codec.model.GenericBox;
import org.fz.nettyx.codec.model.GenericPair;
import org.fz.nettyx.serializer.basic.c.signed.cchar;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;

/**
 * Groups fixtures for resolving nested generics and generic arrays through inheritance.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-11
 */
public final class NestedGenericFixtures {

    private NestedGenericFixtures() {}

    /**
     * Declares the two generic fields inherited by the nested hierarchy fixtures.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    @Getter
    @Setter
    public static class Base<A, B> {
        private A first;
        private B second;
    }

    /**
     * Nests both type parameters in the first inherited field and reuses the first parameter in the second field.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    public static class Middle<T, U> extends Base<GenericPair<T, U>, T> {}

    /**
     * Wraps the second type parameter in a {@code GenericBox} and reverses the parameter order passed to {@code Middle}.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    @Struct(endian = Struct.Endian.BE)
    public static class Hierarchy<X, Y> extends Middle<GenericBox<Y>, X> {}

    /**
     * Binds the deepest hierarchy fixture to concrete {@code cchar} and {@code cint} types.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    @Struct(endian = Struct.Endian.BE)
    public static class ConcreteHierarchy extends Hierarchy<cchar, cint> {}

    /**
     * Declares a fixed array of two generic elements for inherited array tests.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    @Getter
    @Setter
    @Struct(endian = Struct.Endian.BE)
    public static class FixedArray<T> {
        @ToArray(length = 2)
        private T[] values;
    }

    /**
     * Wraps each element of the inherited fixed array in a {@code GenericBox}.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    @Struct(endian = Struct.Endian.BE)
    public static class WrappedArray<T> extends FixedArray<GenericBox<T>> {}
}