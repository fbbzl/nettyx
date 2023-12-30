package org.fz.nettyx.serializer.typed.annotation.collection;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.fz.nettyx.serializer.typed.ByteBufHandler;
import org.fz.nettyx.serializer.typed.TypedSerializer;
import org.fz.nettyx.serializer.typed.annotation.FieldHandler;
import org.fz.nettyx.util.StructUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
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
@Target(FIELD)
@Retention(RUNTIME)
@FieldHandler(ToCharSequence.CharSequenceHandler.class)
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
    int bufferLength() default 0;

    class CharSequenceHandler implements ByteBufHandler.ReadWriteHandler<TypedSerializer> {

        @Override
        public Object doRead(TypedSerializer serializer, Field field) {
            StructUtils.checkAssignable(field, CharSequence.class);

            ToCharSequence toCharSequence = field.getAnnotation(ToCharSequence.class);

            String charset = toCharSequence.charset();
            if (!Charset.isSupported(charset)) throw new UnsupportedCharsetException("do not support charset [" + charset + "]");

            ByteBuf byteBuf = serializer.getByteBuf();
            if (!byteBuf.isReadable())
                throw new IllegalArgumentException("buffer is not readable please check [" + ByteBufUtil.hexDump(byteBuf) + "]");

            return byteBuf.readBytes(toCharSequence.bufferLength()).toString(Charset.forName(charset));
        }

        @Override
        public void doWrite(TypedSerializer serializer, Field field, Object value, ByteBuf writingBuffer) {
            StructUtils.checkAssignable(field, CharSequence.class);
            ToCharSequence toCharSequence = field.getAnnotation(ToCharSequence.class);

            int bufferLength = toCharSequence.bufferLength();
            String charset = toCharSequence.charset();

            if (value != null) writingBuffer.writeBytes(value.toString().getBytes(Charset.forName(charset)));
            else               writingBuffer.writeBytes(new byte[bufferLength]);

        }

    }
}
