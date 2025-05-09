package org.fz.nettyx.serializer.struct.annotation;

import cn.hutool.core.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.StructFieldHandler;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * find enum by enum name
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2024 /1/10 16:43
 */
@SuppressWarnings("all")
@Target(FIELD)
@Retention(RUNTIME)
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
    class ToNamedEnumHandler implements StructFieldHandler<ToNamedEnum> {
        @Override
        public boolean isSingleton()
        {
            return true;
        }

        @Override
        public Object doRead(
                Type        root,
                Object      earlyStruct,
                StructField field,
                ByteBuf     reading,
                ToNamedEnum toNamedEnum)
        {
            Class<Enum> enumClass = (Class<Enum>) toNamedEnum.enumType();
            String enumName = reading
                    .readCharSequence(toNamedEnum.bufferLength(),
                                      Charset.forName(toNamedEnum.charset())).toString();

            return EnumUtil.fromString(enumClass, enumName);
        }

        @Override
        public void doWrite(
                Type        root,
                Object      struct,
                StructField field,
                Object      fieldVal,
                ByteBuf     writing,
                ToNamedEnum toNamedEnum)
        {
            int    bufferLength = toNamedEnum.bufferLength();
            String charset      = toNamedEnum.charset();

            if (fieldVal != null) writing.writeBytes(fieldVal.toString().getBytes(Charset.forName(charset)));
            else writing.writeBytes(new byte[bufferLength]);
        }
    }

}
