package org.fz.nettyx.serializer.struct;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.codec.model.EmptyStruct;
import org.fz.nettyx.codec.model.ProtectedConstructorStruct;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.serializer.struct.annotation.Chunk;
import org.fz.nettyx.serializer.struct.annotation.ToCharSequence;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class StructSerializerRobustnessTest {

    @BeforeClass
    public static void init()
    {
        new StructSerializerContext("org.fz.nettyx.codec.model");
    }

    @Test
    public void serializeFailureDoesNotReleaseCallerBuffer()
    {
        ByteBuf buffer = Unpooled.buffer();
        try {
            assertThrows(RuntimeException.class,
                         () -> new StructSerializer(String.class).doSerialize("unsupported", buffer));
            assertEquals(1, buffer.refCnt());
        }
        finally {
            buffer.release();
        }
    }

    @Test
    public void fixedLengthStringDropsZeroPadding() throws Exception
    {
        Field field = ValidFields.class.getDeclaredField("text");
        ToCharSequence annotation = field.getAnnotation(ToCharSequence.class);
        ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{'a', 'b', 'c', 0, 0});

        Object value = new ToCharSequence.ToStringHandler()
                .doRead(null, null, null, null, field.getGenericType(), buffer, annotation);

        assertEquals("abc", value);
    }

    @Test
    public void fixedLengthStringPreservesEmbeddedNullCharacters() throws Exception
    {
        Field field = ValidFields.class.getDeclaredField("text");
        ToCharSequence annotation = field.getAnnotation(ToCharSequence.class);
        ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{'a', 0, 'b', 0, 0});

        Object value = new ToCharSequence.ToStringHandler()
                .doRead(null, null, null, null, field.getGenericType(), buffer, annotation);

        assertEquals("a\0b", value);
    }

    @Test
    public void flexibleStructArrayRejectsElementsThatConsumeNoBytes()
    {
        ByteBuf buffer = Unpooled.wrappedBuffer(new byte[]{1});
        StructSerializer serializer = new StructSerializer(EmptyStruct.class);

        assertThrows(SerializeException.class,
                     () -> serializer.readStructArray(EmptyStruct.class, buffer, 0, true));
    }

    @Test
    public void negativeAnnotationLengthsAreRejected() throws Exception
    {
        Field chunkField = InvalidFields.class.getDeclaredField("chunk");
        Field textField = InvalidFields.class.getDeclaredField("text");

        assertThrows(IllegalArgumentException.class,
                     () -> new Chunk.ChunkHandler().doValid(chunkField.getAnnotation(Chunk.class), chunkField));
        assertThrows(IllegalArgumentException.class,
                     () -> new ToCharSequence.ToStringHandler()
                             .doValid(textField.getAnnotation(ToCharSequence.class), textField));
    }

    @Test
    public void fixedArrayByteLengthOverflowIsRejected()
    {
        ByteBuf buffer = Unpooled.buffer();
        try {
            StructSerializer serializer = new StructSerializer(EmptyStruct.class);
            assertThrows(SerializeException.class,
                         () -> serializer.writeBasicArray(null, 8, Integer.MAX_VALUE, buffer, false, null));
            assertEquals(0, buffer.writerIndex());
        }
        finally {
            buffer.release();
        }
    }

    @Test
    public void generatedReaderCanCallProtectedNoArgConstructor()
    {
        ProtectedConstructorStruct value = StructSerializer.toStruct(
                ProtectedConstructorStruct.class, Unpooled.EMPTY_BUFFER);
        assertEquals(ProtectedConstructorStruct.class, value.getClass());
    }


    private static class ValidFields {
        @ToCharSequence(bufferLength = 5)
        String text;
    }

    private static class InvalidFields {
        @Chunk(length = -1)
        byte[] chunk;

        @ToCharSequence(bufferLength = -1)
        String text;
    }
}
