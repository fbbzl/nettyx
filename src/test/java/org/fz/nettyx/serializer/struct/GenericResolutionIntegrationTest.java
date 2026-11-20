package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.codec.model.Bill;
import org.fz.nettyx.codec.model.GirlFriend;
import org.fz.nettyx.codec.model.Son;
import org.fz.nettyx.codec.model.User;
import org.fz.nettyx.codec.model.Wife;
import org.fz.nettyx.serializer.basic.c.signed.cchar;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.fz.nettyx.serializer.basic.c.signed.clong4;
import org.fz.nettyx.serializer.basic.c.unsigned.culong8;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Verifies how the serializer resolves inherited generic fields, unbound variables, and nested arrays.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-11
 */
public class GenericResolutionIntegrationTest {

    private static final Type USER_TYPE =
            new TypeReference<User<clong4, Wife<culong8, Son<clong4, Bill>>, GirlFriend>>() {}.getType();

    @BeforeClass
    public static void scanModels() {
        new StructContext("org.fz.nettyx.codec.model", "org.fz.nettyx.serializer.struct");
    }

    @Test
    public void memberInterfaceOwnerDoesNotChangeInheritedFieldType() {
        byte[] bytes = {11, 22, 33, 44, 55};
        InheritedInt bean = deserialize(InheritedInt.class, bytes);

        assertEquals(Integer.valueOf(0x0b16212c), bean.getValue().value());
        assertEquals(Byte.valueOf((byte) 55), bean.getTail().value());
        assertArrayEquals(bytes, serialize(InheritedInt.class, bean));
    }

    @Test
    public void inheritedNullFieldKeepsItsFourByteWidth() {
        InheritedInt bean = new InheritedInt();
        bean.setTail(new cchar(55));

        assertArrayEquals(new byte[]{0, 0, 0, 0, 55}, serialize(InheritedInt.class, bean));
    }

    @Test
    public void fieldTypeResolvesThroughMultipleGenericSuperclasses() {
        byte[] bytes = {11, 22, 33, 44, 55};
        InheritedViaMiddle bean = deserialize(InheritedViaMiddle.class, bytes);

        assertEquals(Integer.valueOf(0x0b16212c), bean.getValue().value());
        assertArrayEquals(bytes, serialize(InheritedViaMiddle.class, bean));
    }

    @Test(timeout = 1_000)
    public void unboundVariablesAreNotClassifiedByTheirBounds() {
        Type variable = InheritedFields.class.getTypeParameters()[0];
        StructSerializer serializer = new StructSerializer(InheritedFields.class);

        assertFalse(serializer.isBasic(variable));
        assertFalse(serializer.isStruct(variable));
    }

    @Test(timeout = 30_000)
    public void nestedGenericArraysRoundTripAcrossThreads() throws Exception {
        int length = serialize(USER_TYPE, new User<>()).length;
        ExecutorService executor = Executors.newFixedThreadPool(4);
        try {
            List<Future<?>> tasks = new ArrayList<>();
            for (int worker = 0; worker < 4; worker++) {
                int seed = worker;
                tasks.add(executor.submit(() -> verifyUserRoundTrips(length, seed)));
            }
            for (Future<?> task : tasks) task.get(20, TimeUnit.SECONDS);
        }
        finally {
            executor.shutdownNow();
        }
    }

    private static void verifyUserRoundTrips(int length, int seed) {
        Random random = new Random(seed);
        for (int repeat = 0; repeat < 40; repeat++) {
            byte[] expected = new byte[length];
            // Keep embedded UTF-8 strings valid and floating-point fields finite.
            for (int i = 0; i < length; i++) expected[i] = (byte) (33 + random.nextInt(94));
            Object decoded = deserialize(USER_TYPE, expected);
            assertArrayEquals(expected, serialize(USER_TYPE, decoded));
        }
    }

    private static <T> T deserialize(Type root, byte[] bytes) {
        ByteBuf input = Unpooled.wrappedBuffer(bytes);
        try {
            T result = StructSerializer.toStruct(root, input);
            assertEquals(0, input.readableBytes());
            return result;
        }
        finally {
            input.release();
        }
    }

    private static byte[] serialize(Type root, Object bean) {
        ByteBuf output = Unpooled.buffer();
        try {
            StructSerializer.toByteBuf(root, bean, output);
            byte[] bytes = new byte[output.readableBytes()];
            output.readBytes(bytes);
            return bytes;
        }
        finally {
            output.release();
        }
    }

    /**
     * Declares a generic value and a trailing byte for checking field widths after type resolution.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    public static class InheritedFields<T> {
        private T value;
        private cchar tail;

        public T getValue() { return value; }
        public void setValue(T value) { this.value = value; }
        public cchar getTail() { return tail; }
        public void setTail(cchar tail) { this.tail = tail; }
    }

    /**
     * Binds the inherited value to a byte and declares a member interface for the owner resolution test.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    public static class UnrelatedOwner extends InheritedFields<cchar> {
        /**
         * Provides a generic interface whose owner has a different binding for the inherited field.
         *
         * @author fengbinbin
         * @version 1.0
         * @since 2026-11-11
         */
        public interface Tag<T> {}
    }

    /**
     * Passes a type parameter to the generic field owner through an intermediate superclass.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    public static class MiddleFields<T> extends InheritedFields<T> {}

    /**
     * Binds the inherited value to a four-byte integer through an intermediate superclass.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    @Struct(endian = Struct.Endian.BE)
    public static class InheritedViaMiddle extends MiddleFields<cint> {}

    /**
     * Binds the inherited value to a four-byte integer while implementing an unrelated member interface.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    @Struct(endian = Struct.Endian.BE)
    public static class InheritedInt extends InheritedFields<cint> implements UnrelatedOwner.Tag<String> {}
}
