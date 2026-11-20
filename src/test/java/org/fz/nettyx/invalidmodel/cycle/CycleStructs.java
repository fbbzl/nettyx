package org.fz.nettyx.invalidmodel.cycle;

import lombok.Data;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.basic.c.signed.cint;

/**
 * Groups struct dependency graphs for cycle and registration validation.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
public final class CycleStructs {

    private CycleStructs() {}

    /**
     * Creates a direct cycle through a field of its own type.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class Self {
        private Self value;
    }

    /**
     * References {@code MutualB} in a two-type cycle.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class MutualA {
        private MutualB b;
    }

    /**
     * Closes the two-type cycle by referencing {@code MutualA}.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class MutualB {
        private MutualA a;
    }

    /**
     * Starts the three-type cycle with a reference to {@code LevelB}.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class LevelA {
        private LevelB b;
    }

    /**
     * Connects {@code LevelA} to {@code LevelC} in the three-type cycle.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class LevelB {
        private LevelC c;
    }

    /**
     * Closes the three-type cycle with a reference to {@code LevelA}.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class LevelC {
        private LevelA a;
    }

    /**
     * Creates a self-reference through a fixed array element.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class ArraySelf {
        @ToArray(length = 1)
        private ArraySelf[] children;
    }

    /**
     * Creates a direct cycle through the same parameterized struct type.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class GenericSelf<T> {
        private GenericSelf<T> next;
    }

    /**
     * Creates an indirect cycle by passing itself as the value type of a generic box.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class GenericOwner {
        private GenericBox<GenericOwner> box;
    }

    /**
     * Provides the generic value field that resolves back to {@code GenericOwner}.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class GenericBox<T> {
        private T value;
    }

    /**
     * Creates an indirect cycle by passing itself as the element type of a boxed array.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class GenericArrayOwner {
        private GenericArrayBox<GenericArrayOwner> box;
    }

    /**
     * Provides the generic array field whose element type resolves back to {@code GenericArrayOwner}.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class GenericArrayBox<T> {
        @ToArray(length = 1)
        private T[] values;
    }

    /**
     * References a struct registered in a separate scan to exercise cross-scan cycle detection.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class CrossBatchA {
        private CrossBatchB b;
    }

    /**
     * Closes the cycle across scan batches by referencing {@code CrossBatchA}.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class CrossBatchB {
        private CrossBatchA a;
    }

    /**
     * References a struct that is deliberately omitted from the registration batch.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class MissingDefinitionRoot {
        private MissingDefinitionLeaf leaf;
    }

    /**
     * Provides the referenced struct left unregistered in the missing-definition test.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class MissingDefinitionLeaf {
        private cint value;
    }

    /**
     * Applies the array annotation to a scalar string to exercise scan validation.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class NonArrayAnnotation {
        @ToArray(length = 1)
        private String value;
    }

    /**
     * References the same leaf directly through an array and indirectly through a branch.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class AcyclicRoot {
        private AcyclicBranch branch;

        @ToArray(length = 1)
        private AcyclicLeaf[] leaves;
    }

    /**
     * Provides the indirect path from the acyclic root to the shared leaf.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class AcyclicBranch {
        private AcyclicLeaf leaf;
    }

    /**
     * Provides the integer leaf shared by two paths in the acyclic graph.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-07-29
     */
    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class AcyclicLeaf {
        private cint value;
    }
}
