package org.fz.nettyx.serializer.type.annotation;

import io.netty.buffer.ByteBuf;
import org.fz.erwin.exception.Throws;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.type.StructFieldHandler;
import org.fz.nettyx.serializer.type.StructSerializer;
import org.fz.nettyx.serializer.type.StructSerializerContext.StructDefinition.StructField;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import static cn.hutool.core.text.CharSequenceUtil.removeAllSuffix;
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
        public boolean isSingleton()
        {
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
                ToCharSequence   toCharSequence)
        {
            String charset = toCharSequence.charset();
            if (!Charset.isSupported(charset))
                throw new UnsupportedCharsetException("do not support charset [" + charset + "]");

            int bufferLength = toCharSequence.bufferLength();
            if (reading.readableBytes() < bufferLength)
                throw new TooLessBytesException(bufferLength, reading.readableBytes());

            byte[] bytes = new byte[bufferLength];
            reading.readBytes(bytes);
            String value = new String(bytes, Charset.forName(charset));
            return removeAllSuffix(value, "\0");
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
                ToCharSequence   toCharSequence)
        {
            int    bufferLength = toCharSequence.bufferLength();
            String charset      = toCharSequence.charset();

            if (fieldVal != null) {
                byte[] encoded  = fieldVal.toString().getBytes(Charset.forName(charset));
                int    writeLen = Math.min(encoded.length, bufferLength);
                writing.writeBytes(encoded, 0, writeLen);
                int pad = bufferLength - writeLen;
                if (pad > 0) writing.writeZero(pad);
            } else writing.writeZero(bufferLength);
        }

        @Override
        public void doValid(ToCharSequence annotation, Field field)
        {
            Class<?> fieldType = field.getType();
            Throws.ifFalse(CharSequence.class.isAssignableFrom(fieldType) && fieldType.isAssignableFrom(String.class),
                           () -> new TypeJudgmentException("@ToCharSequence field must accept String values, but got [" + fieldType + "]"));
            Throws.ifTrue(annotation.bufferLength() < 0,
                          () -> new IllegalArgumentException("char sequence buffer length must not be negative, but got [" + annotation.bufferLength() + "]"));
            if (!Charset.isSupported(annotation.charset())) {
                throw new UnsupportedCharsetException("do not support charset [" + annotation.charset() + "]");
            }
        }
    }
}
