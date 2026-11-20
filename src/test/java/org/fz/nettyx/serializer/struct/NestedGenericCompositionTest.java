package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.generic.BigEndianGenericValue;
import org.fz.nettyx.beanmodel.generic.GenericStructArrayBean;
import org.fz.nettyx.beanmodel.generic.LittleEndianGenericValue;
import org.fz.nettyx.beanmodel.generic.deep.NestedGenericFixtures.ConcreteHierarchy;
import org.fz.nettyx.beanmodel.generic.deep.NestedGenericFixtures.FixedArray;
import org.fz.nettyx.beanmodel.generic.deep.NestedGenericFixtures.Hierarchy;
import org.fz.nettyx.beanmodel.generic.deep.NestedGenericFixtures.WrappedArray;
import org.fz.nettyx.codec.model.GenericBox;
import org.fz.nettyx.codec.model.GenericPair;
import org.fz.nettyx.codec.model.GenericTriple;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.basic.c.signed.cchar;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.fz.nettyx.serializer.basic.c.signed.clong4;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * Checks binary layout and type resolution for nested generic fields and arrays, including inheritance and concurrent reads.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-11
 */
public class NestedGenericCompositionTest {

    private static final Type PAIR_ARRAY =
            new TypeReference<GenericStructArrayBean<GenericBox<GenericPair<cint, cchar>>>>() {}.getType();

    @BeforeClass
    public static void scanModels() {
        new StructContext("org.fz.nettyx.codec.model", "org.fz.nettyx.beanmodel.generic");
    }

    @Test
    public void nestedBranchesPreserveEachArgumentPositionAndLeafValue() {
        Type type = new TypeReference<GenericBox<GenericPair<GenericBox<cint>,
                GenericTriple<cchar, GenericBox<clong4>, GenericPair<cchar, cint>>>>>() {}.getType();
        byte[] expected = ByteBuffer.allocate(14).order(ByteOrder.nativeOrder())
                .putInt(0x12345678).put((byte) 7).putInt(-123456789).put((byte) -9).putInt(42).array();
        GenericBox<GenericPair<GenericBox<cint>,
                GenericTriple<cchar, GenericBox<clong4>, GenericPair<cchar, cint>>>> bean = decode(type, expected);

        assertEquals(Integer.valueOf(0x12345678), bean.getValue().getFirst().getValue().value());
        var branch = bean.getValue().getSecond();
        assertEquals(Byte.valueOf((byte) 7), branch.getFirst().value());
        assertEquals(Integer.valueOf(-123456789), branch.getSecond().getValue().value());
        assertEquals(Byte.valueOf((byte) -9), branch.getThird().getFirst().value());
        assertEquals(Integer.valueOf(42), branch.getThird().getSecond().value());
        assertArrayEquals(expected, encode(type, bean));
    }

    @Test
    public void repeatedNestedArgumentsDoNotShareDecodedInstances() {
        Type type = new TypeReference<GenericPair<GenericBox<cint>, GenericBox<cint>>>() {}.getType();
        byte[] expected = ints(11, 22);
        GenericPair<GenericBox<cint>, GenericBox<cint>> bean = decode(type, expected);

        assertEquals(Integer.valueOf(11), bean.getFirst().getValue().value());
        assertEquals(Integer.valueOf(22), bean.getSecond().getValue().value());
        assertNotSame(bean.getFirst(), bean.getSecond());
        assertNotSame(bean.getFirst().getValue(), bean.getSecond().getValue());
        bean.getFirst().setValue(new cint(33));
        assertEquals(Integer.valueOf(22), bean.getSecond().getValue().value());
        assertArrayEquals(ints(33, 22), encode(type, bean));
    }

    @Test
    public void innermostStructControlsBigEndianInsideLittleEndianWrappers() {
        Type type = new TypeReference<LittleEndianGenericValue<GenericBox<BigEndianGenericValue<cint>>>>()
                {}.getType();
        byte[] expected = {1, 2, 3, 4};
        LittleEndianGenericValue<GenericBox<BigEndianGenericValue<cint>>> bean = decode(type, expected);

        assertEquals(Integer.valueOf(0x01020304), bean.getValue().getValue().getValue().value());
        assertArrayEquals(expected, encode(type, bean));
    }

    @Test
    public void innermostStructControlsLittleEndianInsideBigEndianWrappers() {
        Type type = new TypeReference<BigEndianGenericValue<GenericBox<LittleEndianGenericValue<cint>>>>()
                {}.getType();
        byte[] expected = {4, 3, 2, 1};
        BigEndianGenericValue<GenericBox<LittleEndianGenericValue<cint>>> bean = decode(type, expected);

        assertEquals(Integer.valueOf(0x01020304), bean.getValue().getValue().getValue().value());
        assertArrayEquals(expected, encode(type, bean));
    }

    @Test
    public void fixedAndFlexibleArraysRetainNestedPairTypesAndValues() {
        byte[] expected = pairBytes(11, 22, 33, 44);
        GenericStructArrayBean<GenericBox<GenericPair<cint, cchar>>> bean = decode(PAIR_ARRAY, expected);

        assertEquals(GenericBox[].class, bean.getFixed().getClass());
        assertEquals(GenericBox[].class, bean.getFlexible().getClass());
        assertEquals(2, bean.getFixed().length);
        assertEquals(2, bean.getFlexible().length);
        assertPair(bean.getFixed()[0], 11);
        assertPair(bean.getFixed()[1], 22);
        assertPair(bean.getFlexible()[0], 33);
        assertPair(bean.getFlexible()[1], 44);
        assertArrayEquals(expected, encode(PAIR_ARRAY, bean));
    }

    @Test
    public void nestedStructArraySupportsAnEmptyFlexibleTail() {
        byte[] expected = pairBytes(12, 23);
        GenericStructArrayBean<GenericBox<GenericPair<cint, cchar>>> bean = decode(PAIR_ARRAY, expected);

        assertEquals(0, bean.getFlexible().length);
        assertPair(bean.getFixed()[0], 12);
        assertPair(bean.getFixed()[1], 23);
        assertArrayEquals(expected, encode(PAIR_ARRAY, bean));
    }

    @Test
    public void nullNestedStructArrayUsesTheResolvedElementWidth() {
        GenericStructArrayBean<GenericBox<GenericPair<cint, cchar>>> bean = new GenericStructArrayBean<>();
        byte[] actual = encode(PAIR_ARRAY, bean);

        assertArrayEquals(new byte[10], actual);
        GenericStructArrayBean<GenericBox<GenericPair<cint, cchar>>> decoded = decode(PAIR_ARRAY, actual);
        assertPair(decoded.getFixed()[0], 0);
        assertPair(decoded.getFixed()[1], 0);
        assertEquals(0, decoded.getFlexible().length);
    }

    @Test
    public void shortFixedNestedArrayPadsTheMissingElement() {
        GenericStructArrayBean<GenericBox<GenericPair<cint, cchar>>> bean = pairArray(17);

        assertArrayEquals(pairBytes(17, 0), encode(PAIR_ARRAY, bean));
    }

    @Test
    public void nullElementInFixedNestedArrayDoesNotChangeFollowingOffsets() {
        GenericStructArrayBean<GenericBox<GenericPair<cint, cchar>>> bean = pairArray(0, 27);
        bean.getFixed()[0] = null;

        assertArrayEquals(pairBytes(0, 27), encode(PAIR_ARRAY, bean));
    }

    @Test
    public void longFixedNestedArrayEncodesOnlyItsDeclaredLength() {
        GenericStructArrayBean<GenericBox<GenericPair<cint, cchar>>> bean = pairArray(17, 27, 37);

        assertArrayEquals(pairBytes(17, 27), encode(PAIR_ARRAY, bean));
    }

    @Test
    public void flexibleNestedArrayRejectsATruncatedFinalStruct() {
        byte[] complete = pairBytes(11, 22, 33);
        ByteBuf input = Unpooled.buffer();
        try {
            input.writeBytes(complete).writeByte(99);
            assertThrows(TooLessBytesException.class, () -> StructSerializer.toStruct(PAIR_ARRAY, input));
            assertEquals(complete.length, input.readerIndex());
        }
        finally {
            input.release();
        }
    }

    @Test
    public void genericWrapperCanContainAnArrayOfOtherGenericWrappers() {
        Type type = new TypeReference<GenericBox<GenericStructArrayBean<GenericBox<cint>>>>() {}.getType();
        byte[] expected = ints(3, 5, 7);
        GenericBox<GenericStructArrayBean<GenericBox<cint>>> bean = decode(type, expected);

        assertEquals(Integer.valueOf(3), bean.getValue().getFixed()[0].getValue().value());
        assertEquals(Integer.valueOf(5), bean.getValue().getFixed()[1].getValue().value());
        assertEquals(1, bean.getValue().getFlexible().length);
        assertEquals(Integer.valueOf(7), bean.getValue().getFlexible()[0].getValue().value());
        assertArrayEquals(expected, encode(type, bean));
    }

    @Test
    public void fixedGenericArraysCanBeNestedInsideFixedGenericArrays() {
        Type type = new TypeReference<FixedArray<FixedArray<GenericBox<cint>>>>() {}.getType();
        byte[] expected = ints(11, 22, 33, 44);
        FixedArray<FixedArray<GenericBox<cint>>> bean = decode(type, expected);

        assertEquals(FixedArray[].class, bean.getValues().getClass());
        assertEquals(2, bean.getValues().length);
        for (int row = 0; row < 2; row++) {
            assertEquals(GenericBox[].class, bean.getValues()[row].getValues().getClass());
            assertEquals(2, bean.getValues()[row].getValues().length);
            for (int column = 0; column < 2; column++) {
                assertEquals(Integer.valueOf(11 * (row * 2 + column + 1)),
                             bean.getValues()[row].getValues()[column].getValue().value());
            }
        }
        assertArrayEquals(expected, encode(type, bean));
    }

    @Test
    public void concreteInheritanceBindsNestedAndReorderedArguments() {
        byte[] expected = ByteBuffer.allocate(9).order(ByteOrder.nativeOrder())
                .putInt(0x12345678).put((byte) -7).putInt(42).array();
        ConcreteHierarchy bean = decode(ConcreteHierarchy.class, expected);

        assertEquals(Integer.valueOf(0x12345678), bean.getFirst().getFirst().getValue().value());
        assertEquals(Byte.valueOf((byte) -7), bean.getFirst().getSecond().value());
        assertEquals(Integer.valueOf(42), bean.getSecond().getValue().value());
        assertArrayEquals(expected, encode(ConcreteHierarchy.class, bean));
    }

    @Test
    public void parameterizedInheritanceSubstitutesAlreadyParameterizedArguments() {
        Type type = new TypeReference<Hierarchy<GenericBox<cchar>, GenericBox<cint>>>() {}.getType();
        byte[] expected = ByteBuffer.allocate(9).order(ByteOrder.nativeOrder())
                .putInt(13).put((byte) 17).putInt(19).array();
        Hierarchy<GenericBox<cchar>, GenericBox<cint>> bean = decode(type, expected);

        assertEquals(Integer.valueOf(13), bean.getFirst().getFirst().getValue().getValue().value());
        assertEquals(Byte.valueOf((byte) 17), bean.getFirst().getSecond().getValue().value());
        assertEquals(Integer.valueOf(19), bean.getSecond().getValue().getValue().value());
        assertArrayEquals(expected, encode(type, bean));
    }

    @Test
    public void inheritedGenericArrayResolvesItsNestedElementType() {
        Type type = new TypeReference<WrappedArray<GenericPair<cint, cchar>>>() {}.getType();
        byte[] expected = pairBytes(21, 31);
        WrappedArray<GenericPair<cint, cchar>> bean = decode(type, expected);

        assertEquals(GenericBox[].class, bean.getValues().getClass());
        assertPair(bean.getValues()[0], 21);
        assertPair(bean.getValues()[1], 31);
        assertArrayEquals(expected, encode(type, bean));
    }

    @Test
    public void nestedRootsWithSwappedTypeArgumentsStayIsolatedWhenInterleaved() {
        Type first = new TypeReference<GenericBox<GenericPair<cint, cchar>>>() {}.getType();
        Type second = new TypeReference<GenericBox<GenericPair<cchar, cint>>>() {}.getType();
        for (int repeat = 0; repeat < 12; repeat++) {
            assertSwappedRoots(first, second, repeat + 1);
        }
    }

    @Test(timeout = 15_000)
    public void nestedRootsWithSwappedTypeArgumentsStayIsolatedAcrossThreads() throws Exception {
        Type first = new TypeReference<GenericBox<GenericPair<cint, cchar>>>() {}.getType();
        Type second = new TypeReference<GenericBox<GenericPair<cchar, cint>>>() {}.getType();
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Future<?>> tasks = new ArrayList<>();
            for (int worker = 0; worker < 4; worker++) {
                int seed = worker * 20;
                tasks.add(executor.submit(() -> {
                    for (int repeat = 0; repeat < 20; repeat++) {
                        assertSwappedRoots(first, second, seed + repeat + 1);
                    }
                }));
            }
            for (Future<?> task : tasks) task.get(10, TimeUnit.SECONDS);
        }
        finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(2, TimeUnit.SECONDS));
        }
    }

    @Test
    public void deeplyNestedUnsupportedLeafTypesAreRejected() {
        Type stringLeaf = new TypeReference<GenericBox<GenericBox<GenericBox<String>>>>() {}.getType();
        Type arrayLeaf = new TypeReference<GenericBox<GenericBox<GenericBox<cint[]>>>>() {}.getType();
        Type wildcardLeaf = new TypeReference<GenericBox<GenericBox<GenericBox<? extends cint>>>>()
                {}.getType();
        Type rawLeaf = new TypeReference<GenericBox<GenericBox<GenericBox>>>() {}.getType();
        for (Type type : new Type[]{stringLeaf, arrayLeaf, wildcardLeaf, rawLeaf}) {
            assertThrows(type.getTypeName(), TypeJudgmentException.class, () -> decode(type, ints(42)));
        }
    }

    private static void assertSwappedRoots(Type first, Type second, int value) {
        byte[] intFirst = ByteBuffer.allocate(5).order(ByteOrder.nativeOrder())
                .putInt(0x12340000 + value).put((byte) value).array();
        byte[] charFirst = ByteBuffer.allocate(5).order(ByteOrder.nativeOrder())
                .put((byte) -value).putInt(0x56780000 + value).array();
        GenericBox<GenericPair<cint, cchar>> firstBean = decode(first, intFirst);
        GenericBox<GenericPair<cchar, cint>> secondBean = decode(second, charFirst);
        assertEquals(cint.class, firstBean.getValue().getFirst().getClass());
        assertEquals(Integer.valueOf(0x12340000 + value), firstBean.getValue().getFirst().value());
        assertEquals(Byte.valueOf((byte) value), firstBean.getValue().getSecond().value());
        assertEquals(cchar.class, secondBean.getValue().getFirst().getClass());
        assertEquals(Byte.valueOf((byte) -value), secondBean.getValue().getFirst().value());
        assertEquals(Integer.valueOf(0x56780000 + value), secondBean.getValue().getSecond().value());
        assertArrayEquals(intFirst, encode(first, firstBean));
        assertArrayEquals(charFirst, encode(second, secondBean));
    }

    @SuppressWarnings("unchecked")
    private static GenericStructArrayBean<GenericBox<GenericPair<cint, cchar>>> pairArray(int... values) {
        GenericBox<GenericPair<cint, cchar>>[] fixed = new GenericBox[values.length];
        for (int i = 0; i < values.length; i++) {
            GenericPair<cint, cchar> pair = new GenericPair<>();
            pair.setFirst(new cint(values[i]));
            pair.setSecond(new cchar(values[i]));
            fixed[i] = new GenericBox<>();
            fixed[i].setValue(pair);
        }
        GenericStructArrayBean<GenericBox<GenericPair<cint, cchar>>> bean = new GenericStructArrayBean<>();
        bean.setFixed(fixed);
        return bean;
    }

    private static void assertPair(GenericBox<GenericPair<cint, cchar>> box, int expected) {
        assertNotNull(box);
        assertEquals(cint.class, box.getValue().getFirst().getClass());
        assertEquals(Integer.valueOf(expected), box.getValue().getFirst().value());
        assertEquals(cchar.class, box.getValue().getSecond().getClass());
        assertEquals(Byte.valueOf((byte) expected), box.getValue().getSecond().value());
    }

    private static byte[] pairBytes(int... values) {
        ByteBuffer bytes = ByteBuffer.allocate(values.length * 5).order(ByteOrder.nativeOrder());
        for (int value : values) bytes.putInt(value).put((byte) value);
        return bytes.array();
    }

    private static byte[] ints(int... values) {
        ByteBuffer bytes = ByteBuffer.allocate(values.length * 4).order(ByteOrder.nativeOrder());
        for (int value : values) bytes.putInt(value);
        return bytes.array();
    }

    private static <T> T decode(Type type, byte[] bytes) {
        ByteBuf input = Unpooled.wrappedBuffer(bytes);
        try {
            T bean = StructSerializer.toStruct(type, input);
            assertEquals(0, input.readableBytes());
            return bean;
        }
        finally {
            input.release();
        }
    }

    private static byte[] encode(Type type, Object bean) {
        ByteBuf output = Unpooled.buffer();
        try {
            StructSerializer.toByteBuf(type, bean, output);
            byte[] bytes = new byte[output.readableBytes()];
            output.readBytes(bytes);
            return bytes;
        }
        finally {
            output.release();
        }
    }
}