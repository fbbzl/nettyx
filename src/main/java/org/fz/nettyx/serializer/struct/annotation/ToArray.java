package org.fz.nettyx.serializer.struct.annotation;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.StructSerializer.isBasic;
import static org.fz.nettyx.serializer.struct.StructSerializer.isStruct;
import static org.fz.nettyx.serializer.struct.StructUtils.getComponentType;
import static org.fz.nettyx.serializer.struct.StructUtils.newBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.newStruct;

import cn.hutool.core.util.ClassUtil;
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

    @SuppressWarnings("unchecked")
    class ToArrayHandler implements PropertyHandler.ReadWriteHandler<ToArray> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToArray annotation) {
            Class<?> elementType = !ClassUtil.isAssignable(Basic.class, (elementType = getComponentType(field)))
                ? serializer.getArrayFieldActualType(field) : elementType;

            Throws.ifTrue(elementType == Object.class, new TypeJudgmentException(field));

            int length = annotation.length();

            try {
                return readArray(serializer.getByteBuf(), elementType, length);
            } catch (TypeJudgmentException typeJudgmentException) {
                throw new UnsupportedOperationException("can not determine the type of field [" + field + "]");
            }
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object arrayValue, ToArray annotation,
            ByteBuf writing) {
            Class<?> elementType = !ClassUtil.isAssignable(Basic.class, (elementType = getComponentType(field)))
                ? serializer.getArrayFieldActualType(field) : elementType;

            Throws.ifTrue(elementType == Object.class, new TypeJudgmentException(field));

            int length = annotation.length();
            int elementBytesSize = StructUtils.findBasicSize(elementType);

            try {
                writeArray(arrayValue, writing, elementType, elementBytesSize, length);
            } catch (TypeJudgmentException typeJudgmentException) {
                throw new UnsupportedOperationException("can not determine the type of field [" + field + "]");
            }
        }

        public static Object[] readArray(ByteBuf buf, Class<?> elementType, int length) {
            if (isBasic(elementType)) {
                return readBasicArray(elementType, length, buf);
            } else if (isStruct(elementType)) {
                return readStructArray(elementType, length, buf);
            } else throw new TypeJudgmentException();
        }

        public static void writeArray(Object arrayValue, ByteBuf writing, Class<?> elementType,
            int elementBytesSize, int length) {
            if (isBasic(elementType)) {
                Basic<?>[] basicArray = (Basic<?>[]) arrayValue;

                if (basicArray == null) {
                    writing.writeBytes(new byte[elementBytesSize * length]);
                    return;
                }
                if (basicArray.length < length) {
                    basicArray = fillArray(basicArray, (Class<Basic<?>>) elementType, length);
                }

                writeBasicArray(basicArray, length, writing);
            } else if (isStruct(elementType)) {
                writeStructArray(arrayValue, elementType, length, writing);
            }
            else throw new TypeJudgmentException();
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

        //**************************************         private start         ***************************************//

        private static <T> T[] newArray(Class<?> componentType, int length) {
            return (T[]) Array.newInstance(componentType, length);
        }

        private static <T> T[] fillArray(T[] arrayValue, Class<T> elementType, int length) {
            T[] filledArray = (T[]) Array.newInstance(elementType, length);
            System.arraycopy(arrayValue, 0, filledArray, 0, arrayValue.length);
            return filledArray;
        }

        private static <B extends Basic<?>> B[] readBasicArray(Class<?> elementType, int declaredLength,
            ByteBuf arrayBuf) {
            B[] basics = newArray(elementType, declaredLength);

            for (int i = 0; i < basics.length; i++) {
                basics[i] = newBasic(elementType, arrayBuf);
            }

            return basics;
        }

        private static <S> S[] readStructArray(Class<S> elementType, int length, ByteBuf arrayBuf) {
            S[] structs = newArray(elementType, length);

            for (int i = 0; i < structs.length; i++) {
                structs[i] = StructSerializer.read(arrayBuf, elementType);
            }

            return structs;
        }

        private static void writeBasicArray(Basic<?>[] basicArray, int elementBytesLength, ByteBuf writingBuf) {
            for (Basic<?> basic : basicArray) {
                if (basic == null) {
                    writingBuf.writeBytes(new byte[elementBytesLength]);
                } else {
                    writingBuf.writeBytes(basic.getBytes());
                }
            }
        }

        private static <T> void writeStructArray(Object arrayValue, Class<T> elementType, int declaredLength,
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

        //**************************************         private end           ***************************************//

    }

}
