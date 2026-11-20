package org.fz.nettyx.serializer.struct;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.codec.model.GenericBox;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.basic.Basic;
import org.fz.nettyx.serializer.basic.c.signed.cchar;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.fz.nettyx.serializer.basic.c.signed.clong4;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Checks binary encoding and decoding through deeply nested structs, including null values and truncated input.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-11
 */
@RunWith(Parameterized.class)
public class DeepGenericStructTest {

    @Parameterized.Parameters(name = "depth={0}, leaf={1}")
    public static Iterable<Object[]> cases() {
        List<Object[]> cases = new ArrayList<>();
        for (int depth : new int[]{1, 2, 4, 8, 16}) {
            for (Class<?> leaf : new Class<?>[]{cchar.class, cint.class, clong4.class}) {
                cases.add(new Object[]{depth, leaf});
            }
        }
        return cases;
    }

    @BeforeClass
    public static void scanModels() {
        new StructContext("org.fz.nettyx.codec.model");
    }

    private final int depth;
    private final Class<?> leafClass;
    private final Type root;

    public DeepGenericStructTest(int depth, Class<?> leafClass) {
        this.depth = depth;
        this.leafClass = leafClass;
        TypeToken<?> type = TypeToken.of(leafClass);
        for (int level = 0; level < depth; level++) type = TypeToken.of(boxType(type));
        root = type.getType();
    }

    @Test
    public void decodesEveryNestedLayerAndPreservesTrailingBytes() {
        byte[] expected = expectedBytes();
        ByteBuf input = Unpooled.buffer();
        ByteBuf output = Unpooled.buffer();
        try {
            input.writeByte(0x55).writeBytes(expected).writeByte(0x66);
            input.readerIndex(1);
            Object decoded = StructSerializer.toStruct(root, input);
            assertLeaf(decoded, leafValue().value());
            assertEquals(1 + expected.length, input.readerIndex());
            assertEquals(0x66, input.readUnsignedByte());

            StructSerializer.toByteBuf(root, decoded, output);
            assertEquals(expected.length, output.readableBytes());
            byte[] actual = new byte[output.readableBytes()];
            output.readBytes(actual);
            assertArrayEquals(expected, actual);
        }
        finally {
            input.release();
            output.release();
        }
    }

    @Test
    public void encodesConstructedNestedObjectsWithoutLosingTheWriterOffset() {
        ByteBuf output = Unpooled.buffer();
        try {
            output.writeByte(0x55);
            StructSerializer.toByteBuf(root, wrap(leafValue(), depth), output);
            byte[] expected = expectedBytes();
            assertEquals(1 + expected.length, output.writerIndex());
            assertEquals(0x55, output.readUnsignedByte());
            byte[] actual = new byte[expected.length];
            output.readBytes(actual);
            assertArrayEquals(expected, actual);
        }
        finally {
            output.release();
        }
    }

    @Test
    public void nullAtAnIntermediateLevelKeepsTheResolvedLeafWidth() {
        ByteBuf output = Unpooled.buffer();
        try {
            StructSerializer.toByteBuf(root, wrap(null, Math.max(1, depth / 2)), output);
            assertEquals(expectedBytes().length, output.readableBytes());
            byte[] zeros = new byte[output.readableBytes()];
            output.getBytes(output.readerIndex(), zeros);
            assertArrayEquals(new byte[expectedBytes().length], zeros);

            Object decoded = StructSerializer.toStruct(root, output);
            Object zero = leafClass == cchar.class ? (Object) Byte.valueOf((byte) 0) : Integer.valueOf(0);
            assertLeaf(decoded, zero);
            assertEquals(0, output.readableBytes());
        }
        finally {
            output.release();
        }
    }

    @Test
    public void truncatedLeafFailsWithoutConsumingItsPartialBytes() {
        byte[] bytes = expectedBytes();
        ByteBuf input = Unpooled.wrappedBuffer(Arrays.copyOf(bytes, bytes.length - 1));
        try {
            assertThrows(TooLessBytesException.class, () -> StructSerializer.toStruct(root, input));
            assertEquals(0, input.readerIndex());
        }
        finally {
            input.release();
        }
    }

    private void assertLeaf(Object value, Object expected) {
        for (int level = 0; level < depth; level++) {
            assertNotNull("missing box at level " + level, value);
            assertEquals("wrong class at level " + level, GenericBox.class, value.getClass());
            value = ((GenericBox<?>) value).getValue();
        }
        assertNotNull(value);
        assertEquals(leafClass, value.getClass());
        assertEquals(expected, ((Basic<?>) value).value());
    }

    private Basic<?> leafValue() {
        if (leafClass == cchar.class) return new cchar(-91);
        if (leafClass == cint.class) return new cint(0x12345678);
        return new clong4(-123456789);
    }

    private byte[] expectedBytes() {
        if (leafClass == cchar.class) return new byte[]{(byte) -91};
        int value = leafClass == cint.class ? 0x12345678 : -123456789;
        return ByteBuffer.allocate(4).order(ByteOrder.nativeOrder()).putInt(value).array();
    }

    private static Object wrap(Object leaf, int depth) {
        Object value = leaf;
        for (int level = 0; level < depth; level++) {
            GenericBox<Object> box = new GenericBox<>();
            box.setValue(value);
            value = box;
        }
        return value;
    }

    private static <T> Type boxType(TypeToken<T> valueType) {
        return new TypeToken<GenericBox<T>>() {}.where(new TypeParameter<T>() {}, valueType).getType();
    }
}