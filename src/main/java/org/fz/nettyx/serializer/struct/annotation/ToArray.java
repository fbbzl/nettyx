package org.fz.nettyx.serializer.struct.annotation;

import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import org.fz.erwin.exception.Throws;
import org.fz.nettyx.exception.StructFieldHandlerException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.StructFieldHandler;
import org.fz.nettyx.serializer.struct.StructHelper;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructSerializerContext.StructDefinition.StructField;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;

import static cn.hutool.core.util.ReflectUtil.getFields;
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
    int length() default -1;

    /**
     * flexible array, it must be placed in the last field
     */
    boolean flexible() default false;

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

            boolean flexible = toArray.flexible();

            return serializer.readArray(componentType, reading, length, flexible);
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

            boolean flexible = toArray.flexible();

            serializer.writeArray(fieldVal, componentType, length, writing, flexible);
        }

        static Type getComponentType(
                Type root,
                Type type)
        {
            if (type instanceof Class<?> clazz)
                return clazz.getComponentType();
            if (type instanceof GenericArrayType genericArrayType)
                return TypeUtil.getActualType(root, genericArrayType.getGenericComponentType());

            return type;
        }

        @Override
        public void doAnnotationValid(ToArray toArray, Field field) {
            if (toArray.length() < 0 && !toArray.flexible())
                throw new StructFieldHandlerException("array field must use @ToArray to assign array length or be flexible");
            if (toArray.flexible()) {
                Field[] structFields = getFields(field.getDeclaringClass(), StructHelper::legalStructField);

                if (structFields[structFields.length - 1] != field) {
                    throw new StructFieldHandlerException("flexible array field must be the last field");
                }
            }
        }
    }

}
