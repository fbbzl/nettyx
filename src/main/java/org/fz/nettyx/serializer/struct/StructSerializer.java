package org.fz.nettyx.serializer.struct;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.util.TypeRefer;
import org.fz.util.exception.Throws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static io.netty.buffer.Unpooled.buffer;
import static org.fz.nettyx.serializer.struct.StructDefinition.STRUCT_DEFINITION_CACHE;
import static org.fz.nettyx.serializer.struct.StructHelper.*;

/**
 * the basic serializer of byte-work Provides a protocol based on byte offset partitioning fields
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:18
 */
@SuppressWarnings("unchecked")
public final class StructSerializer implements Serializer {
    /**
     * rootType of struct
     */
    private final Type rootType;

    /**
     * byteBuf ready for serialization/deserialization
     */
    private final ByteBuf byteBuf;

    /**
     * an object ready for serialization/deserialization
     */
    private final Object struct;

    public Type getType() {
        return rootType;
    }

    @Override
    public ByteBuf getByteBuf() {
        return byteBuf;
    }

    StructSerializer(Type rootType, ByteBuf byteBuf, Object struct) {
        this.rootType = rootType;
        this.byteBuf  = byteBuf;
        this.struct   = struct;
    }

    public static <T> T toStruct(Type rootType, ByteBuf byteBuf) {
        if (rootType instanceof Class<?>)                            return new StructSerializer(rootType, byteBuf, newStruct(rootType)).doDeserialize();
        else
        if (rootType instanceof ParameterizedType parameterizedType) return new StructSerializer(rootType, byteBuf, newStruct(parameterizedType.getRawType())).doDeserialize();
        else
        if (rootType instanceof TypeRefer<?> typeRefer)              return toStruct(typeRefer.getTypeValue(), byteBuf);
        else
        if (rootType instanceof TypeReference<?> typeReference)      return toStruct(typeReference.getType(), byteBuf);
        else                                                         throw new TypeJudgmentException(rootType);
    }

    public static <T> T toStruct(Type type, byte[] bytes) {
        return toStruct(type, Unpooled.wrappedBuffer(bytes));
    }

    public static <T> T toStruct(Type rootType, ByteBuffer byteBuffer) {
        return toStruct(rootType, Unpooled.wrappedBuffer(byteBuffer));
    }

    public static <T> T toStruct(Type rootType, InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int b = is.read(); b >= 0; b = is.read()) baos.write(b);
        is.close();
        return toStruct(rootType, baos.toByteArray());
    }

    //*************************************       read write splitter       ********************************************//

    public static <T> ByteBuf toByteBuf(T struct) {
        return toByteBuf(struct.getClass(), struct);
    }

    public static <T> ByteBuf toByteBuf(Type rootType, T struct) {
        Throws.ifNull(struct, () -> "struct can not be null when write, root type: [" + rootType + "]");

        if (rootType instanceof Class<?> || rootType instanceof ParameterizedType) return new StructSerializer(rootType, buffer(), struct).doSerialize();
        else
        if (rootType instanceof TypeRefer<?> typeRefer)                            return toByteBuf(typeRefer.getTypeValue(), struct);
        else
        if (rootType instanceof TypeReference<?> typeReference)                    return toByteBuf(typeReference.getType(), struct);
        else throw new TypeJudgmentException(rootType);
    }

    public static <T> byte[] toBytes(T struct) {
        return toBytes(struct.getClass(), struct);
    }

    public static <T> byte[] toBytes(Type rootType, T struct) {
        ByteBuf writeBuf = toByteBuf(rootType, struct);
        try {
            byte[] bytes = new byte[writeBuf.readableBytes()];
            writeBuf.readBytes(bytes);
            return bytes;
        } finally {
            ReferenceCountUtil.release(writeBuf);
        }
    }

    public static <T> ByteBuffer toNioBuffer(T struct) {
        return toNioBuffer(struct.getClass(), struct);
    }

    public static <T> ByteBuffer toNioBuffer(Type rootType, T struct) {
        return ByteBuffer.wrap(toBytes(rootType, struct));
    }

    public static <T> void writeStream(T struct, OutputStream outputStream) throws IOException {
        outputStream.write(toBytes(struct.getClass(), struct));
    }

    public static <T> void writeStream(Type rootType, T struct, OutputStream outputStream) throws IOException {
        outputStream.write(toBytes(rootType, struct));
    }

    //*************************************      working code splitter      ******************************************//

    @Override
    public <T> T doDeserialize() {
        StructDefinition structDef = getStructDefinition(getRawType(rootType));

        for (StructField structField : structDef.fields()) {
            Field                 field           = structField.getWrapped();
            StructFieldHandler<?> handler         = structField.getStructFieldHandler();
            Type                  fieldActualType = TypeUtil.getActualType(rootType, field);

            try {
                handler.beforeRead(    this, fieldActualType, structField, structField.getAnnotation());
                Object fieldValue =
                        handler.doRead(this, fieldActualType, structField, structField.getAnnotation());
                handler.afterRead(     this, fieldActualType, structField, structField.getAnnotation());
                structField.getSetter().accept(struct, fieldValue);
            }
            catch (Exception exception) {
                handler.whenReadThrow( this, fieldActualType, structField, structField.getAnnotation(), exception);
                throw new SerializeException("read exception occur, field is [" + field + "]", exception);
            }
        }

        return (T) struct;
    }

    @Override
    public ByteBuf doSerialize() {
        ByteBuf          writing   = this.getByteBuf();
        StructDefinition structDef = getStructDefinition(getRawType(rootType));

        for (StructField structField : structDef.fields()) {
            Field                 field           = structField.getWrapped();
            StructFieldHandler<?> handler         = structField.getStructFieldHandler();
            Object                fieldValue      = structField.getGetter().apply(struct);
            Type                  fieldActualType = TypeUtil.getActualType(rootType, field);

            try {
                handler.beforeWrite(   this, fieldActualType, structField, structField.getAnnotation(), fieldValue, writing);
                handler.doWrite(       this, fieldActualType, structField, structField.getAnnotation(), fieldValue, writing);
                handler.afterWrite(    this, fieldActualType, structField, structField.getAnnotation(), fieldValue, writing);
            }
            catch (Exception exception) {
                handler.whenWriteThrow(this, fieldActualType, structField, structField.getAnnotation(), fieldValue, writing, exception);
                throw new SerializeException("write exception occur, field [" + field + "]", exception);
            }
        }
        return writing;
    }

    <B extends Basic<?>> B readBasic(Type basicType) {
        return newBasic(basicType, this.getByteBuf());
    }

    <S> S readStruct(Type structType) {
        return StructSerializer.toStruct(structType, this.getByteBuf());
    }

    public <T> T[] readArray(Type elementType, int length) {
        if (isBasic(elementType))  return (T[]) readBasicArray(elementType, length);
        else
        if (isStruct(elementType)) return readStructArray(elementType, length);
        else                       throw new TypeJudgmentException();
    }

    public <B extends Basic<?>> B[] readBasicArray(Type elementType, int length) {
        B[] basics = newArray(elementType, length);

        for (int i = 0; i < basics.length; i++) basics[i] = newBasic(elementType, getByteBuf());

        return basics;
    }

    public <S> S[] readStructArray(Type elementType, int length) {
        S[] structs = newArray(elementType, length);
        Type elementActualType = TypeUtil.getActualType(rootType, elementType);

        for (int i = 0; i < structs.length; i++) structs[i] = readStruct(elementActualType);

        return structs;
    }

    public <T> List<T> readList(Type elementType, int length) {
        if (isBasic(elementType))  return (List<T>) readBasicList(elementType, length);
        else
        if (isStruct(elementType)) return readStructList(elementType, length);
        else                       throw new TypeJudgmentException();
    }

    public <B extends Basic<?>> List<B> readBasicList(Type elementType, int length) {
        return CollUtil.newArrayList(readBasicArray(elementType, length));
    }

    public <T> List<T> readStructList(Type elementType, int length) {
        return CollUtil.newArrayList(readStructArray(elementType, length));
    }

    <B extends Basic<?>> void writeBasic(Object basicValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(((B) (basicValue)).getBytes());
    }

    <S> void writeStruct(Type rootType, S structValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(StructSerializer.toByteBuf(rootType, structValue));
    }
    public void writeArray(Object arrayValue, Type componentType, int length, ByteBuf writing) {
        if (isBasic(componentType)) {
            int        basicElementSize = StructHelper.findBasicSize(componentType);
            Basic<?>[] basicArray       = (Basic<?>[]) arrayValue;

            if (basicArray == null) {
                writing.writeBytes(new byte[basicElementSize * length]);
                return;
            }

            writeBasicArray(basicArray, basicElementSize, length, writing);
        }
        else
        if (isStruct(componentType)) {
            writeStructArray(arrayNullDefault(arrayValue, componentType, length), componentType, length, writing);
        }
        else throw new TypeJudgmentException();
    }

    public void writeBasicArray(Basic<?>[] basicArray, int elementBytesSize, int length, ByteBuf writing) {
        for (int i = 0; i < length; i++) {
            if (i < basicArray.length) {
                Basic<?> basic = basicArray[i];
                if (basic == null) writing.writeBytes(new byte[elementBytesSize]);
                else               writing.writeBytes(basicArray[i].getBytes());
            }
            else
                writing.writeBytes(new byte[elementBytesSize]);
        }
    }

    public <T> void writeStructArray(T[] structArray, Type elementType, int length, ByteBuf writing) {
        for (int i = 0; i < length; i++) {
            if (i < structArray.length)
                writing.writeBytes(StructSerializer.toByteBuf(elementType, structNullDefault(structArray[i], elementType)));
            else
                writing.writeBytes(StructSerializer.toByteBuf(newStruct(elementType)));
        }
    }

    public void writeList(List<?> list, Type elementType, int length, ByteBuf writing) {
        if (isBasic(elementType))  writeBasicList(list, findBasicSize(elementType), length, writing);
        else
        if (isStruct(elementType)) writeStructList(list, elementType, length, writing);
        else                       throw new TypeJudgmentException();
    }

    public void writeBasicList(List<?> list, int elementBytesSize, int length, ByteBuf writing) {
        Iterator<?> iterator = list.iterator();
        for (int i = 0; i < length; i++) {
            if (iterator.hasNext()) {
                Basic<?> basic = (Basic<?>) iterator.next();
                if (basic == null) writing.writeBytes(new byte[elementBytesSize]);
                else               writing.writeBytes(basic.getBytes());
            }
            else writing.writeBytes(new byte[elementBytesSize]);
        }
    }

    public void writeStructList(List<?> list, Type elementType, int length, ByteBuf writing) {
        Iterator<?> iterator = list.iterator();
        for (int i = 0; i < length; i++) {
            if (iterator.hasNext())
                writing.writeBytes(StructSerializer.toByteBuf(elementType, structNullDefault(iterator.next(), elementType)));
            else
                writing.writeBytes(StructSerializer.toByteBuf(elementType, newStruct(elementType)));
        }
    }

    public static <T> T[] newArray(Type componentType, int length) {
        if (componentType instanceof Class<?> clazz)
            return (T[]) Array.newInstance(clazz, length);
        if (componentType instanceof ParameterizedType parameterizedType)
            return (T[]) Array.newInstance((Class<?>) parameterizedType.getRawType(), length);
        else
            return (T[]) Array.newInstance(Object.class, length);
    }

    public Type getComponentType(Type type) {
        if (type instanceof Class<?> clazz)                    return clazz.getComponentType();
        if (type instanceof GenericArrayType genericArrayType) return TypeUtil.getActualType(rootType, genericArrayType.getGenericComponentType());
        else                                                   return type;
    }

    public Type getElementType(Type type) {
        if (type instanceof Class<?> clazz)                      return clazz.getComponentType();
        if (type instanceof ParameterizedType parameterizedType) return TypeUtil.getActualType(rootType, parameterizedType.getActualTypeArguments()[0]);
        else                                                     return type;
    }

    public static <T> T basicNullDefault(Object fieldValue, Type fieldActualType) {
        return (T) defaultIfNull(fieldValue, () -> newEmptyBasic(fieldActualType));
    }

    public static <T> T structNullDefault(Object fieldValue, Type fieldActualType) {
        return (T) defaultIfNull(fieldValue, () -> newStruct(fieldActualType));
    }

    public static <T> T[] arrayNullDefault(Object arrayValue, Type componentType, int length) {
        return (T[]) defaultIfNull(arrayValue, () -> newArray(componentType, length));
    }

    public boolean isBasic(Field field) {
        return isBasic(field.getGenericType());
    }

    public boolean isBasic(Class<?> clazz) {
        return Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;
    }

    public boolean isBasic(Type type) {
        if (type instanceof Class<?> clazz)               return isBasic(clazz);
        if (type instanceof TypeVariable<?> typeVariable) return isBasic(TypeUtil.getActualType(rootType, typeVariable));

        return false;
    }

    public boolean isStruct(Class<?> clazz) {
        return STRUCT_DEFINITION_CACHE.containsKey(clazz);
    }

    public boolean isStruct(Field field) {
        return isStruct(field.getGenericType());
    }

    public boolean isStruct(Type type) {
        if (type instanceof Class<?> clazz)                      return isStruct(clazz);
        if (type instanceof ParameterizedType parameterizedType) return isStruct((Class<?>) parameterizedType.getRawType());
        if (type instanceof TypeVariable<?> typeVariable)        return isStruct(TypeUtil.getActualType(rootType, typeVariable));

        return false;
    }

    /**
     * this is a  useful method that sometimes you may read the field value before current field value, if so you can
     * get the serializing struct object by this method
     *
     * @param <T> the type parameter
     * @return the t
     */
    public <T> T earlyStruct() {
        return (T) this.struct;
    }

    //******************************************      public end       ***********************************************//
}
