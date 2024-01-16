package org.fz.nettyx.serializer.struct.annotation.array;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static org.fz.nettyx.serializer.struct.StructUtils.getComponentType;
import static org.fz.nettyx.serializer.struct.StructUtils.newStruct;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Array;
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
                (structElementType = getComponentType(field)) != Object.class ? serializer.getArrayFieldActualType(
                    field) : structElementType;

            Throws.ifTrue(structElementType == Object.class, new TypeJudgmentException(field));

            int declaredLength = annotation.length();

            return readStructArray(structElementType, declaredLength, serializer.getByteBuf());
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object arrayValue, ToStructArray annotation,
            ByteBuf writing) {
            Class<?> structElementType =
                (structElementType = getComponentType(field)) != Object.class ? serializer.getArrayFieldActualType(
                    field) : structElementType;

            Throws.ifTrue(structElementType == Object.class, new TypeJudgmentException(field));

            int declaredLength = annotation.length();

            writeStructArray(arrayValue, structElementType, declaredLength, writing);
        }

        public static <T> T[] newArray(Class<?> componentType, int length) {
            return (T[]) Array.newInstance(componentType, length);
        }

        private static <S> S[] readStructArray(Class<S> elementType, int length, ByteBuf arrayBuf) {
            S[] structs = newArray(elementType, length);

            for (int i = 0; i < structs.length; i++) {
                structs[i] = StructSerializer.read(arrayBuf, elementType);
            }

            return structs;
        }

        public static <T> void writeStructArray(Object arrayValue, Class<T> elementType, int declaredLength,
            ByteBuf writingBuf) {
            T[] array = (T[]) arrayValue;

            if (array == null) {
                array = newArray(elementType, declaredLength);
            }

            if (array.length < declaredLength) {
                array = fillArray(array, elementType, declaredLength);
            }

            for (Object struct : array) {
                writingBuf.writeBytes(StructSerializer.write(defaultIfNull(struct, () -> newStruct(elementType))));
            }

        }

        public static <T> T[] fillArray(T[] arrayValue, Class<T> elementType, int length) {
            T[] filledArray = (T[]) Array.newInstance(elementType, length);
            System.arraycopy(arrayValue, 0, filledArray, 0, arrayValue.length);
            return filledArray;
        }

    }

}
