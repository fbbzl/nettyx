package org.fz.nettyx.serializer.struct.annotation;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.StructPropHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.util.Throws;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.Type;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


/**
 * array field must use this to assign array length!!!
 *
 * @author fengbinbin
 * @since 2021 -10-20 08:18
 */
@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface ToArray {

    /**
     * array length
     *
     * @return the int
     */
    int length();

    class ToArrayHandler implements StructPropHandler.ReadWriteHandler<ToArray> {
        @Override
        public boolean isSingleton() {
            return true;
        }

        @Override
        public Object doRead(StructSerializer serializer, Type fieldType, Field field, ToArray annotation) {
            Type componentType = serializer.getComponentType(fieldType);

            Throws.ifTrue(componentType == Object.class, new TypeJudgmentException(field));

            int length = annotation.length();

            try {
                return serializer.readArray(componentType, length);
            } catch (TypeJudgmentException typeJudgmentException) {
                throw new UnsupportedOperationException("can not determine the type of field [" + field + "]");
            }
        }

        @Override
        public void doWrite(StructSerializer serializer, Type fieldType, Field field, Object arrayValue, ToArray annotation, ByteBuf writing) {
            Type componentType = serializer.getComponentType(fieldType);

            Throws.ifTrue(componentType == Object.class, new TypeJudgmentException(field));

            int length = annotation.length();

            try {
                serializer.writeArray(arrayValue, componentType, length, writing);
            } catch (TypeJudgmentException typeJudgmentException) {
                throw new UnsupportedOperationException("can not determine the type of field [" + field + "]");
            }
        }

        //**************************************         private end           ***************************************//

    }

}
