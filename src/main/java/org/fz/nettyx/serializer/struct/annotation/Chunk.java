package org.fz.nettyx.serializer.struct.annotation;

import io.netty.buffer.ByteBuf;
import org.fz.erwin.exception.Throws;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructSerializerContext.StructDefinition.StructField;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * chunk only support for byte[] type field
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
            int length = chunk.length();
            if (reading.readableBytes() < length)
                throw new TooLessBytesException(length, reading.readableBytes());

            byte[] chunkBytes = new byte[length];
            reading.readBytes(chunkBytes);

            return chunkBytes;
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
            if (fieldVal != null) {
                byte[] bytes = (byte[]) fieldVal;
                int padding = computePadding(chunk, bytes.length);
                writing.writeBytes(bytes);
                if (padding > 0) writing.writeZero(padding);
            } else {
                writing.writeZero(chunk.length());
            }
        }

        @Override
        public void doAnnotationValid(Chunk chunk, Field field) {
            Class<?> fieldType = field.getType();
            Throws.ifFalse(byte[].class.isAssignableFrom(fieldType),
                           () -> new TypeJudgmentException("chunk only support byte[] type field, but got [" + fieldType + "]"));
        }

        static int computePadding(Chunk chunk, int valueLength)
        {
            int chunkLength = chunk.length(), padding = chunkLength - valueLength;
            Throws.ifTrue(padding < 0,
                          () -> new IllegalArgumentException("chunk buffer length is: [" + chunkLength + "], but got "
                                                             + "length: [" + valueLength + "]"));
            return padding;
        }
    }

}

