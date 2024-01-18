package org.fz.nettyx.serializer.struct.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.StructUtils.getComponentType;
import static org.fz.nettyx.serializer.struct.StructUtils.newBasic;

import cn.hutool.core.util.ClassUtil;
import io.netty.buffer.ByteBuf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
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

    @SuppressWarnings("unchecked")
    class ToBasicArrayHandler implements PropertyHandler.ReadWriteHandler<ToBasicArray> {

        @Override
        public Object doRead(StructSerializer serializer, Field field, ToBasicArray annotation) {
            Class<? extends Basic<?>> basicElementType =
                !ClassUtil.isAssignable(Basic.class, (basicElementType = getComponentType(field))) ? serializer.getArrayFieldActualType(field)
                    : basicElementType;

            Throws.ifNotAssignable(Basic.class, basicElementType,
                "type [" + basicElementType + "] is not a basic type");

            return readBasicArray(basicElementType, annotation.length(), serializer.getByteBuf());
        }

        @Override
        public void doWrite(StructSerializer serializer, Field field, Object arrayValue, ToBasicArray annotation,
            ByteBuf writing) {
            Class<? extends Basic<?>> basicElementType =
                !ClassUtil.isAssignable(Basic.class, (basicElementType = getComponentType(field))) ? serializer.getArrayFieldActualType(field)
                    : basicElementType;

            Throws.ifNotAssignable(Basic.class, basicElementType,
                "type [" + basicElementType + "] is not a basic type");

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

        public static <T> T[] newArray(Class<?> componentType, int length) {
            return (T[]) Array.newInstance(componentType, length);
        }

        public static <T> T[] fillArray(T[] arrayValue, Class<T> elementType, int length) {
            T[] filledArray = (T[]) Array.newInstance(elementType, length);
            System.arraycopy(arrayValue, 0, filledArray, 0, arrayValue.length);
            return filledArray;
        }

        private static <B extends Basic<?>> B[] readBasicArray(Class<B> elementType, int declaredLength,
            ByteBuf arrayBuf) {
            B[] basics = newArray(elementType, declaredLength);

            for (int i = 0; i < basics.length; i++) {
                basics[i] = newBasic(elementType, arrayBuf);
            }

            return basics;
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
    }

}
