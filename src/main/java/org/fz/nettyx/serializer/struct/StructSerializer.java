package org.fz.nettyx.serializer.struct;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static io.netty.buffer.Unpooled.buffer;
import static org.fz.nettyx.serializer.struct.PropertyHandler.isReadHandler;
import static org.fz.nettyx.serializer.struct.PropertyHandler.isWriteHandler;
import static org.fz.nettyx.serializer.struct.StructUtils.getStructFields;
import static org.fz.nettyx.serializer.struct.StructUtils.newStruct;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
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
    private final ByteBuf byteBuf;

    /**
     * type of struct
     */
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

    public static <T> T read(ByteBuf byteBuf, T struct, TypeReference<T> typeReference) {
        Throws.ifNull(struct, "struct can not be null");
        return new StructSerializer(byteBuf, struct, typeReference.getType()).toObject();
    }

    public static <T> T read(ByteBuf byteBuf, T struct) {
        Throws.ifNull(struct, "struct can not be null when read");
        return (T) new StructSerializer(byteBuf, struct, struct.getClass()).toObject();
    }

    public static <T> T read(ByteBuf byteBuf, Class<T> clazz, TypeReference<T> typeReference) {
        return read(byteBuf, newStruct(clazz), typeReference);
    }

    public static <T> T read(ByteBuf byteBuf, Class<T> clazz) {
        return read(byteBuf, newStruct(clazz));
    }

    public static <T> T read(byte[] bytes, T struct, TypeReference<T> typeReference) {
        return read(Unpooled.wrappedBuffer(bytes), struct, typeReference);
    }

    public static <T> T read(byte[] bytes, T struct) {
        return read(Unpooled.wrappedBuffer(bytes), struct);
    }

    public static <T> T read(byte[] bytes, Class<T> clazz, TypeReference<T> typeReference) {
        return read(bytes, newStruct(clazz), typeReference);
    }

    public static <T> T read(byte[] bytes, Class<T> clazz) {
        return read(bytes, newStruct(clazz));
    }

    public static <T> T read(ByteBuffer byteBuffer, T struct, TypeReference<T> typeReference) {
        return read(Unpooled.wrappedBuffer(byteBuffer), struct, typeReference);
    }

    public static <T> T read(ByteBuffer byteBuffer, T struct) {
        return read(Unpooled.wrappedBuffer(byteBuffer), struct);
    }

    public static <T> T read(ByteBuffer byteBuffer, Class<T> clazz, TypeReference<T> typeReference) {
        return read(byteBuffer, newStruct(clazz), typeReference);
    }

    public static <T> T read(ByteBuffer byteBuffer, Class<T> clazz) {
        return read(byteBuffer, newStruct(clazz));
    }

    public static <T> T read(InputStream inputStream, Class<T> clazz, TypeReference<T> typeReference) throws IOException {
        return read(inputStream, newStruct(clazz), typeReference);
    }

    public static <T> T read(InputStream inputStream, Class<T> clazz) throws IOException {
        return read(inputStream, newStruct(clazz));
    }

    public static <T> T read(InputStream is, T struct, TypeReference<T> typeReference) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int b = is.read(); b >= 0; b = is.read()) baos.write(b);
        is.close();
        return read(baos.toByteArray(), struct, typeReference);
    }

    public static <T> T read(InputStream is, T struct) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int b = is.read(); b >= 0; b = is.read()) baos.write(b);
        is.close();
        return read(baos.toByteArray(), struct);
    }

    //*************************************      read write splitter      ********************************************//

    public static <T> ByteBuf write(T struct, TypeReference<T> typeReference) {
        return new StructSerializer(buffer(), struct, typeReference).toByteBuf();
    }
    public static <T> ByteBuf write(T struct) {
        Throws.ifNull(struct, "struct can not be null when write");
        return new StructSerializer(buffer(), struct, struct.getClass()).toByteBuf();
    }

    public static <T> byte[] writeBytes(T struct, TypeReference<T> typeReference) {
        ByteBuf writeBuf = StructSerializer.write(struct, typeReference);
        try {
            byte[] bytes = new byte[writeBuf.readableBytes()];
            writeBuf.readBytes(bytes);
            return bytes;
        } finally {
            ReferenceCountUtil.release(writeBuf);
        }
    }

    public static <T> byte[] writeBytes(T struct) {
        ByteBuf writeBuf = StructSerializer.write(struct);
        try {
            byte[] bytes = new byte[writeBuf.readableBytes()];
            writeBuf.readBytes(bytes);
            return bytes;
        } finally {
            ReferenceCountUtil.release(writeBuf);
        }
    }

    public static <T> ByteBuffer writeNioBuffer(T struct, TypeReference<T> typeReference) {
        return ByteBuffer.wrap(StructSerializer.writeBytes(struct, typeReference));
    }

    public static <T> ByteBuffer writeNioBuffer(T struct) {
        return ByteBuffer.wrap(StructSerializer.writeBytes(struct));
    }

    public static <T> void writeStream(T struct, OutputStream outputStream, TypeReference<T> typeReference) throws IOException {
        byte[] writeBuf = StructSerializer.writeBytes(struct, typeReference);
        outputStream.write(writeBytes(writeBuf));
    }

    public static <T> void writeStream(T struct, OutputStream outputStream) throws IOException {
        byte[] writeBuf = StructSerializer.writeBytes(struct);
        outputStream.write(writeBytes(writeBuf));
    }

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
                // some fields may ignore
                if (isIgnore(field)) continue;

                if (useReadHandler(field)) fieldValue = readHandled(field, this);
                else
                if (isBasic(field))        fieldValue = readBasic(field,  this.getByteBuf());
                else
                if (isStruct(field))       fieldValue = readStruct(field, this.getByteBuf());
                else                       throw new TypeJudgmentException(field);
                StructUtils.writeField(struct, field, fieldValue);
            }
            catch (Exception exception) { throw new SerializeException("field read exception, field is[" + field + "]", exception); }
        }
        return (T) struct;
    }

    /**
     * convert Object to ByteBuf
     *
     * @return the byte buf
     */
    ByteBuf toByteBuf() {
        for (Field field : getStructFields(getStructType())) {
            try {
                Object fieldValue = StructUtils.readField(struct, field);
                // some fields may ignore
                if (isIgnore(field)) continue;

                if (useWriteHandler(field)) writeHandled(field, fieldValue, this);
                else
                if (isBasic(field))         writeBasic((Basic<?>) defaultIfNull(fieldValue, () -> StructUtils.newBasic(field, buffer())), this.getByteBuf());
                else
                if (isStruct(field))        writeStruct(defaultIfNull(fieldValue, () -> StructUtils.newStruct(field)), this.getByteBuf());
                else throw new TypeJudgmentException(field);
            } catch (Exception exception) {
                throw new SerializeException("field write exception, field [" + field + "]", exception);
            }
        }
        return getByteBuf();
    }

    /**
     * read buf into basic field
     *
     * @param <B>        the type parameter
     * @param basicField the basic field
     * @param byteBuf    the byte buf
     * @return the b
     */
    public static <B extends Basic<?>> B readBasic(Field basicField, ByteBuf byteBuf) {
        return StructUtils.newBasic(basicField, byteBuf);
    }

    /**
     * read struct into struct field
     *
     * @param <S>         the type parameter
     * @param structField the struct field
     * @param byteBuf     the byte buf
     * @return the s
     */
    public static <S> S readStruct(Field structField, ByteBuf byteBuf) {
        // invoke struct no-arg constructor
        S struct = StructUtils.newStruct(structField);
        return StructSerializer.read(byteBuf, struct);
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
            readHandler.beforeReadThrow(upperSerializer, handleField, handlerAnnotation, readHandlerException);
            throw new HandlerException(handleField, readHandler.getClass(), readHandlerException);
        }
    }

    //*************************************         read write splitter         **************************************//

    /**
     * write basic bytes
     */
    public static <B extends Basic<?>> void writeBasic(B basicValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(basicValue.getBytes());
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
            writeHandler.beforeWriteThrow(upperSerializer, handleField, fieldValue, handlerAnnotation, writing,
                writeHandlerException);
            throw new HandlerException(handleField, writeHandler.getClass(), writeHandlerException);
        }
    }

    /**
     * Is basic boolean.
     *
     * @param <T> the type parameter
     * @param object the object
     * @return the boolean
     */
    public static <T> boolean isBasic(T object) {
        return isBasic(object.getClass());
    }

    /**
     * Is basic boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean isBasic(Field field) {
        return isBasic(field.getType());
    }

    /**
     * Is basic boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    public static boolean isBasic(Class<?> clazz) {
        return Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;
    }

    public static boolean isStruct(Field field) {
        return isStruct(field.getType());
    }

    /**
     * Is struct boolean.
     *
     * @param <T> the type parameter
     * @param object the object
     * @return the boolean
     */
    public static <T> boolean isStruct(T object) {
        return isStruct(object.getClass());
    }

    /**
     * Is struct boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
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
        return AnnotationUtil.hasAnnotation(field, Ignore.class) || StructUtils.isTransient(field);
    }

    /**
     * Is read handleable boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean useReadHandler(AnnotatedElement field) {
        return isReadHandler((PropertyHandler<?>) StructUtils.getHandler(field));
    }

    /**
     * Is write handleable boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean useWriteHandler(AnnotatedElement field) {
        return isWriteHandler((PropertyHandler<?>) StructUtils.getHandler(field));
    }

    /**
     * this is a  useful method that sometimes you may read the field value before current field value, if so
     * you can get the serializing struct object by this method
     * @param <T> the type parameter
     * @return the t
     */
    public <T> T earlyStruct() {
        return (T) this.struct;
    }

    public <T> Class<T> getStructType() {
        return (Class<T>) this.struct.getClass();
    }

    @Override
    public ByteBuf getByteBuf() {
        return this.byteBuf;
    }

    /**
     * Take byte buf byte buf.
     *
     * @param length the take length
     * @return the byte buf
     */
    public ByteBuf readBytes(int length) {
        return getByteBuf().readBytes(length);
    }

    //******************************************      public end       ***********************************************//

}
