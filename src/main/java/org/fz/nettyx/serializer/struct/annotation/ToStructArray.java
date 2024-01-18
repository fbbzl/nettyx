package org.fz.nettyx.serializer.struct.annotation;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.StructSerializer.isNotStruct;
import static org.fz.nettyx.serializer.struct.StructUtils.getComponentType;
import static org.fz.nettyx.serializer.struct.StructUtils.newStruct;

import io.netty.buffer.ByteBuf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
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

            Throws.ifTrue(isNotStruct(structElementType),
                "type [" + structElementType + "] is not a struct, please keep struct class with annotation @Struct");
            Throws.ifTrue(structElementType == Object.class, new TypeJudgmentException(field));

            int declaredLength = annotation.length();

            writeStructArray(arrayValue, structElementType, declaredLength, writing);
        }

        public static <T> T[] newArray(Class<?> componentType, int length) {
            return (T[]) Array.newInstance(componentType, length);
        }

        public static <S> S[] readStructArray(Class<S> elementType, int length, ByteBuf arrayBuf) {
            S[] structs = newArray(elementType, length);

            for (int i = 0; i < structs.length; i++) {
                structs[i] = StructSerializer.read(arrayBuf, elementType);
            }

            return structs;
        }

        public static <T> void writeStructArray(Object arrayValue, Class<T> elementType, int declaredLength,
            ByteBuf writing) {
            T[] array = (T[]) arrayValue;

            if (array == null) {
                array = newArray(elementType, declaredLength);
            }

            for (int i = 0; i < declaredLength; i++) {
                if (i > array.length - 1) {
                    writing.writeBytes(StructSerializer.write(newStruct(elementType)));
                } else {
                    writing.writeBytes(StructSerializer.write(defaultIfNull(array[i], () -> newStruct(elementType))));
                }
            }
        }

        public static void writeStructCollection(Collection<?> collection, Class<?> elementType, int declaredLength,
            ByteBuf writing) {
            Iterator<?> iterator = collection.iterator();

            for (int i = 0; i < declaredLength; i++) {
                if (iterator.hasNext()) {
                    writing.writeBytes(
                        StructSerializer.write(defaultIfNull(iterator.next(), () -> newStruct(elementType))));
                } else {
                    writing.writeBytes(StructSerializer.write(newStruct(elementType)));
                }
            }
        }

    }
}