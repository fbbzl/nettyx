package org.fz.nettyx.serializer.struct.annotation.array;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static org.fz.nettyx.serializer.struct.StructSerializer.isBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.getComponentType;
import static org.fz.nettyx.serializer.struct.StructUtils.newStruct;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.PropertyHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructUtils;
import org.fz.nettyx.serializer.struct.basic.Basic;
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
                (structElementType = getComponentType(field)) != Object.class ? serializer.getArrayFieldActualType(field)
                    : structElementType;

            Throws.ifTrue(structElementType == Object.class, new TypeJudgmentException(field));

            return readStructArray(structElementType, annotation.length(), serializer.getByteBuf());
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object value, ToStructArray annotation,
            ByteBuf writing) {
            ReadWriteHandler.super.doWrite(serializer, field, value, annotation, writing);
            Class<?> structElementType =
                (structElementType = getComponentType(field)) != Object.class ? serializer.getArrayFieldActualType(field)
                    : structElementType;


            Throws.ifTrue(structElementType == Object.class, new TypeJudgmentException(field));

            int declaredLength = annotation.length(), elementBytesSize = StructUtils.findBasicSize(basicElementType);

            Basic<?>[] basicArray = (Basic<?>[]) arrayValue;

            if (basicArray == null) {
                writing.writeBytes(new byte[elementBytesSize * declaredLength]);
                return;
            }
            if (basicArray.length < declaredLength) {
                basicArray = fillArray(basicArray, (Class<Basic<?>>) basicElementType, declaredLength);
            }

            writeBasicArray(basicArray, declaredLength, writing);

        }

        public static <T> T[] newArray(Field arrayField, int arrayLength) {
            return (T[]) Array.newInstance(arrayField.getType().getComponentType(), arrayLength);
        }

        public static <T> T[] newArray(Class<?> componentType, int length) {
            return (T[]) Array.newInstance(componentType, length);
        }

        public static <T> T[] fillArray(T[] arrayValue, Class<T> elementType, int length) {
            T[] filledArray = (T[]) Array.newInstance(elementType, length);
            System.arraycopy(arrayValue, 0, filledArray, 0, arrayValue.length);
            return filledArray;
        }

        private static <S> S[] readStructArray(Class<S> elementType, int length, ByteBuf arrayBuf) {
            S[] structs = newArray(elementType, length);

            for (int i = 0; i < structs.length; i++) {
                structs[i] = StructSerializer.read(arrayBuf, elementType);
            }

            return structs;
        }

        public static <T> void writeArray(Object arrayValue, Class<T> elementType, int declaredLength,
            ByteBuf writingBuf) {
            T[] array = (T[]) arrayValue;
            if (declaredLength < array.length) {
                throw new IllegalArgumentException(
                    "array length exceed the declared length in annotation [" + ToBasicArray.class + "]");
            }
            if (declaredLength > array.length) {
                array = fillArray(array, elementType, declaredLength);
            }

            if (isBasic(elementType)) {
                writeBasicArray((Basic<?>[]) array, (Class<Basic<?>>) elementType, writingBuf);
            } else {
                writeStructArray(array, elementType, writingBuf);
            }
        }

        public static <T> void writeArray(Object arrayValue, int elementBytesLength, int declaredLength,
            ByteBuf writingBuf) {
            T[] array = (T[]) arrayValue;
            if (declaredLength != array.length) {
                throw new IllegalArgumentException("array length exceed the declared length in annotation [" + ToBasicArray.class + "]");
            }

            if (isBasic(elementType)) {
                writeBasicArray((Basic<?>[]) array, elementBytesLength, writingBuf);
            } else {
                writeStructArray(array, elementBytesLength, writingBuf);
            }
        }


        private static void writeStructArray(Object[] structArray, Class<?> structType, ByteBuf writingBuf) {
            for (Object struct : structArray) {
                writingBuf.writeBytes(StructSerializer.write(defaultIfNull(struct, () -> newStruct(structType))));
            }
        }

        private static void writeStructArray(Object[] basicArray, int elementBytesLength, ByteBuf writingBuf) {
            for (Object struct : basicArray) {
                if (struct == null) writingBuf.writeBytes(new byte[elementBytesLength]);
                else                writingBuf.writeBytes(StructSerializer.write(struct));
            }
        }
    }


}
