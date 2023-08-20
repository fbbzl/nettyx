package org.fz.nettyx.serializer.typed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.annotation.FieldHandler;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.handler.ByteBufHandler;
import org.fz.nettyx.handler.ReadHandler;
import org.fz.nettyx.handler.WriteHandler;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.Serializers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import static org.fz.nettyx.serializer.Serializers.*;


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
        return read(byteBuf, newInstance(clazz));
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
        return read(bytes, newInstance(clazz));
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
        return read(byteBuffer, newInstance(clazz));
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
        return read(inputStream, newInstance(clazz));
    }

    /**
     * convert domain to byteBuf
     *
     * @param <T>    the type parameter
     * @param domain the struct
     * @return the byte buf
     */
    public static <T> ByteBuf write(T domain) {
        return new TypedSerializer(Unpooled.buffer(structSize(domain.getClass())), domain).toByteBuf();
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
        for (Field field : getInstantiateFields(getDomainType())) {
            try {
                // some fields may skip
                if (isIgnore(field))         {            continue;             }
                // first check if field with annotation
                if (isReadHandleable(field)) { readHandled(field, this.domain, this); }
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
        for (Field field : getInstantiateFields(getDomainType())) {
            try {
                // some fields may skip
                if (isIgnore(field))          {         continue;         }
                // first check if field with annotation
                if (isWriteHandleable(field)) { writeHandled(field, this.domain, this, this.byteBuf); }
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
     */
    public static void readBasic(Field basicField, Object domain, ByteBuf byteBuf) {
        ByteBuf basicBuf = byteBuf.readBytes(basicSize(basicField));
        Basic<?> basic = createBasic(basicField).setByteBuf(basicBuf);
        Serializers.writeField(domain, basicField, basic);
    }

    /**
     * read struct into struct field
     */
    public static <S> void readStruct(Field structField, Object domain, ByteBuf byteBuf) {
        S struct = Serializers.createStruct(structField);
        ByteBuf structBuf = byteBuf.readBytes(structSize(structField));
        Serializers.writeField(domain, structField, TypedSerializer.read(structBuf, struct));
    }

    /**
     * read array field
     */
    public static <E> void readArray(Field arrayField, Object domain, ByteBuf byteBuf) {
        E[] array = createArray(arrayField);

        ByteBuf arrayBuf = byteBuf.readBytes(arraySize(arrayField));

        Class<?> elementType = arrayField.getType().getComponentType();

        Object[] arrayValue = isBasic(elementType) ?
                readBasicArray((Basic<?>[]) array, arrayBuf) :
                readStructArray(array, arrayBuf);

        Serializers.writeField(domain, arrayField, arrayValue);
    }

    /**
     * read the field with annotation {@link FieldHandler}
     *
     * @see FieldHandler
     * @param handledField the field with the @FieldHandler
     */
    public static void readHandled(Field handledField, Object domain, TypedSerializer uplevelSerializer) {
        final Class<? extends ByteBufHandler> handlerClass = handledField.getAnnotation(FieldHandler.class).value();
        Serializers.writeField(domain, handledField, ((ReadHandler<TypedSerializer>) newInstance(handlerClass)).doRead(uplevelSerializer, handledField));
    }

    /**
     * convert to basic array
     */
    private static <B extends Basic<?>> B[] readBasicArray(B[] basics, ByteBuf arrayBuf) {
        Class<B> elementType = (Class<B>) basics.getClass().getComponentType();
        int elementSize = basicSize(elementType);

        for (int i = 0; i < basics.length; i++) {
            basics[i] = createBasic(elementType).setByteBuf(arrayBuf.readBytes(elementSize));
        }

        return basics;
    }

    /**
     * convert to struct array
     */
    private static <S> S[] readStructArray(S[] structs, ByteBuf arrayBuf) {
        Class<S> elementType = (Class<S>) structs.getClass().getComponentType();
        int elementSize = structSize(elementType);

        for (int i = 0; i < structs.length; i++) {
            structs[i] = TypedSerializer.read(arrayBuf.readBytes(elementSize), elementType);
        }

        return structs;
    }

    /**
     * write basic bytes
     */
    public static void writeBasic(Field basicField, Object domain, ByteBuf byteBuf) {
        Basic<?> basicValue = Serializers.readField(domain, basicField);
        if (basicValue == null) byteBuf.writeBytes(new byte[basicSize(basicField)]);
        else                    byteBuf.writeBytes(basicValue.getByteBuf());
    }

    /**
     * write struct bytes
     */
    public static void writeStruct(Field structField, Object domain, ByteBuf byteBuf) {
        Object structValue = Serializers.readField(domain, structField);

        if (structValue == null) byteBuf.writeBytes(new byte[structSize(structField)]);
        else                     byteBuf.writeBytes(TypedSerializer.write(structValue));
    }

    /**
     * write array bytes
     */
    public static void writeArray(Field arrayField, Object domain, ByteBuf byteBuf) {
        Object[] arrayValue = Serializers.readField(domain, arrayField);
        int declaredLength = getLength(arrayField);

        if (arrayValue == null) {
            byteBuf.writeBytes(new byte[arraySize(arrayField)]);
            return;
        }
        // array element type
        Class<?> elementType = arrayField.getType().getComponentType();

        if (declaredLength < arrayValue.length) throw new IllegalArgumentException("[" + arrayField + "] array length exceed the assigned array length");
        if (declaredLength > arrayValue.length) arrayValue = fillArray(arrayValue, elementType, declaredLength);

        if (isBasic(elementType)) writeBasicArray((Basic<?>[]) arrayValue, basicSize((Class<Basic<?>>)elementType), byteBuf);
        else                      writeStructArray(arrayValue, structSize(elementType), byteBuf);
    }

    /**
     * write using handler
     */
    public static void writeHandled(Field handledField, Object domain, TypedSerializer uplevelSerializer, ByteBuf byteBuf) {
        final Class<? extends ByteBufHandler> handlerClass = handledField.getAnnotation(FieldHandler.class).value();
        ((WriteHandler<TypedSerializer>) newInstance(handlerClass)).doWrite(uplevelSerializer, handledField, Serializers.readField(domain, handledField), byteBuf);
    }

    /**
     * write basic array
     */
    private static void writeBasicArray(Basic<?>[] basicArray, int elementSize, ByteBuf byteBuf) {
        for (Basic<?> basic : basicArray) {
            if (basic == null) byteBuf.writeBytes(new byte[elementSize]);
            else               byteBuf.writeBytes(basic.getByteBuf());
        }
    }

    /**
     * write struct array
     */
    private static void writeStructArray(Object[] structArray, int elementSize, ByteBuf byteBuf) {
        for (Object struct : structArray) {
            if (struct == null) byteBuf.writeBytes(new byte[elementSize]);
            else                byteBuf.writeBytes(TypedSerializer.write(struct));
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
