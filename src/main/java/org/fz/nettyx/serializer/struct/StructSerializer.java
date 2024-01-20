package org.fz.nettyx.serializer.struct;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static io.netty.buffer.Unpooled.buffer;
import static org.fz.nettyx.serializer.struct.StructUtils.getStructFields;
import static org.fz.nettyx.serializer.struct.StructUtils.newEmptyBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.newStruct;
import static org.fz.nettyx.serializer.struct.StructUtils.useReadHandler;
import static org.fz.nettyx.serializer.struct.StructUtils.useWriteHandler;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.ModifierUtil;
import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.ByteBuffer;
import lombok.Getter;
import org.fz.nettyx.exception.HandlerException;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.struct.PropertyHandler.ReadHandler;
import org.fz.nettyx.serializer.struct.PropertyHandler.WriteHandler;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.annotation.Struct;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.util.Throws;
import org.fz.nettyx.util.TypeRefer;

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
     * byteBuf ready for serialization/deserialization
     */
    @Getter
    private final ByteBuf byteBuf;

    /**
     * type of struct
     */
    @Getter
    private final Type type;

    /**
     * an object ready for serialization/deserialization
     */
    private final Object struct;

    /**
     * Instantiates a new Typed byte buf serializer.
     *
     * @param byteBuf the byte buf
     * @param struct  the struct
     */
    StructSerializer(ByteBuf byteBuf, Object struct, Type type) {
        this.byteBuf = byteBuf;
        this.struct = struct;
        this.type = type;
    }

    public static <T> T read(ByteBuf byteBuf, Type type) {
        if (type instanceof Class<?>)          return new StructSerializer(byteBuf, newStruct((Class<T>) type), type).toObject();
        else
        if (type instanceof ParameterizedType) return new StructSerializer(byteBuf, newStruct((Class<T>) ((ParameterizedType) type).getRawType()), type).toObject();
        else
        if (type instanceof TypeRefer)         return read(byteBuf, ((TypeRefer<T>) type).getType());
        else
        if (type instanceof TypeReference)     return read(byteBuf, ((TypeReference<T>) type).getType());
        else throw new TypeJudgmentException(type);
    }

    public static <T> T read(byte[] bytes, Type type) {
        return read(Unpooled.wrappedBuffer(bytes), type);
    }

    public static <T> T read(ByteBuffer byteBuffer, Type type) {
        return read(Unpooled.wrappedBuffer(byteBuffer), type);
    }

    public static <T> T read(InputStream is, Type type) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int b = is.read(); b >= 0; b = is.read()) {
            baos.write(b);
        }
        is.close();
        return read(baos.toByteArray(), type);
    }

    //*************************************      read write splitter      ********************************************//

    public static <T> ByteBuf write(T struct, Type type) {
        Throws.ifNull(struct, "struct can not be null when write");

        if (type instanceof Class<?>)          return new StructSerializer(buffer(), struct, type).toByteBuf();
        else
        if (type instanceof ParameterizedType) return new StructSerializer(buffer(), struct, type).toByteBuf();
        else
        if (type instanceof TypeRefer)         return write(struct, ((TypeRefer<T>) type).getType());
        else
        if (type instanceof TypeReference)     return write(struct, ((TypeReference<T>) type).getType());
        else throw new TypeJudgmentException(type);
    }

    public static <T> ByteBuf write(T struct) {
        Throws.ifNull(struct, "struct can not be null when write");
        return write(struct, struct.getClass());
    }

    public static <T> byte[] writeBytes(T struct) {
        return writeBytes(struct, struct.getClass());
    }

    public static <T> byte[] writeBytes(T struct, Type type) {
        ByteBuf writeBuf = write(struct, type);
        try {
            byte[] bytes = new byte[writeBuf.readableBytes()];
            writeBuf.readBytes(bytes);
            return bytes;
        } finally {
            ReferenceCountUtil.release(writeBuf);
        }
    }

    public static <T> ByteBuffer writeNioBuffer(T struct) {
        return writeNioBuffer(struct, struct.getClass());
    }

    public static <T> ByteBuffer writeNioBuffer(T struct, Type type) {
        return ByteBuffer.wrap(writeBytes(struct, type));
    }

    public static <T> void writeStream(T struct, OutputStream outputStream) throws IOException {
        outputStream.write(writeBytes(struct));
    }

    public static <T> void writeStream(T struct, OutputStream outputStream, Type type) throws IOException {
        outputStream.write(writeBytes(struct, type));
    }

    //*************************************      working code splitter      ******************************************//

    /**
     * parse ByteBuf to Object
     *
     * @param <T> the type parameter
     * @return the t
     */
    <T> T toObject() {
        for (Field field : getStructFields(getStructType())) {
            try {
                Object fieldValue;
                Class<?> fieldActualType;
                // some fields may ignore
                if (isIgnore(field)) continue;

                if (useReadHandler(field)) {
                    fieldValue = readHandled(field, this);
                } else if (isBasic(fieldActualType = getFieldActualType(field))) {
                    fieldValue = readBasic(fieldActualType, this.getByteBuf());
                } else if (isStruct(fieldActualType)) {
                    fieldValue = readStruct(fieldActualType, this.getByteBuf());
                }
                else throw new TypeJudgmentException(field);
                StructUtils.writeField(struct, field, fieldValue);
            } catch (Exception exception) {
                throw new SerializeException("field read exception, field is [" + field + "]", exception);
            }
        }
        return (T) struct;
    }

    /**
     * convert Object to ByteBuf
     *
     * @return the byte buf
     */
    ByteBuf toByteBuf() {
        ByteBuf writing = this.getByteBuf();
        for (Field field : getStructFields(getStructType())) {
            try {
                Object fieldValue = StructUtils.readField(struct, field);
                Class<?> fieldActualType ;
                // some fields may ignore
                if (isIgnore(field)) continue;

                if (useWriteHandler(field)) {
                    writeHandled(field, fieldValue, this);
                } else if (isBasic(fieldActualType = getFieldActualType(field))) {
                    writeBasic(defaultIfNull(fieldValue, () -> newEmptyBasic(fieldActualType)), writing);
                } else if (isStruct(fieldActualType)) {
                    writeStruct(defaultIfNull(fieldValue, () -> newStruct(fieldActualType)), writing);
                }
                else throw new TypeJudgmentException(field);
            } catch (Exception exception) {
                throw new SerializeException("field write exception, field [" + field + "]", exception);
            }
        }
        return writing;
    }

    public static <B extends Basic<?>> B readBasic(Field basicField, ByteBuf byteBuf) {
        return StructUtils.newBasic(basicField, byteBuf);
    }

    public static <B extends Basic<?>> B readBasic(Type basicType, ByteBuf byteBuf) {
        return StructUtils.newBasic((Class<?>) basicType, byteBuf);
    }

    public static <S> S readStruct(Field structField, ByteBuf byteBuf) {
        return StructSerializer.read(byteBuf, structField.getType());
    }

    public static <S> S readStruct(Type type, ByteBuf byteBuf) {
        return StructSerializer.read(byteBuf, type);
    }

    public static <A extends Annotation> Object readHandled(Field handleField, StructSerializer upperSerializer) {
        ReadHandler<A> readHandler = StructUtils.getHandler(handleField);
        A handlerAnnotation = StructUtils.findHandlerAnnotation(handleField);
        try {
            readHandler.preReadHandle(upperSerializer, handleField, handlerAnnotation);
            Object handledValue = readHandler.doRead(upperSerializer, handleField, handlerAnnotation);
            readHandler.postReadHandle(upperSerializer, handleField, handlerAnnotation);
            return handledValue;
        } catch (Exception readHandlerException) {
            readHandler.afterReadThrow(upperSerializer, handleField, handlerAnnotation, readHandlerException);
            throw new HandlerException(handleField, readHandler.getClass(), readHandlerException);
        }
    }

    //*************************************         read write splitter         **************************************//

    /**
     * write basic bytes
     */
    public static <B extends Basic<?>> void writeBasic(Object basicValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(((B) (basicValue)).getBytes());
    }

    /**
     * write struct bytes
     *
     * @param structValue the structValue
     */
    public static <S> void writeStruct(S structValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(StructSerializer.write(structValue));
    }

    /**
     * write using handler
     *
     * @param handleField the handled field
     * @param upperSerializer the upper serializer
     */
    public static <A extends Annotation> void writeHandled(Field handleField, Object fieldValue,
        StructSerializer upperSerializer) {
        WriteHandler<A> writeHandler = StructUtils.getHandler(handleField);
        A handlerAnnotation = StructUtils.findHandlerAnnotation(handleField);
        ByteBuf writing = upperSerializer.getByteBuf();
        try {
            writeHandler.preWriteHandle(upperSerializer, handleField, fieldValue, handlerAnnotation, writing);
            writeHandler.doWrite(upperSerializer, handleField, fieldValue, handlerAnnotation, writing);
            writeHandler.postWriteHandle(upperSerializer, handleField, fieldValue, handlerAnnotation, writing);
        } catch (Exception writeHandlerException) {
            writeHandler.afterWriteThrow(upperSerializer, handleField, fieldValue, handlerAnnotation, writing,
                writeHandlerException);
            throw new HandlerException(handleField, writeHandler.getClass(), writeHandlerException);
        }
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

    public <T> Class<T> getStructType() {
        if (this.type instanceof Class<?>) {
            return (Class<T>) this.type;
        } else if (this.type instanceof ParameterizedType) {
            return (Class<T>) ((ParameterizedType) this.type).getRawType();
        }
        throw new TypeJudgmentException(this.type);
    }

    public <T> Class<T> getFieldActualType(Field field) {
        Type fieldType = TypeUtil.getType(field);
        // If it's a Class, it means that no generics are specified
        if (fieldType instanceof Class<?>) {
            return (Class<T>) fieldType;
        }
        if (fieldType instanceof TypeVariable) {
            return (Class<T>) TypeUtil.getActualType(this.type, fieldType);
        }
        else
        if (this.type instanceof ParameterizedType) {
            Type actualType = TypeUtil.getActualType(this.type, field);
            Type[] actualTypeArguments = ((ParameterizedType) actualType).getActualTypeArguments();
            if (actualTypeArguments.length == 0) return (Class<T>) Object.class;

            return (Class<T>) actualTypeArguments[0];
        }
        else return (Class<T>) Object.class;
    }

    public <T> Class<T> getArrayFieldActualType(Field field) {
        if (this.type instanceof ParameterizedType) {
            GenericArrayType actualType = (GenericArrayType) TypeUtil.getActualType(this.type, field);
            return (Class<T>) TypeUtil.getActualType(this.type, actualType.getGenericComponentType());
        }
        return (Class<T>) Object.class;
    }

    //******************************************      public end       ***********************************************//

    public static boolean isTransient(Field field) {
        return ModifierUtil.hasModifier(field, ModifierUtil.ModifierType.TRANSIENT);
    }

    public static <T> boolean isNotBasic(T object) {
        return isNotBasic(object.getClass());
    }

    public static boolean isNotBasic(Field field) {
        return isNotBasic(field.getType());
    }

    public static boolean isNotBasic(Class<?> clazz) {
        return !isBasic(clazz);
    }

    public static <T> boolean isBasic(T object) {
        return isBasic(object.getClass());
    }

    public static boolean isBasic(Field field) {
        return isBasic(field.getType());
    }

    public static boolean isBasic(Class<?> clazz) {
        return Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;
    }

    public static boolean isNotStruct(Field field) {
        return isNotStruct(field.getType());
    }

    public static <T> boolean isNotStruct(T object) {
        return isNotStruct(object.getClass());
    }

    public static boolean isNotStruct(Class<?> clazz) {
        return !isStruct(clazz);
    }

    public static boolean isStruct(Field field) {
        return isStruct(field.getType());
    }

    public static <T> boolean isStruct(T object) {
        return isStruct(object.getClass());
    }

    public static boolean isStruct(Class<?> clazz) {
        return AnnotationUtil.hasAnnotation(clazz, Struct.class);
    }

    /**
     * Is ignore boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean isIgnore(Field field) {
        return AnnotationUtil.hasAnnotation(field, Ignore.class) || isTransient(field);
    }

}
