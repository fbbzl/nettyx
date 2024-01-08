package org.fz.nettyx.serializer.struct.annotation.collection;

import static io.netty.buffer.Unpooled.buffer;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.StructSerializer.isBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.getComponentType;
import static org.fz.nettyx.serializer.struct.StructUtils.newBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.newStruct;
import static org.fz.nettyx.serializer.struct.StructUtils.nullDefault;

import io.netty.buffer.ByteBuf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import org.fz.nettyx.serializer.struct.Basic;
import org.fz.nettyx.serializer.struct.PropertyHandler;
import org.fz.nettyx.serializer.struct.StructSerializer;

/**
 * array field must use this to assign array length!!!
 *
 * @author fengbinbin
 * @since 2021-10-20 08:18
 **/

@Documented
@Target(FIELD)
@Retention(RUNTIME)
public @interface ToArray {

    /**
     * array length
     */
    int length() default 0;

    @SuppressWarnings("unchecked")
    class ToArrayHandler implements PropertyHandler.ReadWriteHandler<ToArray> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToArray annotation) {
            Class<?> elementType = getComponentType(field);
            int length = annotation.length();
            return readArray(elementType, length, serializer.getByteBuf());
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object arrayValue, ToArray annotation,
            ByteBuf writingBuffer) {
            Class<?> elementType = getComponentType(field);
            int declaredLength = annotation.length();

            Object[] array = (Object[]) nullDefault(arrayValue, () -> newArray(field, declaredLength));

            writeArray(array, elementType, declaredLength, writingBuffer);
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

        public static <E> E[] readArray(Class<E> elementType, int length, ByteBuf byteBuf) {
            E[] array = newArray(elementType, length);

            Object[] arrayValue = isBasic(elementType) ?
                readBasicArray((Basic<?>[]) array, byteBuf) :
                readStructArray(array, byteBuf);

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

        public static <T> void writeArray(Object arrayValue, Class<T> elementType, int declaredLength, ByteBuf writingBuf) {
            // cast to array
            T[] array = (T[]) arrayValue;
            if (declaredLength < array.length) throw new IllegalArgumentException("array length exceed the declared length in annotation [" + ToArray.class + "]");
            if (declaredLength > array.length) array = fillArray(array, elementType, declaredLength);

            if (isBasic(elementType)) writeBasicArray((Basic<?>[]) array, (Class<Basic<?>>) elementType, writingBuf);
            else                      writeStructArray(array, elementType, writingBuf);
        }

        /**
         * write basic array
         */
        private static void writeBasicArray(Basic<?>[] basicArray, Class<Basic<?>> basicType, ByteBuf writingBuf) {
            for (Basic<?> basic : basicArray) {
                writingBuf.writeBytes(nullDefault(basic, () -> newBasic(basicType, buffer())).getBytes());
            }
        }

        /**
         * write struct array
         */
        private static void writeStructArray(Object[] structArray, Class<?> structType, ByteBuf writingBuf) {
            for (Object struct : structArray) {
                writingBuf.writeBytes(StructSerializer.write(nullDefault(struct, () -> newStruct(structType))));
            }
        }
    }

}
