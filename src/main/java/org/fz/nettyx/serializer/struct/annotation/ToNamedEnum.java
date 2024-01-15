package org.fz.nettyx.serializer.struct.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import cn.hutool.core.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import org.fz.nettyx.serializer.struct.PropertyHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;

/**
 * find enum by enum name
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /1/10 16:43
 */
@Target(FIELD)
@Retention(RUNTIME)
@SuppressWarnings("all")
public @interface ToNamedEnum {

    /**
     * Enum type class.
     *
     * @return the class
     */
    Class<? extends Enum> enumType();

    /**
     * Charset string.
     *
     * @return the string
     */
    String charset() default "US-ASCII";

    /**
     * Buffer length int.
     *
     * @return the int
     */
    int bufferLength();

    /**
     * The type To named enum handler.
     */
    class ToNamedEnumHandler implements PropertyHandler.ReadWriteHandler<ToNamedEnum> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToNamedEnum toNamedEnum) {
            Class<Enum> enumClass = (Class<Enum>) toNamedEnum.enumType();
            String enumName = serializer.getByteBuf()
                .readCharSequence(toNamedEnum.bufferLength(), Charset.forName(toNamedEnum.charset())).toString();

            return EnumUtil.fromString(enumClass, enumName);
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object value, ToNamedEnum toNamedEnum,
            ByteBuf writing) {
            int bufferLength = toNamedEnum.bufferLength();
            String charset = toNamedEnum.charset();

            if (value != null) writing.writeBytes(value.toString().getBytes(Charset.forName(charset)));
            else               writing.writeBytes(new byte[bufferLength]);
        }
    }

}
