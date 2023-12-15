package org.fz.nettyx.serializer;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.annotation.FieldHandler;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.ByteBufHandler.ReadHandler;
import org.fz.nettyx.serializer.ByteBufHandler.WriteHandler;
import org.fz.nettyx.serializer.type.Basic;
import org.fz.nettyx.util.StructUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import static org.fz.nettyx.serializer.Serializers.*;
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
     * @param domain the domain
     */
    TypedSerializer(ByteBuf byteBuf, Object domain) {
        this.byteBuf = byteBuf;
        this.domain = domain;
    }

    /**
     * convert byteBuf to domain object
     *
     * @param <T> the type parameter
     * @param byteBuf the byte buf
     * @param domain the domain
     * @return the t
     */
    public static <T> T read(ByteBuf byteBuf, T domain) {
        return new TypedSerializer(byteBuf, domain).toObject();
    }

    /**
     * convert byteBuf to domain object by class
     *
     * @param <T> the type parameter
     * @param byteBuf the byte buf
     * @param clazz the clazz
     * @return the t
     */
    public static <T> T read(ByteBuf byteBuf, Class<T> clazz) {
        return read(byteBuf, Serializers.newStructInstance(clazz));
    }

    /**
     * convert byte-array to domain object
     *
     * @param <T> the type parameter
     * @param bytes the bytes
     * @param domain the domain
     * @return the t
     */
    public static <T> T read(byte[] bytes, T domain) {
        return TypedSerializer.read(Unpooled.wrappedBuffer(bytes), domain);
    }

    /**
     * convert byte-array to domain object by class
     *
     * @param <T> the type parameter
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
     * @param <T> the type parameter
     * @param byteBuffer the byte buffer
     * @param domain the domain
     * @return the t
     */
    public static <T> T read(ByteBuffer byteBuffer, T domain) {
        return TypedSerializer.read(Unpooled.wrappedBuffer(byteBuffer), domain);
    }

    /**
     * convert byteBuf to domain object by class
     *
     * @param <T> the type parameter
     * @param byteBuffer the byte buffer
     * @param clazz the clazz
     * @return the t
     */
    public static <T> T read(ByteBuffer byteBuffer, Class<T> clazz) {
        return read(byteBuffer, Serializers.newStructInstance(clazz));
    }

    /**
     * convert InputStream to domain object
     *
     * @param <T> the type parameter
     * @param is the is
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
     * @param <T> the type parameter
     * @param inputStream the input stream
     * @param clazz the clazz
     * @return the t
     * @throws IOException the io exception
     */
    public static <T> T read(InputStream inputStream, Class<T> clazz) throws IOException {
        return read(inputStream, Serializers.newStructInstance(clazz));
    }

    /**
     * convert domain to byteBuf
     *
     * @param <T> the type parameter
     * @param domain the struct
     * @return the byte buf
     */
    public static <T> ByteBuf write(T domain) {
        return new TypedSerializer(Unpooled.buffer(), domain).toByteBuf();
    }

    /**
     * convert domain to byte-array
     *
     * @param <T> the type parameter
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
     * @param <T> the type parameter
     * @param domain the object
     * @return the byte buffer
     */
    public static <T> ByteBuffer writeNioBuffer(T domain) {
        return TypedSerializer.write(domain).nioBuffer();
    }

    /**
     * convert domain to output stream
     *
     * @param <T> the type parameter
     * @param domain the object
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
                // some fields may skip
                if (isIgnore(field))         {            continue;             }
                // first check if field with annotation
                if (useReadHandler(field)) { readHandled(field, this.domain, this); }
                else
                if (isBasic(field))          { readBasic(field,   this.domain, this.byteBuf);   }
                else
                if (isStruct(field))         { readStruct(field,  this.domain, this.byteBuf);   }
                else
                if (isArray(field))          { readArray(field,   this.domain, this.byteBuf);   }
                else throw new TypeJudgmentException("can not determine field type, field is[" + field + "]");
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
                // some fields may skip
                if (isIgnore(field))          {         continue;         }
                // first check if field with annotation
                if (useWriteHandler(field)) { writeHandled(field, this.domain, this, this.byteBuf); }
                else
                if (isBasic(field))           { writeBasic(field, this.domain, this.byteBuf);   }
                else
                if (isStruct(field))          { writeStruct(field, this.domain, this.byteBuf);  }
                else
                if (isArray(field))           { writeArray(field, this.domain, this.byteBuf);   }
                else throw new TypeJudgmentException("can not determine field type, field is[" + field + "]");
            }
            catch (Exception exception) { throw new SerializeException("field write exception, field [" + field + "]", exception); }
        }
        return byteBuf;
    }

    /**
     * read buf into basic field
     *
     * @param basicField the basic field
     * @param domain the domain
     * @param byteBuf the byte buf
     */
    public static void readBasic(Field basicField, Object domain, ByteBuf byteBuf) {
        Basic<?> basic = newBasicInstance(basicField, byteBuf);
        StructUtils.writeField(domain, basicField, basic);
    }

    /**
     * read struct into struct field
     *
     * @param <S> the type parameter
     * @param structField the struct field
     * @param domain the domain
     * @param byteBuf the byte buf
     */
    public static <S> void readStruct(Field structField, Object domain, ByteBuf byteBuf) {
        // invoke struct no-arg constructor
        S struct = Serializers.newStructInstance(structField);
        StructUtils.writeField(domain, structField, TypedSerializer.read(byteBuf, struct));
    }

    /**
     * read array field
     *
     * @param <E> the type parameter
     * @param arrayField the array field
     * @param domain the domain
     * @param byteBuf the byte buf
     */
    public static <E> void readArray(Field arrayField, Object domain, ByteBuf byteBuf) {
        E[] array = newArrayInstance(arrayField);

        Class<?> elementType = arrayField.getType().getComponentType();

        Object[] arrayValue = isBasic(elementType) ?
                readBasicArray((Basic<?>[]) array, byteBuf) :
                readStructArray(array, byteBuf);

        StructUtils.writeField(domain, arrayField, arrayValue);
    }

    /**
     * read the field with annotation {@link FieldHandler}
     *
     * @param handledField the field with the @FieldHandler
     * @param domain the domain
     * @param upperSerializer the upper serializer
     * @see FieldHandler
     */
    public static void readHandled(Field handledField, Object domain, TypedSerializer upperSerializer) {
        final Class<? extends ByteBufHandler> handlerClass = handledField.getAnnotation(FieldHandler.class).value();
        StructUtils.writeField(domain, handledField, ((ReadHandler<TypedSerializer>) Serializers.newHandlerInstance(handlerClass)).doRead(upperSerializer, handledField));
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
     *
     * @param basicField the basic field
     * @param domain the domain
     * @param byteBuf the byte buf
     */
    public static void writeBasic(Field basicField, Object domain, ByteBuf byteBuf) {
        Basic<?> basicValue = StructUtils.readField(domain, basicField);

        // new basic by empty buffer
        if (basicValue == null) basicValue = Serializers.newBasicInstance(basicField, Unpooled.buffer());

        byteBuf.writeBytes(basicValue.getByteBuf());
    }

    /**
     * write struct bytes
     *
     * @param structField the struct field
     * @param domain the domain
     * @param byteBuf the byte buf
     */
    public static void writeStruct(Field structField, Object domain, ByteBuf byteBuf) {
        Object structValue = StructUtils.readField(domain, structField);

        // new struct if null
        if (structValue == null) structValue = Serializers.newStructInstance(structField);

        byteBuf.writeBytes(TypedSerializer.write(structValue));
    }

    /**
     * write array bytes
     *
     * @param arrayField the array field
     * @param domain the domain
     * @param byteBuf the byte buf
     */
    public static void writeArray(Field arrayField, Object domain, ByteBuf byteBuf) {
        Object[] arrayValue = StructUtils.readField(domain, arrayField);
        int declaredLength = getArrayLength(arrayField);

        if (arrayValue == null) {
            arrayValue = Serializers.newArrayInstance(arrayField);
        }
        // array element type
        Class<?> elementType = arrayField.getType().getComponentType();

        if (declaredLength < arrayValue.length) throw new IllegalArgumentException("[" + arrayField + "] array length exceed the assigned array length");
        if (declaredLength > arrayValue.length) arrayValue = fillArray(arrayValue, elementType, declaredLength);

        if (isBasic(elementType)) writeBasicArray((Basic<?>[]) arrayValue, (Class<Basic<?>>) elementType, byteBuf);
        else                      writeStructArray(arrayValue, elementType, byteBuf);
    }

    /**
     * write using handler
     *
     * @param handledField the handled field
     * @param domain the domain
     * @param upperSerializer the upper serializer
     * @param byteBuf the byte buf
     */
    public static void writeHandled(Field handledField, Object domain, TypedSerializer upperSerializer, ByteBuf byteBuf) {
        final Class<? extends ByteBufHandler> handlerClass = handledField.getAnnotation(FieldHandler.class).value();
        ((WriteHandler<TypedSerializer>) Serializers.newHandlerInstance(handlerClass)).doWrite(upperSerializer, handledField, StructUtils.readField(domain, handledField), byteBuf);
    }

    /**
     * write basic array
     */
    private static void writeBasicArray(Basic<?>[] basicArray, Class<Basic<?>> basicType, ByteBuf byteBuf) {
        for (Basic<?> basic : basicArray) {
            if (basic == null) {
                basic = Serializers.newBasicInstance(basicType, Unpooled.buffer());
            }

            byteBuf.writeBytes(basic.getByteBuf());
        }
    }

    /**
     * write struct array
     */
    private static void writeStructArray(Object[] structArray, Class<?> structType, ByteBuf byteBuf) {
        for (Object struct : structArray) {
            if (struct == null) {
                struct = Serializers.newStructInstance(structType);
            }

            byteBuf.writeBytes(TypedSerializer.write(struct));
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

}
