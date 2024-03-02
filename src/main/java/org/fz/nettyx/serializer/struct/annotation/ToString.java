package org.fz.nettyx.serializer.struct.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;

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
public @interface ToString {

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

    class ToStringHandler implements StructFieldHandler.ReadWriteHandler<ToString> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToString toString) {
            String charset = toString.charset();
            if (!Charset.isSupported(charset)) throw new UnsupportedCharsetException("do not support charset [" + charset + "]");

            ByteBuf byteBuf = serializer.getByteBuf();
            if (!byteBuf.isReadable()) {
                throw new IllegalArgumentException(
                    "buffer is not readable please check [" + ByteBufUtil.hexDump(byteBuf) + "], field is [" + field
                        + "]");
            }
            byte[] bytes;
            byteBuf.readBytes(bytes = new byte[toString.bufferLength()]);
            return new String(bytes, Charset.forName(charset));
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object value, ToString toString, ByteBuf writing) {
            int bufferLength = toString.bufferLength();
            String charset = toString.charset();

            if (value != null) writing.writeBytes(value.toString().getBytes(Charset.forName(charset)));
            else               writing.writeBytes(new byte[bufferLength]);

        }

    }
}
