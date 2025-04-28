package org.fz.nettyx.serializer.struct;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Iterator;
import java.util.List;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static org.fz.nettyx.serializer.struct.StructHelper.*;
import static org.fz.nettyx.serializer.struct.StructSerializerContext.STRUCT_DEFINITION_CACHE;

/**
 * The top-level parent class of all custom serialization processors default is not singleton
 *
 * @author fengbinbin
 * @since 2022 -01-16 16:39
 */
@SuppressWarnings("all")
public interface StructFieldHandler<A extends Annotation> {

    StructFieldHandler<? extends Annotation> DEFAULT_READ_WRITE_HANDLER = new StructFieldHandler() {
        @Override
        public boolean isSingleton() {
            return true;
        }
    };

    /**
     * config the handler instance if is singleton
     *
     * @return if is singleton handler
     */
    default boolean isSingleton() {
        return false;
    }

    default Object doRead(Type root, Object earlyObject, StructField structField, ByteBuf reading, A annotation) {
        Field wrapped    = structField.wrapped();
        Type  actualType = structField.type(root);

        if (isBasic(root, wrapped)) return readBasic((Class<? extends Basic<?>>) actualType, reading);
        if (isStruct(root, wrapped)) return readStruct(actualType, reading);

        throw new TypeJudgmentException(structField);
    }

    default void doWrite(Type root, Object struct, StructField structField, Object fieldVal, ByteBuf writing,
                         A annotation) {
        Field wrapped = structField.wrapped();
        Type actualType = structField.type(root);
        if (isBasic(root, wrapped)) {
            writeBasic((Basic<?>) basicNullDefault(fieldVal, (Class<? extends Basic<?>>) actualType), writing);
            return;
        }

        if (isStruct(root, wrapped)) {
            writeStruct(actualType, structNullDefault(fieldVal, actualType), writing);
            return;
        }
        throw new TypeJudgmentException(structField);
    }

    default boolean isBasic(Type root, Field field) {
        return isBasic(root, field.getGenericType());
    }

    default boolean isStruct(Type root, Field field) {
        return isStruct(root, field.getGenericType());
    }

    default <B extends Basic<?>> B readBasic(Class<? extends Basic<?>> basicType, ByteBuf byteBuf) {
        return newBasic(basicType, byteBuf);
    }

    public static boolean isBasic(Type root, Type type) {
        if (type instanceof Class<?> clazz)               return Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;
        if (type instanceof TypeVariable<?> typeVariable) return isBasic(root, TypeUtil.getActualType(root, typeVariable));

        return false;
    }

    public static boolean isStruct(Type root, Type type) {
        if (type instanceof Class<?> clazz)                      return STRUCT_DEFINITION_CACHE.containsKey(clazz);
        if (type instanceof ParameterizedType parameterizedType) return isStruct(root, parameterizedType.getRawType());
        if (type instanceof TypeVariable<?> typeVariable)        return isStruct(root, TypeUtil.getActualType(root, typeVariable));

        return false;
    }

    default <S> S readStruct(Type structType, ByteBuf byteBuf) {
        return StructSerializer.toStruct(structType, byteBuf);
    }

    default <T> T[] readArray(Type root, Type elementType, ByteBuf byteBuf, int length) {
        if (isBasic(root, elementType))  return (T[]) readBasicArray((Class<? extends Basic<?>>) elementType, byteBuf, length);
        if (isStruct(root, elementType)) return readStructArray(root, elementType, byteBuf, length);
        else                             throw new TypeJudgmentException();
    }

    default <B extends Basic<?>> B[] readBasicArray(Class<? extends Basic<?>> elementType, ByteBuf byteBuf,
                                                    int length) {
        B[] basics = newArray(elementType, length);

        for (int i = 0; i < basics.length; i++) basics[i] = newBasic(elementType, byteBuf);

        return basics;
    }

    default <S> S[] readStructArray(Type root, Type elementType, ByteBuf byteBuf, int length) {
        S[] structs = newArray(elementType, length);
        Type elementActualType = TypeUtil.getActualType(root, elementType);

        for (int i = 0; i < structs.length; i++) structs[i] = readStruct(elementActualType, byteBuf);

        return structs;
    }

    default <T> List<T> readList(Type root, Type elementType, ByteBuf byteBuf, int length) {
        if (isBasic(root, elementType))  return (List<T>) readBasicList(root, (Class<? extends Basic<?>>) elementType, byteBuf, length);
        else
        if (isStruct(root, elementType)) return readStructList(root, elementType, byteBuf, length);
        else throw new TypeJudgmentException();
    }

    default <B extends Basic<?>> List<B> readBasicList(Type root, Class<? extends Basic<?>> elementType,
                                                       ByteBuf byteBuf, int length) {
        return CollUtil.newArrayList(readBasicArray(elementType, byteBuf, length));
    }

    default <T> List<T> readStructList(Type root, Type elementType, ByteBuf byteBuf, int length) {
        return CollUtil.newArrayList(readStructArray(root, elementType, byteBuf, length));
    }

    default <B extends Basic<?>> void writeBasic(Object basicValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(((B) (basicValue)).getBytes());
    }

    default <S> void writeStruct(Type root, S structValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(StructSerializer.toByteBuf(root, structValue));
    }

    default void writeArray(Type root, Object arrayValue, Type componentType, int length, ByteBuf writing) {
        if (isBasic(root, componentType)) {
            int        basicElementSize = StructHelper.findBasicSize(componentType);
            Basic<?>[] basicArray       = (Basic<?>[]) arrayValue;

            if (basicArray == null) {
                writing.writeBytes(new byte[basicElementSize * length]); return;
            }

            writeBasicArray(basicArray, basicElementSize, length, writing);
        }
        else if (isStruct(root, componentType)) {
            writeStructArray(arrayNullDefault(arrayValue, componentType, length), componentType, length, writing);
        }
        else throw new TypeJudgmentException();
    }

    default void writeBasicArray(Basic<?>[] basicArray, int elementBytesSize, int length, ByteBuf writing) {
        for (int i = 0; i < length; i++) {
            if (i < basicArray.length) {
                Basic<?> basic = basicArray[i]; if (basic == null) writing.writeBytes(new byte[elementBytesSize]);
                else writing.writeBytes(basicArray[i].getBytes());
            }
            else writing.writeBytes(new byte[elementBytesSize]);
        }
    }

    default <T> void writeStructArray(T[] structArray, Type elementType, int length, ByteBuf writing) {
        for (int i = 0; i < length; i++) {
            if (i < structArray.length) writing.writeBytes(StructSerializer.toByteBuf(elementType,
                                                                                      structNullDefault(structArray[i], elementType)));
            else writing.writeBytes(StructSerializer.toByteBuf(newStruct(elementType)));
        }
    }

    default void writeList(Type root, List<?> list, Type elementType, int length, ByteBuf writing) {
        if (isBasic(root, elementType))  writeBasicList(list, findBasicSize(elementType), length, writing);
        else
        if (isStruct(root, elementType)) writeStructList(list, elementType, length, writing);
        else
        throw new TypeJudgmentException();
    }

    default void writeBasicList(List<?> list, int elementBytesSize, int length, ByteBuf writing) {
        Iterator<?> iterator = list.iterator(); for (int i = 0; i < length; i++) {
            if (iterator.hasNext()) {
                Basic<?> basic = (Basic<?>) iterator.next();
                if (basic == null) writing.writeBytes(new byte[elementBytesSize]);
                else writing.writeBytes(basic.getBytes());
            }
            else writing.writeBytes(new byte[elementBytesSize]);
        }
    }

    default void writeStructList(List<?> list, Type elementType, int length, ByteBuf writing) {
        Iterator<?> iterator = list.iterator();
        for (int i = 0; i < length; i++) {
            if (iterator.hasNext())
                writing.writeBytes(StructSerializer.toByteBuf(elementType, structNullDefault(iterator.next(), elementType)));
            else
                writing.writeBytes(StructSerializer.toByteBuf(elementType, newStruct(elementType)));
        }
    }

    default <T> T basicNullDefault(Object fieldValue, Class<? extends Basic<?>> fieldActualType) {
        return (T) defaultIfNull(fieldValue, () -> newEmptyBasic(fieldActualType));
    }

    default <T> T structNullDefault(Object fieldValue, Type fieldActualType) {
        return (T) defaultIfNull(fieldValue, () -> newStruct(fieldActualType));
    }

    default <T> T[] arrayNullDefault(Object arrayValue, Type componentType, int length) {
        return (T[]) defaultIfNull(arrayValue, () -> newArray(componentType, length));
    }

}
