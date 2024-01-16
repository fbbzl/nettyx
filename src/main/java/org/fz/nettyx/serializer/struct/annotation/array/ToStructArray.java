package org.fz.nettyx.serializer.struct.annotation.array;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static org.fz.nettyx.serializer.struct.StructSerializer.isBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.newBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.newStruct;

import io.netty.buffer.ByteBuf;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructUtils;
import org.fz.nettyx.serializer.struct.basic.Basic;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2024/1/16 16:06
 */
public @interface ToStructArray {



    /**
     * New array t [ ].
     *
     * @param <T> the type parameter
     * @param arrayField the array field
     * @param arrayLength the array length
     * @return the t [ ]
     */
    public static <T> T[] newArray(Field arrayField, int arrayLength) {
        return (T[]) Array.newInstance(arrayField.getType().getComponentType(), arrayLength);
    }

    /**
     * New array t [ ].
     *
     * @param <T> the type parameter
     * @param componentType the component type
     * @param length the length
     * @return the t [ ]
     */
    public static <T> T[] newArray(Class<?> componentType, int length) {
        return (T[]) Array.newInstance(componentType, length);
    }

    /**
     * Fill array t [ ].
     *
     * @param <T> the type parameter
     * @param arrayValue the array value
     * @param elementType the element type
     * @param length the length
     * @return the t [ ]
     */
    public static <T> T[] fillArray(T[] arrayValue, Class<T> elementType, int length) {
        T[] filledArray = (T[]) Array.newInstance(elementType, length);
        System.arraycopy(arrayValue, 0, filledArray, 0, arrayValue.length);
        return filledArray;
    }

    /**
     * Read array e [ ].
     *
     * @param <E> the type parameter
     * @param elementType the element type
     * @param length the length
     * @param byteBuf the byte buf
     * @return the e [ ]
     */
    public static <E> E[] readArray(Class<E> elementType, int length, ByteBuf byteBuf) {
        E[] array = newArray(elementType, length);

        Object[] arrayValue =
            isBasic(elementType) ? readBasicArray((Basic<?>[]) array, byteBuf) : readStructArray(array, byteBuf);

        return (E[]) arrayValue;
    }

    /**
     * convert to basic array
     */
    private static <B extends Basic<?>> B[] readBasicArray(B[] basics, ByteBuf arrayBuf) {
        Class<B> elementType = (Class<B>) basics.getClass().getComponentType();

        for (int i = 0; i < basics.length; i++) {
            basics[i] = newBasic(elementType, arrayBuf);
        }

        return basics;
    }

    /**
     * convert to struct array
     */
    private static <S> S[] readStructArray(S[] structs, ByteBuf arrayBuf) {
        Class<S> elementType = (Class<S>) structs.getClass().getComponentType();

        for (int i = 0; i < structs.length; i++) {
            structs[i] = StructSerializer.read(arrayBuf, elementType);
        }

        return structs;
    }

    /**
     * Write array.
     *
     * @param <T> the type parameter
     * @param arrayValue the array value
     * @param elementType the element type
     * @param declaredLength the declared length
     * @param writingBuf the writing buf
     */
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

    private static void writeBasicArray(Basic<?>[] basicArray, Class<Basic<?>> basicType, ByteBuf writingBuf) {
        for (Basic<?> basic : basicArray) {
            writingBuf.writeBytes(defaultIfNull(basic, () -> StructUtils.newEmptyBasic(basicType)).getBytes());
        }
    }

    private static void writeBasicArray(Basic<?>[] basicArray, int elementBytesLength, ByteBuf writingBuf) {
        for (Basic<?> basic : basicArray) {
            if (basic == null) writingBuf.writeBytes(new byte[elementBytesLength]);
            else               writingBuf.writeBytes(basic.getBytes());
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
