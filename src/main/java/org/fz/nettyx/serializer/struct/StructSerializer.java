package org.fz.nettyx.serializer.struct;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static io.netty.buffer.Unpooled.buffer;
import static org.fz.nettyx.serializer.struct.StructUtils.getStructFields;
import static org.fz.nettyx.serializer.struct.StructUtils.newEmptyBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.newStruct;
import static org.fz.nettyx.serializer.struct.StructUtils.useReadHandler;
import static org.fz.nettyx.serializer.struct.StructUtils.useWriteHandler;
import static org.fz.nettyx.serializer.struct.TypeRefer.getRawType;

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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.ByteBuffer;
import lombok.Getter;
import org.fz.nettyx.exception.HandlerException;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.struct.StructFieldHandler.ReadHandler;
import org.fz.nettyx.serializer.struct.StructFieldHandler.WriteHandler;
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
    @Getter
    private final ByteBuf byteBuf;

    /**
     * rootType of struct
     */
    @Getter
    private final Type rootType;

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
    StructSerializer(ByteBuf byteBuf, Object struct, Type rootType) {
        this.byteBuf  = byteBuf;
        this.struct   = struct;
        this.rootType = rootType;
    }

    public static <T> T read(ByteBuf byteBuf, Type rootType) {
        if (rootType instanceof Class<?>) {
            return new StructSerializer(byteBuf, newStruct(rootType), rootType).parseStruct();
        }
        else if (rootType instanceof ParameterizedType) {
            return new StructSerializer(byteBuf, newStruct(((ParameterizedType) rootType).getRawType()),
                                        rootType).parseStruct();
        }
        else if (rootType instanceof TypeRefer) { return read(byteBuf, ((TypeRefer<T>) rootType).getType()); }
        else if (rootType instanceof TypeReference) { return read(byteBuf, ((TypeReference<T>) rootType).getType()); }
        else { throw new TypeJudgmentException(rootType); }
    }

    public static <T> T read(byte[] bytes, Type type) {
        return read(Unpooled.wrappedBuffer(bytes), type);
    }

    public static <T> T read(ByteBuffer byteBuffer, Type rootType) {
        return read(Unpooled.wrappedBuffer(byteBuffer), rootType);
    }

    public static <T> T read(InputStream is, Type rootType) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int b = is.read(); b >= 0; b = is.read()) {
            baos.write(b);
        }
        is.close();
        return read(baos.toByteArray(), rootType);
    }

    //*************************************      read write splitter      ********************************************//

    public static <T> ByteBuf write(T struct) {
        return write(struct, struct.getClass());
    }

    public static <T> ByteBuf write(T struct, Type rootType) {
        Throws.ifNull(struct, "struct can not be null when write");

        if (rootType instanceof Class<?> || rootType instanceof ParameterizedType) {
            return new StructSerializer(buffer(), struct, rootType).toByteBuf();
        }
        else if (rootType instanceof TypeRefer) { return write(struct, ((TypeRefer<T>) rootType).getType()); }
        else if (rootType instanceof TypeReference) { return write(struct, ((TypeReference<T>) rootType).getType()); }
        else { throw new TypeJudgmentException(rootType); }
    }

    public static <T> byte[] writeBytes(T struct) {
        return writeBytes(struct, struct.getClass());
    }

    public static <T> byte[] writeBytes(T struct, Type rootType) {
        ByteBuf writeBuf = write(struct, rootType);
        try {
            byte[] bytes = new byte[writeBuf.readableBytes()];
            writeBuf.readBytes(bytes);
            return bytes;
        }
        finally {
            ReferenceCountUtil.release(writeBuf);
        }
    }

    public static <T> ByteBuffer writeNioBuffer(T struct) {
        return writeNioBuffer(struct, struct.getClass());
    }

    public static <T> ByteBuffer writeNioBuffer(T struct, Type rootType) {
        return ByteBuffer.wrap(writeBytes(struct, rootType));
    }

    public static <T> void writeStream(T struct, OutputStream outputStream) throws IOException {
        outputStream.write(writeBytes(struct, struct.getClass()));
    }

    public static <T> void writeStream(T struct, OutputStream outputStream, Type rootType) throws IOException {
        outputStream.write(writeBytes(struct, rootType));
    }

    //*************************************      working code splitter      ******************************************//

    /**
     * parse ByteBuf to Object
     *
     * @param <T> the type parameter
     *
     * @return the t
     */
    <T> T parseStruct() {
        for (Field field : getStructFields(getRawType(rootType))) {
            try {
                Object fieldValue;
                // some fields may ignore
                if (isIgnore(field)) { continue; }

                if (useReadHandler(field)) {
                    fieldValue = readHandled(field, this);
                }
                else if (isBasic(rootType, field)) {
                    fieldValue = readBasic(rootType, field, this.getByteBuf());
                }
                else if (isStruct(rootType, field)) {
                    fieldValue = readStruct(rootType, field, this.getByteBuf());
                }
                else { throw new TypeJudgmentException(field); }

                StructUtils.writeField(struct, field, fieldValue);
            }
            catch (Exception exception) {
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

                // some fields may ignore
                if (isIgnore(field)) { continue; }

                if (useWriteHandler(field)) {
                    writeHandled(field, fieldValue, this);
                }
                else if (isBasic(rootType, field)) {
                    writeBasic(defaultIfNull(fieldValue,
                                             () -> newEmptyBasic(
                                                 (Class<?>) TypeUtil.getActualType(rootType, field))), writing);
                }
                else if (isStruct(rootType, field)) {
                    writeStruct(rootType,
                                defaultIfNull(fieldValue, () -> newStruct(TypeUtil.getActualType(rootType, field))),
                                writing);
                }
                else { throw new TypeJudgmentException(field); }
            }
            catch (Exception exception) {
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
        return StructSerializer.read(byteBuf, TypeUtil.getActualType(rootType, structField));
    }

    public static <A extends Annotation> Object readHandled(Field handleField, StructSerializer upperSerializer) {
        ReadHandler<A> readHandler       = StructUtils.getHandler(handleField);
        A              handlerAnnotation = StructUtils.findHandlerAnnotation(handleField);
        try {
            readHandler.preReadHandle(upperSerializer, handleField, handlerAnnotation);
            Object handledValue = readHandler.doRead(upperSerializer, handleField, handlerAnnotation);
            readHandler.postReadHandle(upperSerializer, handleField, handlerAnnotation);
            return handledValue;
        }
        catch (Exception readHandlerException) {
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

    public static <S> void writeStruct(Type rootType, S structValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(StructSerializer.write(structValue, rootType));
    }

    public static <A extends Annotation> void writeHandled(Field handleField, Object fieldValue,
                                                           StructSerializer upperSerializer) {
        WriteHandler<A> writeHandler      = StructUtils.getHandler(handleField);
        A               handlerAnnotation = StructUtils.findHandlerAnnotation(handleField);
        ByteBuf         writing           = upperSerializer.getByteBuf();
        try {
            writeHandler.preWriteHandle(upperSerializer, handleField, fieldValue, handlerAnnotation, writing);
            writeHandler.doWrite(upperSerializer, handleField, fieldValue, handlerAnnotation, writing);
            writeHandler.postWriteHandle(upperSerializer, handleField, fieldValue, handlerAnnotation, writing);
        }
        catch (Exception writeHandlerException) {
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
     *
     * @return the t
     */
    public <T> T earlyStruct() {
        return (T) this.struct;
    }

    //******************************************      public end       ***********************************************//

    public static boolean isTransient(Field field) {
        return ModifierUtil.hasModifier(field, ModifierUtil.ModifierType.TRANSIENT);
    }

    public static boolean isNotBasic(Field field) {
        return isNotBasic(field.getType());
    }

    public static boolean isNotBasic(Class<?> clazz) {
        return !isBasic(clazz);
    }

    public static boolean isBasic(Class<?> clazz) {
        return Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;
    }

    public static boolean isBasic(Type root, Field field) {
        return isBasic(root, TypeUtil.getType(field));
    }

    public static boolean isBasic(Type root, Type type) {
        if (type instanceof Class) {
            return isBasic((Class<?>) type);
        }
        if (type instanceof TypeVariable) {
            return isBasic(root, TypeUtil.getActualType(root, type));
        }

        return false;
    }

    public static boolean isNotStruct(Class<?> clazz) {
        return !isStruct(clazz);
    }

    public static boolean isStruct(Class<?> clazz) {
        return AnnotationUtil.hasAnnotation(clazz, Struct.class);
    }

    public static boolean isStruct(Type root, Field field) {
        return isStruct(root, TypeUtil.getType(field));
    }

    public static boolean isStruct(Type root, Type type) {
        if (type instanceof Class) {
            return isStruct((Class<?>) type);
        }
        if (type instanceof ParameterizedType) {
            return isStruct((Class<?>) ((ParameterizedType) type).getRawType());
        }
        if (type instanceof TypeVariable) {
            return isStruct(root, TypeUtil.getActualType(root, type));
        }

        return false;
    }

    /**
     * Is ignore boolean.
     *
     * @param field the field
     *
     * @return the boolean
     */
    public static boolean isIgnore(Field field) {
        return AnnotationUtil.hasAnnotation(field, Ignore.class) || isTransient(field);
    }

}
