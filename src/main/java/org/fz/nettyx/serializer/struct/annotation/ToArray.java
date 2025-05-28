package org.fz.nettyx.serializer.struct.annotation;

import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import org.fz.erwin.exception.Throws;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.GenericArrayType;
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

    class ToArrayHandler implements StructFieldHandler<ToArray> {
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
                ToArray          toArray)
        {
            Type componentType = getComponentType(root, fieldType);

            Throws.ifTrue(componentType == Object.class, () -> new TypeJudgmentException(field));

            int length = toArray.length();

            return serializer.readArray(componentType, reading, length);
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
                ToArray          toArray)
        {
            Type componentType = getComponentType(root, fieldType);

            Throws.ifTrue(componentType == Object.class, () -> new TypeJudgmentException(field));

            int length = toArray.length();

            serializer.writeArray(fieldVal, componentType, length, writing);
        }

        static Type getComponentType(
                Type root,
                Type type)
        {
            if (type instanceof Class<?>)         return ((Class<?>) type).getComponentType();
            if (type instanceof GenericArrayType) return TypeUtil.getActualType(root, ((GenericArrayType) type).getGenericComponentType());
            else                                  return type;
        }
    }

}
