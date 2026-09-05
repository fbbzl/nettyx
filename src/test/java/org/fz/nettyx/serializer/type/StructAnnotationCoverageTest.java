package org.fz.nettyx.serializer.type;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.beanmodel.valid.BasicArrayBean;
import org.fz.nettyx.beanmodel.valid.FlexibleGenericBasicArrayBean;
import org.fz.nettyx.exception.StructFieldHandlerException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.type.StructSerializerContext.StructDefinition.StructField;
import org.fz.nettyx.serializer.type.annotation.Chunk;
import org.fz.nettyx.serializer.type.annotation.ToArray;
import org.fz.nettyx.serializer.type.annotation.ToCharSequence;
import org.fz.nettyx.serializer.type.basic.c.signed.cint;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.nio.charset.UnsupportedCharsetException;

import static org.fz.nettyx.serializer.type.StructSerializerContext.getStructDefinition;
import static org.junit.Assert.*;

public class StructAnnotationCoverageTest {

    @BeforeClass
    public static void scanModels() {
        new StructSerializerContext("org.fz.nettyx.beanmodel.valid");
    }

    @Test
    public void toArrayHandlerReadsWritesAndResolvesArrayTypes() throws Exception {
        StructField field = field(BasicArrayBean.class, "values");
        ToArray annotation = field.annotation();
        ToArray.ToArrayHandler handler = new ToArray.ToArrayHandler();
        StructSerializer serializer = new StructSerializer(BasicArrayBean.class);
        assertTrue(handler.isSingleton());

        ByteBuf input = Unpooled.wrappedBuffer(new byte[]{0, 0, 0, 1, 0, 0, 0, 2});
        ByteBuf output = Unpooled.buffer();
        try {
            cint[] values = (cint[]) handler.doRead(serializer, BasicArrayBean.class, null, field,
                                                    cint[].class, input, annotation);
            assertEquals(2, values.length);
            handler.doWrite(serializer, BasicArrayBean.class, null, field, cint[].class,
                            values, output, annotation);
            assertArrayEquals(new byte[]{0, 0, 0, 1, 0, 0, 0, 2}, bytes(output));

            assertThrows(TypeJudgmentException.class,
                         () -> handler.doRead(serializer, BasicArrayBean.class, null, field,
                                              Object[].class, Unpooled.EMPTY_BUFFER, annotation));
            assertThrows(TypeJudgmentException.class,
                         () -> handler.doWrite(serializer, BasicArrayBean.class, null, field,
                                               Object[].class, new Object[0], output, annotation));
        }
        finally {
            input.release();
            output.release();
        }

        assertEquals(cint.class, ToArray.ToArrayHandler.getComponentType(BasicArrayBean.class, cint[].class));
        assertNull(ToArray.ToArrayHandler.getComponentType(BasicArrayBean.class, String.class));
        Field genericField = FlexibleGenericBasicArrayBean.class.getDeclaredField("values");
        GenericArrayType genericArray = (GenericArrayType) genericField.getGenericType();
        Type root = new TypeReference<FlexibleGenericBasicArrayBean<cint>>() {}.getType();
        assertEquals(cint.class, ToArray.ToArrayHandler.getComponentType(root, genericArray));
        Type unknown = unknownType();
        assertSame(unknown, ToArray.ToArrayHandler.getComponentType(root, unknown));
    }

    @Test
    public void toArrayValidationRejectsMissingLengthAndNonTrailingFlexibleField() throws Exception {
        ToArray.ToArrayHandler handler = new ToArray.ToArrayHandler();
        Field missingLength = InvalidArrayFields.class.getDeclaredField("missingLength");
        Field nonTrailing = InvalidArrayFields.class.getDeclaredField("nonTrailing");
        Field trailing = InvalidArrayFields.class.getDeclaredField("trailing");
        Field fixed = ValidArrayFields.class.getDeclaredField("fixed");

        assertThrows(StructFieldHandlerException.class,
                     () -> handler.doValid(missingLength.getAnnotation(ToArray.class), missingLength));
        assertThrows(StructFieldHandlerException.class,
                     () -> handler.doValid(nonTrailing.getAnnotation(ToArray.class), nonTrailing));
        handler.doValid(trailing.getAnnotation(ToArray.class), trailing);
        handler.doValid(fixed.getAnnotation(ToArray.class), fixed);
    }

    @Test
    public void stringHandlerCoversCharsetsLengthsPaddingAndValidation() throws Exception {
        ToCharSequence.ToStringHandler handler = new ToCharSequence.ToStringHandler();
        Field valid = StringFields.class.getDeclaredField("valid");
        ToCharSequence annotation = valid.getAnnotation(ToCharSequence.class);
        assertTrue(handler.isSingleton());

        ByteBuf shortInput = Unpooled.wrappedBuffer(new byte[]{1});
        ByteBuf validInput = Unpooled.wrappedBuffer(new byte[]{'A', 0, 0, 0});
        ByteBuf output = Unpooled.buffer();
        try {
            assertThrows(TooLessBytesException.class,
                         () -> handler.doRead(null, null, null, null, String.class, shortInput, annotation));
            assertEquals("A", handler.doRead(null, null, null, null, String.class, validInput, annotation));
            handler.doWrite(null, null, null, null, String.class, "A", output, annotation);
            handler.doWrite(null, null, null, null, String.class, "ABCDE", output, annotation);
            handler.doWrite(null, null, null, null, String.class, null, output, annotation);
            assertArrayEquals(new byte[]{'A', 0, 0, 0, 'A', 'B', 'C', 'D', 0, 0, 0, 0}, bytes(output));
        }
        finally {
            shortInput.release();
            validInput.release();
            output.release();
        }

        Field badCharset = StringFields.class.getDeclaredField("badCharset");
        ToCharSequence badCharsetAnnotation = badCharset.getAnnotation(ToCharSequence.class);
        assertThrows(UnsupportedCharsetException.class,
                     () -> handler.doRead(null, null, null, null, String.class,
                                          Unpooled.EMPTY_BUFFER, badCharsetAnnotation));
        assertThrows(UnsupportedCharsetException.class,
                     () -> handler.doValid(badCharsetAnnotation, badCharset));

        Field wrongType = StringFields.class.getDeclaredField("wrongType");
        assertThrows(TypeJudgmentException.class,
                     () -> handler.doValid(wrongType.getAnnotation(ToCharSequence.class), wrongType));
        Field negative = StringFields.class.getDeclaredField("negative");
        assertThrows(IllegalArgumentException.class,
                     () -> handler.doValid(negative.getAnnotation(ToCharSequence.class), negative));
        handler.doValid(annotation, valid);
    }

    @Test
    public void chunkHandlerCoversReadWritePaddingOverflowAndValidation() throws Exception {
        Chunk.ChunkHandler handler = new Chunk.ChunkHandler();
        Field valid = ChunkFields.class.getDeclaredField("valid");
        Chunk annotation = valid.getAnnotation(Chunk.class);
        assertTrue(handler.isSingleton());

        ByteBuf shortInput = Unpooled.wrappedBuffer(new byte[]{1});
        ByteBuf validInput = Unpooled.wrappedBuffer(new byte[]{1, 2, 3, 4});
        ByteBuf output = Unpooled.buffer();
        try {
            assertThrows(TooLessBytesException.class,
                         () -> handler.doRead(null, null, null, null, byte[].class, shortInput, annotation));
            assertArrayEquals(new byte[]{1, 2, 3, 4},
                              (byte[]) handler.doRead(null, null, null, null, byte[].class, validInput, annotation));
            handler.doWrite(null, null, null, null, byte[].class, new byte[]{1, 2}, output, annotation);
            handler.doWrite(null, null, null, null, byte[].class, new byte[]{3, 4, 5, 6}, output, annotation);
            handler.doWrite(null, null, null, null, byte[].class, null, output, annotation);
            assertArrayEquals(new byte[]{1, 2, 0, 0, 3, 4, 5, 6, 0, 0, 0, 0}, bytes(output));
            assertThrows(IllegalArgumentException.class,
                         () -> handler.doWrite(null, null, null, null, byte[].class,
                                               new byte[5], output, annotation));
        }
        finally {
            shortInput.release();
            validInput.release();
            output.release();
        }

        Field wrongType = ChunkFields.class.getDeclaredField("wrongType");
        assertThrows(TypeJudgmentException.class,
                     () -> handler.doValid(wrongType.getAnnotation(Chunk.class), wrongType));
        Field negative = ChunkFields.class.getDeclaredField("negative");
        assertThrows(IllegalArgumentException.class,
                     () -> handler.doValid(negative.getAnnotation(Chunk.class), negative));
        handler.doValid(annotation, valid);
    }

    private static StructField field(Class<?> type, String name) {
        for (StructField field : getStructDefinition(type).fields()) {
            if (field.wrapped().getName().equals(name)) return field;
        }
        throw new AssertionError("field not found: " + name);
    }

    private static byte[] bytes(ByteBuf buffer) {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
    }

    private static Type unknownType() {
        return new Type() {};
    }

    private static class InvalidArrayFields {
        @ToArray
        Object[] missingLength;
        @ToArray(flexible = true)
        Object[] nonTrailing;
        int tail;
        @ToArray(flexible = true)
        Object[] trailing;
    }

    private static class ValidArrayFields {
        @ToArray(length = 0)
        Object[] fixed;
    }

    private static class StringFields {
        @ToCharSequence(bufferLength = 4, charset = "US-ASCII")
        String valid;
        @ToCharSequence(bufferLength = 1, charset = "definitely-not-a-charset")
        String badCharset;
        @ToCharSequence(bufferLength = 1)
        int wrongType;
        @ToCharSequence(bufferLength = -1)
        String negative;
    }

    private static class ChunkFields {
        @Chunk(length = 4)
        byte[] valid;
        @Chunk(length = 1)
        String wrongType;
        @Chunk(length = -1)
        byte[] negative;
    }
}
