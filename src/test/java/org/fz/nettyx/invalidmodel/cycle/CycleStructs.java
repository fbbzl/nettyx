package org.fz.nettyx.invalidmodel.cycle;

import lombok.Data;
import org.fz.nettyx.serializer.type.annotation.Struct;
import org.fz.nettyx.serializer.type.annotation.ToArray;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;

public final class CycleStructs {

    private CycleStructs() {}

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class Self {
        private Self value;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class MutualA {
        private MutualB b;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class MutualB {
        private MutualA a;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class LevelA {
        private LevelB b;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class LevelB {
        private LevelC c;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class LevelC {
        private LevelA a;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class ArraySelf {
        @ToArray(length = 1)
        private ArraySelf[] children;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class GenericSelf<T> {
        private GenericSelf<T> next;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class GenericOwner {
        private GenericBox<GenericOwner> box;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class GenericBox<T> {
        private T value;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class GenericArrayOwner {
        private GenericArrayBox<GenericArrayOwner> box;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class GenericArrayBox<T> {
        @ToArray(length = 1)
        private T[] values;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class CrossBatchA {
        private CrossBatchB b;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class CrossBatchB {
        private CrossBatchA a;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class MissingDefinitionRoot {
        private MissingDefinitionLeaf leaf;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class MissingDefinitionLeaf {
        private cint value;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class NonArrayAnnotation {
        @ToArray(length = 1)
        private String value;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class AcyclicRoot {
        private AcyclicBranch branch;

        @ToArray(length = 1)
        private AcyclicLeaf[] leaves;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class AcyclicBranch {
        private AcyclicLeaf leaf;
    }

    @Data
    @Struct(endian = Struct.Endian.BE)
    public static class AcyclicLeaf {
        private cint value;
    }
}
