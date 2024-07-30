package org.fz.nettyx.serializer.struct;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.SerializeHandlerException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.struct.StructPropHandler.ReadHandler;
import org.fz.nettyx.serializer.struct.StructPropHandler.WriteHandler;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.util.Throws;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static io.netty.buffer.Unpooled.buffer;
import static org.fz.nettyx.serializer.struct.StructUtils.*;
import static org.fz.nettyx.serializer.struct.annotation.Struct.STRUCT_FIELD_CACHE;

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
    @Getter
    private final Type rootType;

    /**
     * byteBuf ready for serialization/deserialization
     */
    @Getter
    private final ByteBuf byteBuf;

    /**
     * an object ready for serialization/deserialization
     */
    private final Object struct;

    StructSerializer(Type rootType, ByteBuf byteBuf, Object struct) {
        this.rootType = rootType;
        this.byteBuf  = byteBuf;
        this.struct   = struct;
    }

    public static <T> T read(Type rootType, ByteBuf byteBuf) {
        if (rootType instanceof Class<?>)          return new StructSerializer(rootType, byteBuf, newStruct(rootType)).parseStruct();
        else
        if (rootType instanceof ParameterizedType) return new StructSerializer(rootType, byteBuf, newStruct(((ParameterizedType) rootType).getRawType())).parseStruct();
        else
        if (rootType instanceof TypeRefer)         return read(((TypeRefer<T>) rootType).getTypeValue(), byteBuf);
        else
        if (rootType instanceof TypeReference)     return read(((TypeReference<T>) rootType).getType(), byteBuf);
        else                                       throw new TypeJudgmentException(rootType);
    }

    public static <T> T read(Type type, byte[] bytes) {
        return read(type, Unpooled.wrappedBuffer(bytes));
    }

    public static <T> T read(Type rootType, ByteBuffer byteBuffer) {
        return read(rootType, Unpooled.wrappedBuffer(byteBuffer));
    }

    public static <T> T read(Type rootType, InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int b = is.read(); b >= 0; b = is.read()) baos.write(b);
        is.close();
        return read(rootType, baos.toByteArray());
    }

    //*************************************      read write splitter      ********************************************//

    public static <T> ByteBuf write(T struct) {
        return write(struct.getClass(), struct);
    }

    public static <T> ByteBuf write(Type rootType, T struct) {
        Throws.ifNull(struct, "struct can not be null when write");

        if (rootType instanceof Class<?> || rootType instanceof ParameterizedType) return new StructSerializer(rootType, buffer(), struct).toByteBuf();
        else
        if (rootType instanceof TypeRefer)                                         return write(((TypeRefer<T>) rootType).getTypeValue(), struct);
        else
        if (rootType instanceof TypeReference)                                     return write(((TypeReference<T>) rootType).getType(), struct);
        else throw new TypeJudgmentException(rootType);
    }

    public static <T> byte[] writeBytes(T struct) {
        return writeBytes(struct.getClass(), struct);
    }

    public static <T> byte[] writeBytes(Type rootType, T struct) {
        ByteBuf writeBuf = write(rootType, struct);
        try {
            byte[] bytes = new byte[writeBuf.readableBytes()];
            writeBuf.readBytes(bytes);
            return bytes;
        } finally {
            ReferenceCountUtil.release(writeBuf);
        }
    }

    public static <T> ByteBuffer writeNioBuffer(T struct) {
        return writeNioBuffer(struct.getClass(), struct);
    }

    public static <T> ByteBuffer writeNioBuffer(Type rootType, T struct) {
        return ByteBuffer.wrap(writeBytes(rootType, struct));
    }

    public static <T> void writeStream(T struct, OutputStream outputStream) throws IOException {
        outputStream.write(writeBytes(struct.getClass(), struct));
    }

    public static <T> void writeStream(Type rootType, T struct, OutputStream outputStream) throws IOException {
        outputStream.write(writeBytes(rootType, struct));
    }

    //*************************************      working code splitter      ******************************************//

    <T> T parseStruct() {
        for (Field field : getStructFields(getRawType(rootType))) {
            try {
                Object fieldValue;
                Type   fieldActualType = TypeUtil.getActualType(rootType, field);
                if (useReadHandler(field)) fieldValue = readHandled(field, fieldActualType, this);
                else
                if (isBasic(field)) fieldValue = readBasic(fieldActualType);
                else
                if (isStruct(field)) fieldValue = readStruct(fieldActualType);

                else throw new TypeJudgmentException(field);

                StructUtils.writeField(struct, field, fieldValue);
            } catch (Throwable exception) {
                throw new SerializeException("read exception occur, field is [" + field + "]", exception);
            }
        }
        return (T) struct;
    }

    ByteBuf toByteBuf() {
        ByteBuf writing = this.getByteBuf();
        for (Field field : getStructFields(getRawType(rootType))) {
            try {
                Object fieldValue      = StructUtils.readField(struct, field);
                Type   fieldActualType = TypeUtil.getActualType(rootType, field);
                if (useWriteHandler(field)) writeHandled(field, fieldActualType, fieldValue, this);
                else
                if (isBasic(field)) writeBasic(defaultIfNull(fieldValue, () -> newEmptyBasic(fieldActualType)), writing);
                else
                if (isStruct(field)) writeStruct(fieldActualType, defaultIfNull(fieldValue, () -> newStruct(fieldActualType)), writing);

                else throw new TypeJudgmentException(field);
            } catch (Exception exception) {
                throw new SerializeException("write exception occur, field [" + field + "]", exception);
            }
        }
        return writing;
    }

    public <T> Class<T> getRawType(Type type) {
        if (type instanceof Class<?>) return (Class<T>) type;
        else if (type instanceof ParameterizedType) return (Class<T>) ((ParameterizedType) type).getRawType();

        throw new TypeJudgmentException(type);
    }

    <B extends Basic<?>> B readBasic(Type basicType) {
        return StructUtils.newBasic(basicType, this.getByteBuf());
    }

    <S> S readStruct(Type structType) {
        return StructSerializer.read(structType, this.getByteBuf());
    }

    public <T> T[] readArray(Type elementType, int length) {
        if (isBasic(elementType)) return (T[]) readBasicArray(elementType, length);
        else
        if (isStruct(elementType)) return readStructArray(elementType, length);
        else                       throw new TypeJudgmentException();
    }

    public <T> List<T> readList(Type elementType, int length, List<?> coll) {
        if (isBasic(elementType))  return (List<T>) readBasicList(elementType, length, coll);
        else
        if (isStruct(elementType)) return readStructList(elementType, length, coll);
        else                       throw new TypeJudgmentException();
    }

    public <B extends Basic<?>> B[] readBasicArray(Type elementType, int length) {
        B[] basics = newArray(elementType, length);

        for (int i = 0; i < basics.length; i++) basics[i] = newBasic(elementType, getByteBuf());

        return basics;
    }

    public <B extends Basic<?>> List<B> readBasicList(Type elementType, int length, List<?> coll) {
        return (List<B>) CollUtil.addAll(coll, readBasicArray(elementType, length));
    }

    public <S> S[] readStructArray(Type elementType, int length) {
        S[] structs = newArray(elementType, length);
        Type elementActualType = TypeUtil.getActualType(rootType, elementType);

        for (int i = 0; i < structs.length; i++) structs[i] = readStruct(elementActualType);

        return structs;
    }

    public <T> List<T> readStructList(Type elementType, int length, List<?> coll) {
        return (List<T>) CollUtil.addAll(coll, readStructArray(elementType, length));
    }

    <A extends Annotation> Object readHandled(Field handleField, Type fieldActualType, StructSerializer upperSerializer) {
        ReadHandler<A> readHandler       = StructUtils.getPropHandler(handleField);
        A              handlerAnnotation = StructUtils.findPropHandlerAnnotation(handleField);

        try {
            readHandler.preReadHandle(upperSerializer, handleField, handlerAnnotation);
            Object handledValue = readHandler.doRead(upperSerializer, fieldActualType, handleField, handlerAnnotation);
            readHandler.postReadHandle(upperSerializer, handleField, handlerAnnotation);
            return handledValue;
        } catch (Exception readHandlerException) {
            readHandler.afterReadThrow(upperSerializer, handleField, handlerAnnotation, readHandlerException);
            throw new SerializeHandlerException(handleField, readHandler.getClass(), readHandlerException);
        }
    }

    <B extends Basic<?>> void writeBasic(Object basicValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(((B) (basicValue)).getBytes());
    }

    <S> void writeStruct(Type rootType, S structValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(StructSerializer.write(rootType, structValue));
    }

    <A extends Annotation> void writeHandled(Field handleField, Type fieldActualType, Object fieldValue, StructSerializer upperSerializer) {
        WriteHandler<A> writeHandler      = StructUtils.getPropHandler(handleField);
        A               handlerAnnotation = StructUtils.findPropHandlerAnnotation(handleField);
        ByteBuf         writing           = upperSerializer.getByteBuf();
        try {
            writeHandler.preWriteHandle(upperSerializer, handleField, fieldValue, handlerAnnotation, writing);
            writeHandler.doWrite(upperSerializer, fieldActualType, handleField, fieldValue, handlerAnnotation, writing);
            writeHandler.postWriteHandle(upperSerializer, handleField, fieldValue, handlerAnnotation, writing);
        } catch (Exception writeHandlerException) {
            writeHandler.afterWriteThrow(upperSerializer, handleField, fieldValue, handlerAnnotation, writing, writeHandlerException);
            throw new SerializeHandlerException(handleField, writeHandler.getClass(), writeHandlerException);
        }
    }

    public Type getComponentType(Type type) {
        if (type instanceof Class)            return ((Class<?>) type).getComponentType();
        if (type instanceof GenericArrayType) return TypeUtil.getActualType(rootType, ((GenericArrayType) type).getGenericComponentType());
        else return type;
    }

    public Type getElementType(Type type) {
        if (type instanceof Class) return ((Class<?>) type).getComponentType();
        if (type instanceof ParameterizedType)
            return TypeUtil.getActualType(rootType, ((ParameterizedType) type).getActualTypeArguments()[0]);
        else return type;
    }

    public void writeArray(Object arrayValue, Type elementType, int length, ByteBuf writing) {
        if (isBasic(elementType)) {
            int        basicElementSize = StructUtils.findBasicSize(elementType);
            Basic<?>[] basicArray       = (Basic<?>[]) arrayValue;

            if (basicArray == null) {
                writing.writeBytes(new byte[basicElementSize * length]);
                return;
            }

            writeBasicArray(basicArray, basicElementSize, length, writing);
        }
        else
        if (isStruct(elementType)) {
            Object[] structArray = defaultIfNull((Object[]) arrayValue, () -> newArray(elementType, length));
            writeStructArray(defaultIfNull(structArray, () -> newArray(elementType, length)), elementType, length, writing);
        }
        else throw new TypeJudgmentException();
    }

    public void writeList(List<?> list, Type elementType, int length, ByteBuf writing) {
        if (isBasic(elementType))  writeBasicList(list, findBasicSize(elementType), length, writing);
        else
        if (isStruct(elementType)) writeStructList(list, elementType, length, writing);
        else                       throw new TypeJudgmentException();
    }

    public <T> T[] newArray(Type componentType, int length) {
        if (componentType instanceof Class)
            return (T[]) Array.newInstance((Class<?>) componentType, length);
        if (componentType instanceof ParameterizedType)
            return (T[]) Array.newInstance((Class<?>) ((ParameterizedType) componentType).getRawType(), length);
        else
            return (T[]) Array.newInstance(Object.class, length);
    }

    public void writeBasicArray(Basic<?>[] basicArray, int elementBytesSize, int length, ByteBuf writing) {
        for (int i = 0; i < length; i++) {
            if (i < basicArray.length) {
                Basic<?> basic = basicArray[i];
                if (basic == null) writing.writeBytes(new byte[elementBytesSize]);
                else               writing.writeBytes(basicArray[i].getBytes());
            }
            else writing.writeBytes(new byte[elementBytesSize]);
        }
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

    public <T> void writeStructArray(T[] structArray, Type elementType, int length, ByteBuf writing) {
        for (int i = 0; i < length; i++) {
            if (i < structArray.length) {
                T object = structArray[i];
                ByteBuf write = StructSerializer.write(elementType, defaultIfNull(object, () -> newStruct(elementType)));
                writing.writeBytes(write);
            }
            else                        writing.writeBytes(StructSerializer.write(newStruct(elementType)));
        }
    }

    public void writeStructList(List<?> list, Type elementType, int length, ByteBuf writing) {
        Iterator<?> iterator = list.iterator();
        for (int i = 0; i < length; i++) {
            if (iterator.hasNext()) writing.writeBytes(StructSerializer.write(elementType, defaultIfNull(iterator.next(), () -> newStruct(elementType))));
            else writing.writeBytes(StructSerializer.write(elementType, newStruct(elementType)));
        }
    }

    public boolean isBasic(Field field) {
        return isBasic(TypeUtil.getType(field));
    }

    public boolean isBasic(Class<?> clazz) {
        return Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;
    }

    public boolean isBasic(Type type) {
        if (type instanceof Class)        return isBasic((Class<?>) type);
        if (type instanceof TypeVariable) return isBasic(TypeUtil.getActualType(rootType, type));

        return false;
    }

    public boolean isStruct(Class<?> clazz) {
        return STRUCT_FIELD_CACHE.containsKey(clazz);
    }

    public boolean isStruct(Field field) {
        return isStruct(TypeUtil.getType(field));
    }

    public boolean isStruct(Type type) {
        if (type instanceof Class)             return isStruct((Class<?>) type);
        if (type instanceof ParameterizedType) return isStruct((Class<?>) ((ParameterizedType) type).getRawType());
        if (type instanceof TypeVariable)      return isStruct(TypeUtil.getActualType(rootType, type));

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
