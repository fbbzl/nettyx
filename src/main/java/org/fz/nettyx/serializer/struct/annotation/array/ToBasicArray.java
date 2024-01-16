package org.fz.nettyx.serializer.struct.annotation.array;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.StructSerializer.isBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.getComponentType;
import static org.fz.nettyx.serializer.struct.StructUtils.newBasic;

import io.netty.buffer.ByteBuf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import org.fz.nettyx.exception.TypeJudgmentException;
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
public @interface ToBasicArray {

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
    class ToArrayHandler implements PropertyHandler.ReadWriteHandler<ToBasicArray> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToBasicArray annotation) {
            Class<? extends Basic<?>> elementType =
                (elementType = getComponentType(field)) != Basic.class ? serializer.getArrayFieldActualType(field)
                    : elementType;

            Throws.ifTrue(elementType != Basic.class, new TypeJudgmentException(field));

            return readBasicArray(elementType, annotation.length(), serializer.getByteBuf());
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object arrayValue, ToBasicArray annotation,
            ByteBuf writing) {
            Class<? extends Basic<?>> basicElementType =
                (basicElementType = getComponentType(field)) != Basic.class ? serializer.getArrayFieldActualType(field)
                    : basicElementType;

            Throws.ifTrue(basicElementType != Basic.class, new TypeJudgmentException(field));

            int declaredLength = annotation.length(), elementBytesSize = StructUtils.newEmptyBasic(basicElementType)
                .getSize();

            Basic<?>[] basicArray = (Basic<?>[]) arrayValue;

            if (basicArray == null) {
                writing.writeBytes(new byte[elementBytesSize * declaredLength]);
                return;
            }
            if (basicArray.length < declaredLength) {
                basicArray = fillArray(basicArray, basicElementType, );
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

        private static <B extends Basic<?>> B[] readBasicArray(Class<B> elementType, int declaredLength, ByteBuf arrayBuf) {
            B[] basics = newArray(elementType, declaredLength);

            for (int i = 0; i < basics.length; i++) {
                basics[i] = newBasic(elementType, arrayBuf);
            }

            return basics;
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

            writeBasicArray((Basic<?>[]) array, (Class<Basic<?>>) elementType, writingBuf);
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

        private static void writeBasicArray(Basic<?>[] basicArray, int elementBytesLength, ByteBuf writingBuf) {
            for (Basic<?> basic : basicArray) {
                if (basic == null) writingBuf.writeBytes(new byte[elementBytesLength]);
                else               writingBuf.writeBytes(basic.getBytes());
            }
        }


    }

}
