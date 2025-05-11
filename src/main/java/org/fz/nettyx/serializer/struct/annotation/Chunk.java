package org.fz.nettyx.serializer.struct.annotation;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.util.exception.Throws;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Type;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * chunk only support for [byte[]/ByteBuf] type field
 * Chunks can be used as placeholders directly, when you don't need to parse certain fields, but have to maintain an offset
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025/5/11 13:05
 */

@Target(FIELD)
@Retention(RUNTIME)
public @interface Chunk {

    /**
     * the number of bytes that need to be occupied
     */
    int size();

    class ChunkHandler implements StructFieldHandler<Chunk> {

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        public Object doRead(
                Type        root,
                Object      earlyStruct,
                StructField field,
                ByteBuf     reading,
                Chunk       chunk)
        {
            Class<?> chunkType = field.wrapped().getType();

            checkChunk(chunkType);

            int chunkSize = chunk.size();

            if (chunkType == byte[].class) {
                byte[] chunkBytes = new byte[chunkSize];
                reading.readBytes(chunkBytes);
                return chunkBytes;
            }
            else
            if (chunkType == ByteBuf.class) return reading.readBytes(chunkSize);

            throw new TypeJudgmentException(field);
        }

        @Override
        public void doWrite(
                Type        root,
                Object      struct,
                StructField field,
                Object      fieldVal,
                ByteBuf     writing,
                Chunk       chunk)
        {
            if (fieldVal instanceof byte[]  bytes)   writing.writeBytes(bytes);
            else
            if (fieldVal instanceof ByteBuf byteBuf) writing.writeBytes(byteBuf);
        }

        static void checkChunk(Class<?> fieldType) {
            Throws.ifTrue(
                    !byte[].class.isAssignableFrom(fieldType) && !ByteBuf.class.isAssignableFrom(fieldType),
                    () -> new TypeJudgmentException("chunk only support byte[] type field, but got [" + fieldType + "]"));
        }
    }

}

