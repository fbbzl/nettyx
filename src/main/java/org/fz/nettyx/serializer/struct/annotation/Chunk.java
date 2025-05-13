package org.fz.nettyx.serializer.struct.annotation;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
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
    int length();

    class ChunkHandler implements StructFieldHandler<Chunk> {

        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        public Object doRead(
                StructSerializer serializer,
                Type             root,
                Object           earlyStruct,
                StructField      field,
                Type             fieldType,
                ByteBuf          reading,
                Chunk            chunk)
        {
            Class<?> chunkType = field.wrapped().getType();
            checkChunk(chunkType);

            int chunkLength = chunk.length();

            if (chunkType == byte[].class) {
                byte[] chunkBytes = new byte[chunkLength];
                reading.readBytes(chunkBytes);
                return chunkBytes;
            }
            else
            if (chunkType == ByteBuf.class) return reading.readBytes(chunkLength);

            throw new TypeJudgmentException(field);
        }

        @Override
        public void doWrite(
                StructSerializer serializer,
                Type             root,
                Object           struct,
                StructField      field,
                Type             fieldType,
                Object           fieldVal,
                ByteBuf          writing,
                Chunk            chunk)
        {
            Class<?> chunkType = field.wrapped().getType();
            checkChunk(chunkType);
            if (fieldVal instanceof byte[] bytes) {
                int padding = computePadding(chunk, bytes.length);
                writing.writeBytes(bytes);
                if (padding > 0) writing.writeBytes(new byte[padding]);
            }
            else
            if (fieldVal instanceof ByteBuf byteBuf) {
                int padding = computePadding(chunk, byteBuf.readableBytes());
                writing.writeBytes(byteBuf);
                if (padding > 0) writing.writeBytes(new byte[padding]);
            }
        }

        static void checkChunk(Class<?> fieldType) {
            Throws.ifTrue(!byte[].class.isAssignableFrom(fieldType) && !ByteBuf.class.isAssignableFrom(fieldType),
                          () -> new TypeJudgmentException("chunk only support byte[] type field, but got [" + fieldType + "]"));
        }

        static int computePadding(Chunk chunk, int valueLength) {
            int chunkLength = chunk.length(), padding = chunkLength - valueLength;
            Throws.ifTrue(padding < 0,
                          () -> new IllegalArgumentException("chunk buffer length is: [" + chunkLength + "], but got "
                                                             + "length: [" + valueLength + "]"));
            return padding;
        }
    }

}

