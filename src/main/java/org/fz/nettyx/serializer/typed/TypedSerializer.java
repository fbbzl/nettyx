package org.fz.nettyx.serializer.typed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.typed.ByteBufHandler.ReadHandler;
import org.fz.nettyx.serializer.typed.ByteBufHandler.WriteHandler;
import org.fz.nettyx.serializer.typed.annotation.FieldHandler;
import org.fz.nettyx.serializer.typed.annotation.Length;
import org.fz.nettyx.util.StructUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import static io.netty.buffer.Unpooled.buffer;
import static org.fz.nettyx.serializer.typed.Serializers.*;
import static org.fz.nettyx.serializer.typed.Serializers.nullDefault;
import static org.fz.nettyx.util.StructUtils.getStructFields;

/**
 * the basic serializer of byte-work Provides a protocol based on byte offset partitioning fields
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:18
 */
@SuppressWarnings("unchecked")
public final class TypedSerializer implements Serializer {

    /**
     * byteBuf ready for serialization/deserialization
     */
    private final ByteBuf byteBuf;

    /**
     * an object ready for serialization/deserialization
     */
    private final Object domain;

    /**
     * Instantiates a new Typed byte buf serializer.
     *
     * @param byteBuf the byte buf
     * @param domain  the domain
     */
    TypedSerializer(ByteBuf byteBuf, Object domain) {
        this.byteBuf = byteBuf;
        this.domain = domain;
    }

    /**
     * convert byteBuf to domain object
     *
     * @param <T>     the type parameter
     * @param byteBuf the byte buf
     * @param domain  the domain
     * @return the t
     */
    public static <T> T read(ByteBuf byteBuf, T domain) {
        return new TypedSerializer(byteBuf, domain).toObject();
    }

    /**
     * convert byteBuf to domain object by class
     *
     * @param <T>     the type parameter
     * @param byteBuf the byte buf
     * @param clazz   the clazz
     * @return the t
     */
    public static <T> T read(ByteBuf byteBuf, Class<T> clazz) {
        return read(byteBuf, Serializers.newStructInstance(clazz));
    }

    /**
     * convert byte-array to domain object
     *
     * @param <T>    the type parameter
     * @param bytes  the bytes
     * @param domain the domain
     * @return the t
     */
    public static <T> T read(byte[] bytes, T domain) {
        return TypedSerializer.read(Unpooled.wrappedBuffer(bytes), domain);
    }

    /**
     * convert byte-array to domain object by class
     *
     * @param <T>   the type parameter
     * @param bytes the bytes
     * @param clazz the clazz
     * @return the t
     */
    public static <T> T read(byte[] bytes, Class<T> clazz) {
        return read(bytes, Serializers.newStructInstance(clazz));
    }

    /**
     * convert nio byteBuf to domain object
     *
     * @param <T>        the type parameter
     * @param byteBuffer the byte buffer
     * @param domain     the domain
     * @return the t
     */
    public static <T> T read(ByteBuffer byteBuffer, T domain) {
        return TypedSerializer.read(Unpooled.wrappedBuffer(byteBuffer), domain);
    }

    /**
     * convert byteBuf to domain object by class
     *
     * @param <T>        the type parameter
     * @param byteBuffer the byte buffer
     * @param clazz      the clazz
     * @return the t
     */
    public static <T> T read(ByteBuffer byteBuffer, Class<T> clazz) {
        return read(byteBuffer, Serializers.newStructInstance(clazz));
    }

    /**
     * convert InputStream to domain object
     *
     * @param <T>    the type parameter
     * @param is     the is
     * @param domain the domain
     * @return the t
     * @throws IOException the io exception
     */
    public static <T> T read(InputStream is, T domain) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int b = is.read(); b >= 0; b = is.read()) baos.write(b);
        is.close();
        return TypedSerializer.read(baos.toByteArray(), domain);
    }

    /**
     * convert InputStream to domain object by class
     *
     * @param <T>         the type parameter
     * @param inputStream the input stream
     * @param clazz       the clazz
     * @return the t
     * @throws IOException the io exception
     */
    public static <T> T read(InputStream inputStream, Class<T> clazz) throws IOException {
        return read(inputStream, Serializers.newStructInstance(clazz));
    }

    /**
     * convert domain to byteBuf
     *
     * @param <T>    the type parameter
     * @param domain the struct
     * @return the byte buf
     */
    public static <T> ByteBuf write(T domain) {
        return new TypedSerializer(buffer(), domain).toByteBuf();
    }

    /**
     * convert domain to byte-array
     *
     * @param <T>    the type parameter
     * @param domain the object
     * @return the byte [ ]
     */
    public static <T> byte[] writeBytes(T domain) {
        ByteBuf writeBuf = TypedSerializer.write(domain);
        try {
            return ByteBufUtil.getBytes(writeBuf);
        }
        finally {
            writeBuf.release();
        }
    }

    /**
     * convert domain to nio byteBuf
     *
     * @param <T>    the type parameter
     * @param domain the object
     * @return the byte buffer
     */
    public static <T> ByteBuffer writeNioBuffer(T domain) {
        return TypedSerializer.write(domain).nioBuffer();
    }

    /**
     * convert domain to output stream
     *
     * @param <T>          the type parameter
     * @param domain       the object
     * @param outputStream the output stream
     * @throws IOException the io exception
     */
    public static <T> void writeStream(T domain, OutputStream outputStream) throws IOException {
        ByteBuf writeBuf = TypedSerializer.write(domain);
        try {
            outputStream.write(ByteBufUtil.getBytes(writeBuf));
        }
        finally {
            writeBuf.release();
        }
    }

    /**
     * parse ByteBuf to Object
     *
     * @param <T> the type parameter
     * @return the t
     */
    <T> T toObject() {
        for (Field field : getStructFields(getDomainType())) {
            try {
                Object fieldValue;
                // some fields may ignore
                if (isIgnore(field)) continue;

                if (useReadHandler(field)) fieldValue = readHandled(field, this);
                else if (isBasic(field))   fieldValue = readBasic(field,  this.getByteBuf());
                else if (isStruct(field))  fieldValue = readStruct(field, this.getByteBuf());
                else if (isArray(field))   fieldValue = readArray(field.getType().getComponentType(), getArrayLength(field), this.getByteBuf());
                else                       throw new TypeJudgmentException("can not determine field type, field is[" + field + "]");

                StructUtils.writeField(domain, field, fieldValue);
            }
            catch (Exception exception) { throw new SerializeException("field read exception, field is[" + field + "]", exception); }
        }
        return (T) domain;
    }

    /**
     * convert Object to ByteBuf
     *
     * @return the byte buf
     */
    ByteBuf toByteBuf() {
        for (Field field : getStructFields(getDomainType())) {
            try {
                Object fieldValue = StructUtils.readField(domain, field);
                // some fields may ignore
                if (isIgnore(field)) continue;

                if (useWriteHandler(field)) writeHandled(field, this.domain, this, this.getByteBuf());
                else if (isBasic(field))
                    writeBasic((Basic<?>) nullDefault(fieldValue, () -> newBasicInstance(field, buffer())), this.getByteBuf());
                else if (isStruct(field))
                    writeStruct(nullDefault(fieldValue, () -> newStructInstance(field)), this.getByteBuf());
                else if (isArray(field))
                    writeArray(nullDefault(fieldValue, () -> newArrayInstance(field)), field.getType().getComponentType(), getArrayLength(field), this.getByteBuf());

                else throw new TypeJudgmentException("can not determine field type, field is[" + field + "]");
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
        return newBasicInstance(basicField, byteBuf);
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
        S struct = Serializers.newStructInstance(structField);
        return TypedSerializer.read(byteBuf, struct);
    }

    /**
     * read array field
     *
     * @param <E>        the type parameter
     * @param arrayField the array field
     * @param byteBuf    the byte buf
     */
    public static <E> E[] readArray(Field arrayField, ByteBuf byteBuf) {
        E[] array = newArrayInstance(arrayField);

        Class<?> elementType = arrayField.getType().getComponentType();

        Object[] arrayValue = isBasic(elementType) ?
                readBasicArray((Basic<?>[]) array, byteBuf) :
                readStructArray(array, byteBuf);

        return (E[]) arrayValue;
    }

    public static <E> E[] readArray(Class<?> elementType, int length, ByteBuf byteBuf) {
        E[] array = newArrayInstance(elementType, length);

        Object[] arrayValue = isBasic(elementType) ?
                readBasicArray((Basic<?>[]) array, byteBuf) :
                readStructArray(array, byteBuf);

        return (E[]) arrayValue;
    }

    /**
     * read the field with annotation {@link FieldHandler}
     *
     * @param handledField    the field with the @FieldHandler
     * @param upperSerializer the upper serializer
     * @see FieldHandler
     */
    public static Object readHandled(Field handledField, TypedSerializer upperSerializer) {
        final Class<? extends ByteBufHandler> handlerClass = handledField.getAnnotation(FieldHandler.class).value();
        return ((ReadHandler<TypedSerializer>) Serializers.newHandlerInstance(handlerClass)).doRead(upperSerializer, handledField);
    }

    /**
     * convert to basic array
     */
    private static <B extends Basic<?>> B[] readBasicArray(B[] basics, ByteBuf arrayBuf) {
        Class<B> elementType = (Class<B>) basics.getClass().getComponentType();

        for (int i = 0; i < basics.length; i++) {
            basics[i] = Serializers.newBasicInstance(elementType, arrayBuf);
        }

        return basics;
    }

    /**
     * convert to struct array
     */
    private static <S> S[] readStructArray(S[] structs, ByteBuf arrayBuf) {
        Class<S> elementType = (Class<S>) structs.getClass().getComponentType();

        for (int i = 0; i < structs.length; i++) {
            structs[i] = TypedSerializer.read(arrayBuf, elementType);
        }

        return structs;
    }

    //*************************************         read write splitter         **************************************//

    /**
     * write basic bytes
     */
    public static <B extends Basic<?>> void writeBasic(B basicValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(basicValue.getByteBuf());
    }

    /**
     * write struct bytes
     *
     * @param structValue the structValue
     */
    public static <S> void writeStruct(S structValue, ByteBuf writingBuf) {
        writingBuf.writeBytes(TypedSerializer.write(structValue));
    }

    public static void writeArray(Object arrayValue, Class<?> elementType, int declaredLength, ByteBuf writingBuf) {
        // cast to array
        Object[] array = (Object[]) arrayValue;
        if (declaredLength < array.length) throw new IllegalArgumentException("array length exceed the declared length in annotation [" + Length.class + "]");
        if (declaredLength > array.length) array = fillArray(array, elementType, declaredLength);

        if (isBasic(elementType)) writeBasicArray((Basic<?>[]) array, (Class<Basic<?>>) elementType, writingBuf);
        else                      writeStructArray(array, elementType, writingBuf);
    }

    /**
     * write using handler
     *
     * @param handleField the handled field
     * @param upperSerializer the upper serializer
     * @param writingBuf the byte buf
     */
    public static void writeHandled(Field handleField, Object fieldValue, TypedSerializer upperSerializer, ByteBuf writingBuf) {
        final Class<? extends ByteBufHandler> handlerClass = handleField.getAnnotation(FieldHandler.class).value();
        ((WriteHandler<TypedSerializer>) Serializers.newHandlerInstance(handlerClass)).doWrite(upperSerializer, handleField, fieldValue, writingBuf);
    }

    /**
     * write basic array
     */
    private static void writeBasicArray(Basic<?>[] basicArray, Class<Basic<?>> basicType, ByteBuf writingBuf) {
        for (Basic<?> basic : basicArray) {
            writingBuf.writeBytes(nullDefault(basic, () -> Serializers.newBasicInstance(basicType, buffer())).getByteBuf());
        }
    }

    /**
     * write struct array
     */
    private static void writeStructArray(Object[] structArray, Class<?> structType, ByteBuf writingBuf) {
        for (Object struct : structArray) {
            writingBuf.writeBytes(TypedSerializer.write(nullDefault(struct, () -> Serializers.newStructInstance(structType))));
        }
    }

    /**
     * Early domain t.
     *
     * @param <T> the type parameter
     * @return the t
     */
    public <T> T earlyDomain() {
        return (T) this.domain;
    }

    @Override
    public <T> Class<T> getDomainType() {
        return (Class<T>) this.domain.getClass();
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

}
