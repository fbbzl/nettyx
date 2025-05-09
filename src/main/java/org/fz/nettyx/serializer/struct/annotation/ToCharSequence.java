package org.fz.nettyx.serializer.struct.annotation;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.StructFieldHandler;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The interface Char sequence.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/27 10:40
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface ToCharSequence {

    /**
     * Charset string.
     *
     * @return the legal charset
     * @see StandardCharsets
     */
    String charset() default "UTF-8";

    /**
     * Buffer length int.
     *
     * @return the buffer occupied by this char sequence
     */
    int bufferLength();

    class ToStringHandler implements StructFieldHandler<ToCharSequence> {
        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        public Object doRead(
                Type           root,
                Object         earlyStruct,
                StructField    field,
                ByteBuf        reading,
                ToCharSequence toCharSequence)
        {
            String charset = toCharSequence.charset();
            if (!Charset.isSupported(charset))
                throw new UnsupportedCharsetException("do not support charset [" + charset + "]");

            if (!reading.isReadable()) {
                throw new IllegalArgumentException(
                        "buffer is not readable please check [" + ByteBufUtil.hexDump(reading) + "], field is [" + field
                        + "]");
            }
            byte[] bytes = new byte[toCharSequence.bufferLength()];
            reading.readBytes(bytes);
            return new String(bytes, Charset.forName(charset));
        }

        @Override
        public void doWrite(
                Type           root,
                Object         struct,
                StructField    field,
                Object         fieldVal,
                ByteBuf        writing,
                ToCharSequence toCharSequence)
        {
            int    bufferLength = toCharSequence.bufferLength();
            String charset      = toCharSequence.charset();

            if (fieldVal != null) writing.writeBytes(fieldVal.toString().getBytes(Charset.forName(charset)));
            else writing.writeBytes(new byte[bufferLength]);
        }
    }
}
