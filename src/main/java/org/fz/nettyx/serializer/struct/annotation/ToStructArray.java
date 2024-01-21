package org.fz.nettyx.serializer.struct.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.StructUtils.getComponentType;

import io.netty.buffer.ByteBuf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.PropertyHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.util.Throws;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/16 16:06
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface ToStructArray {

    /**
     * array length
     *
     * @return the int
     */
    int length();

    @SuppressWarnings("unchecked")
    class ToStructArrayHandler implements PropertyHandler.ReadWriteHandler<ToStructArray> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToStructArray annotation) {
            Class<?> structElementType =
                (structElementType = getComponentType(field)) == Object.class ? serializer.getArrayFieldActualType(
                    field) : structElementType;

            Throws.ifTrue(structElementType == Object.class, new TypeJudgmentException(field));

            int declaredLength = annotation.length();

            return readStructArray(structElementType, declaredLength, serializer.getByteBuf());
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object arrayValue, ToStructArray annotation,
            ByteBuf writing) {
            Class<?> structElementType =
                (structElementType = getComponentType(field)) == Object.class ? serializer.getArrayFieldActualType(
                    field) : structElementType;


            Throws.ifTrue(structElementType == Object.class, new TypeJudgmentException(field));

            int declaredLength = annotation.length();



            writeStructArray(arrayValue, structElementType, declaredLength, writing);
        }



    }
}
