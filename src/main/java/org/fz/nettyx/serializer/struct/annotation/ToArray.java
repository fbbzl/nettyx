package org.fz.nettyx.serializer.struct.annotation;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.StructSerializer.isBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.getComponentType;
import static org.fz.nettyx.serializer.struct.StructUtils.newBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.newStruct;

import io.netty.buffer.ByteBuf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import org.fz.nettyx.exception.ParameterizedTypeException;
import org.fz.nettyx.serializer.struct.PropertyHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.fz.nettyx.serializer.struct.StructUtils;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.util.Throws;

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

    /**
     * The type To array handler.
     */
    @SuppressWarnings("unchecked")
    class ToArrayHandler implements PropertyHandler.ReadWriteHandler<ToArray> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToArray annotation) {
            Class<?> elementType =
                (elementType = getComponentType(field)) == Object.class ? serializer.getArrayFieldActualType(field)
                    : elementType;

            Throws.ifTrue(elementType == Object.class, new ParameterizedTypeException(field));

            int length = annotation.length();

            return readArray(elementType, length, serializer.getByteBuf());
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object arrayValue, ToArray annotation,
            ByteBuf writing) {
            Class<?> elementType =
                (elementType = getComponentType(field)) == Object.class ? serializer.getArrayFieldActualType(field)
                    : elementType;

            Throws.ifTrue(elementType == Object.class, new ParameterizedTypeException(field));

            int declaredLength = annotation.length();

            Object[] array = (Object[]) defaultIfNull(arrayValue, () -> newArray(field, declaredLength));

            writeArray(array, elementType, declaredLength, writing);
        }

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
            // cast to array
            T[] array = (T[]) arrayValue;
            if (declaredLength < array.length) {
                throw new IllegalArgumentException(
                    "array length exceed the declared length in annotation [" + ToArray.class + "]");
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

        /**
         * write basic array
         */
        private static void writeBasicArray(Basic<?>[] basicArray, Class<Basic<?>> basicType, ByteBuf writingBuf) {
            for (Basic<?> basic : basicArray) {
                writingBuf.writeBytes(defaultIfNull(basic, () -> StructUtils.newEmptyBasic(basicType)).getBytes());
            }
        }

        private static void writeBasicArray(Basic<?>[] basicArray, int elementBytesSize, ByteBuf writingBuf) {
            for (Basic<?> basic : basicArray) {
                if (basic == null) writingBuf.writeBytes(new byte[elementBytesSize]);
                else               writingBuf.writeBytes(basic.getBytes());
            }
        }

        /**
         * write struct array
         */
        private static void writeStructArray(Object[] structArray, Class<?> structType, ByteBuf writingBuf) {
            for (Object struct : structArray) {
                writingBuf.writeBytes(StructSerializer.write(defaultIfNull(struct, () -> newStruct(structType))));
            }
        }

        private static void writeStructArray(Basic<?>[] basicArray, int elementBytesSize, ByteBuf writingBuf) {
            for (Basic<?> basic : basicArray) {
                if (basic == null) writingBuf.writeBytes(new byte[elementBytesSize]);
                else               writingBuf.writeBytes(basic.getBytes());
            }
        }
    }

}
