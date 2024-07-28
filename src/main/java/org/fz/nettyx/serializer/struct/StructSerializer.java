package org.fz.nettyx.serializer.struct;

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
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static io.netty.buffer.Unpooled.buffer;
import static org.fz.nettyx.serializer.struct.StructUtils.*;
import static org.fz.nettyx.serializer.struct.TypeRefer.getRawType;

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
        if (rootType instanceof TypeRefer)         return read(((TypeRefer<T>) rootType).getType(), byteBuf);
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
        for (int b = is.read(); b >= 0; b = is.read()) {
            baos.write(b);
        }
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
        if (rootType instanceof TypeRefer)                                         return write(((TypeRefer<T>) rootType).getType(), struct);
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

    /**
     * parse ByteBuf to Object
     *
     * @param <T> the type parameter
     * @return the t
     */
    <T> T parseStruct() {
        for (Field field : getStructFields(getRawType(rootType))) {
            try {
                Object fieldValue;
                if (useReadHandler(field))     fieldValue = readHandled(field, this);
                else
                if (isBasic(rootType, field))  fieldValue = readBasic(rootType, field, this.getByteBuf());
                else
                if (isStruct(rootType, field)) fieldValue = readStruct(rootType, field, this.getByteBuf());
                else                           throw new TypeJudgmentException(field);

                StructUtils.writeField(struct, field, fieldValue);
            } catch (Throwable exception) {
                throw new SerializeException("read exception occur, field is [" + field + "]", exception);
            }
        }
        return (T) struct;
    }

    /**
     * read Object to ByteBuf
     *
     * @return the byte buf
     */
    ByteBuf toByteBuf() {
        ByteBuf writing = this.getByteBuf();
        for (Field field : getStructFields(getRawType(rootType))) {
            try {
                Object fieldValue = StructUtils.readField(struct, field);

                if (useWriteHandler(field)) writeHandled(field, fieldValue, this);
                else
                if (isBasic(rootType, field)) {
                    Class<?> basicType = (Class<?>) TypeUtil.getActualType(rootType, field);
                    writeBasic(defaultIfNull(fieldValue, () -> newEmptyBasic(basicType)), writing);
                }
                else
                if (isStruct(rootType, field)) {
                    Type structType = TypeUtil.getActualType(rootType, field);
                    writeStruct(structType, defaultIfNull(fieldValue, () -> newStruct(structType)), writing);
                }
                else throw new TypeJudgmentException(field);
            } catch (Throwable exception) {
                throw new SerializeException("write exception occur, field [" + field + "]", exception);
            }
        }
        return writing;
    }

    public static <B extends Basic<?>> B readBasic(Type rootType, Field basicField, ByteBuf byteBuf) {
        Type basicType = TypeUtil.getActualType(rootType, basicField);
        return StructUtils.newBasic((Class<?>) basicType, byteBuf);
    }

    public static <S> S readStruct(Type rootType, Field structField, ByteBuf byteBuf) {
        return StructSerializer.read(TypeUtil.getActualType(rootType, structField), byteBuf);
    }

    public static <A extends Annotation> Object readHandled(Field handleField, StructSerializer upperSerializer) {
        ReadHandler<A> readHandler       = StructUtils.getPropHandler(handleField);
        A              handlerAnnotation = StructUtils.findPropHandlerAnnotation(handleField);
        try {
            readHandler.preReadHandle(upperSerializer, handleField, handlerAnnotation);
            Object handledValue = readHandler.doRead(upperSerializer, handleField, handlerAnnotation);
            readHandler.postReadHandle(upperSerializer, handleField, handlerAnnotation);
            return handledValue;
        } catch (Exception readHandlerException) {
            readHandler.afterReadThrow(upperSerializer, handleField, handlerAnnotation, readHandlerException);
            throw new SerializeHandlerException(handleField, readHandler.getClass(), readHandlerException);
        }
    }

    //*************************************         read write splitter         **************************************//

    public static <B extends Basic<?>> void writeBasic(Object basicValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(((B) (basicValue)).getBytes());
    }

    public static <S> void writeStruct(Type rootType, S structValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(StructSerializer.write(rootType, structValue));
    }

    public static <A extends Annotation> void writeHandled(Field handleField, Object fieldValue, StructSerializer upperSerializer) {
        WriteHandler<A> writeHandler      = StructUtils.getPropHandler(handleField);
        A               handlerAnnotation = StructUtils.findPropHandlerAnnotation(handleField);
        ByteBuf         writing           = upperSerializer.getByteBuf();
        try {
            writeHandler.preWriteHandle(upperSerializer, handleField, fieldValue, handlerAnnotation, writing);
            writeHandler.doWrite(upperSerializer, handleField, fieldValue, handlerAnnotation, writing);
            writeHandler.postWriteHandle(upperSerializer, handleField, fieldValue, handlerAnnotation, writing);
        } catch (Exception writeHandlerException) {
            writeHandler.afterWriteThrow(upperSerializer, handleField, fieldValue, handlerAnnotation, writing,
                                         writeHandlerException);
            throw new SerializeHandlerException(handleField, writeHandler.getClass(), writeHandlerException);
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

    //******************************************      public end       ***********************************************//
}
