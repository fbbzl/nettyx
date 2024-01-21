package org.fz.nettyx.serializer.struct.annotation;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.fz.nettyx.serializer.struct.StructSerializer.isBasic;
import static org.fz.nettyx.serializer.struct.StructSerializer.isStruct;
import static org.fz.nettyx.serializer.struct.StructUtils.findBasicSize;
import static org.fz.nettyx.serializer.struct.StructUtils.getComponentType;
import static org.fz.nettyx.serializer.struct.StructUtils.newBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.newStruct;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ClassUtil;
import io.netty.buffer.ByteBuf;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Supplier;
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
                writeArray(arrayValue, writing, elementType, length, elementBytesSize);
            } catch (TypeJudgmentException typeJudgmentException) {
                throw new UnsupportedOperationException("can not determine the type of field [" + field + "]");
            }
        }

        public static <T> T[] readArray(ByteBuf buf, Class<?> elementType, int length) {
            if (isBasic(elementType)) {
                return (T[]) readBasicArray(elementType, length, buf);
            } else if (isStruct(elementType)) {
                return (T[]) readStructArray(elementType, length, buf);
            } else throw new TypeJudgmentException();
        }

        public static void writeArray(Object arrayValue, ByteBuf writing, Class<?> elementType, int length,
            int elementBytesSize) {
            if (isBasic(elementType)) {
                Basic<?>[] basicArray = (Basic<?>[]) arrayValue;

                if (basicArray == null) {
                    writing.writeBytes(new byte[elementBytesSize * length]);
                    return;
                }

                writeBasicArray(basicArray, elementBytesSize, length, writing);
            } else if (isStruct(elementType)) {
                Object[] structArray = (Object[]) arrayValue;
                writeStructArray(defaultIfNull(structArray, () -> newArray(elementType, length)), elementType, length,
                    writing);
            }
            else throw new TypeJudgmentException();
        }

        public static <T> Collection<T> readCollection(ByteBuf buf, Class<?> elementType, int length, Supplier<Collection<?>> collSupplier) {
            if (isBasic(elementType)) {
                return (Collection<T>) readBasicCollection(buf, elementType, length, collSupplier);
            } else if (isStruct(elementType)) {
                return readStructCollection(buf, elementType, length, collSupplier);
            } else
                throw new TypeJudgmentException();
        }

        public static void writeCollection(Collection<?> collection, Class<?> elementType, int length,
            ByteBuf writing) {
            if (isBasic(elementType)) {
                writeBasicCollection(collection, findBasicSize(elementType), length, writing);
            } else if (isStruct(elementType)) {
                writeStructCollection(collection, elementType, length, writing);
            } else {
                throw new TypeJudgmentException();
            }
        }

        //**************************************         private start         ***************************************//

        private static <T> T[] newArray(Class<?> componentType, int length) {
            return (T[]) Array.newInstance(componentType, length);
        }

        private static <B extends Basic<?>> B[] readBasicArray(Class<?> elementType, int length,
            ByteBuf arrayBuf) {
            B[] basics = newArray(elementType, length);

            for (int i = 0; i < basics.length; i++) {
                basics[i] = newBasic(elementType, arrayBuf);
            }

            return basics;
        }

        private static <B extends Basic<?>> Collection<B> readBasicCollection(ByteBuf arrayBuf, Class<?> elementType,
            int length, Collection<?> collSup) {
            return (Collection<B>) CollUtil.addAll(collSup, readBasicArray(elementType, length, arrayBuf));
        }

        private static <S> S[] readStructArray(Class<S> elementType, int length, ByteBuf arrayBuf) {
            S[] structs = newArray(elementType, length);

            for (int i = 0; i < structs.length; i++) {
                structs[i] = StructSerializer.read(arrayBuf, elementType);
            }

            return structs;
        }

        private static <T> Collection<T> readStructCollection(ByteBuf arrayBuf, Class<?> elementType, int length,
            Supplier<Collection<?>> collSup) {
            return (Collection<T>) CollUtil.addAll(collSup.get(), readStructArray(elementType, length, arrayBuf));
        }

        private static void writeBasicArray(Basic<?>[] basicArray, int elementBytesSize, int length, ByteBuf writing) {
            for (int i = 0; i < length; i++) {
                if (i > basicArray.length - 1) {
                    writing.writeBytes(new byte[elementBytesSize]);
                } else {
                    Basic<?> basic = basicArray[i];
                    if (basic == null) writing.writeBytes(new byte[elementBytesSize]);
                    else               writing.writeBytes(basicArray[i].getBytes());
                }
            }
        }

        private static void writeBasicCollection(Collection<?> collection, int elementBytesSize, int length,
            ByteBuf writing) {
            Iterator<?> iterator = collection.iterator();
            for (int i = 0; i < length; i++) {
                if (iterator.hasNext()) {
                    Basic<?> basic = (Basic<?>) iterator.next();
                    if (basic == null) writing.writeBytes(new byte[elementBytesSize]);
                    else              writing.writeBytes(basic.getBytes());
                } else {
                    writing.writeBytes(new byte[elementBytesSize]);
                }
            }
        }

        private static <T> void writeStructArray(Object[] structArray, Class<T> elementType, int length,
            ByteBuf writing) {
            for (int i = 0; i < length; i++) {
                if (i > structArray.length - 1) writing.writeBytes(StructSerializer.write(newStruct(elementType)));
                else                            writing.writeBytes(StructSerializer.write(defaultIfNull(structArray[i], () -> newStruct(elementType))));
            }
        }

        private static void writeStructCollection(Collection<?> collection, Class<?> elementType, int length,
            ByteBuf writing) {
            Iterator<?> iterator = collection.iterator();

            for (int i = 0; i < length; i++) {
                if (iterator.hasNext()) writing.writeBytes(StructSerializer.write(defaultIfNull(iterator.next(), () -> newStruct(elementType))));
                else                    writing.writeBytes(StructSerializer.write(newStruct(elementType)));
            }
        }

        //**************************************         private end           ***************************************//

    }

}
