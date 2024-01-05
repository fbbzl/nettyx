package org.fz.nettyx.serializer.struct;

import static io.netty.buffer.Unpooled.buffer;
import static org.fz.nettyx.serializer.struct.SerializerHandler.isReadHandler;
import static org.fz.nettyx.serializer.struct.SerializerHandler.isWriteHandler;
import static org.fz.nettyx.serializer.struct.StructUtils.getArrayLength;
import static org.fz.nettyx.serializer.struct.StructUtils.getComponentType;
import static org.fz.nettyx.serializer.struct.StructUtils.getStructFields;
import static org.fz.nettyx.serializer.struct.StructUtils.newArray;
import static org.fz.nettyx.serializer.struct.StructUtils.newBasic;
import static org.fz.nettyx.serializer.struct.StructUtils.newStruct;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.function.Supplier;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.struct.SerializerHandler.ReadHandler;
import org.fz.nettyx.serializer.struct.SerializerHandler.WriteHandler;
import org.fz.nettyx.serializer.struct.annotation.Ignore;
import org.fz.nettyx.serializer.struct.annotation.Length;
import org.fz.nettyx.serializer.struct.annotation.PropertyHandler;
import org.fz.nettyx.serializer.struct.annotation.Struct;

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
     * an object ready for serialization/deserialization
     */
    private final Object struct;

    /**
     * Instantiates a new Typed byte buf serializer.
     *
     * @param byteBuf the byte buf
     * @param struct  the struct
     */
    StructSerializer(ByteBuf byteBuf, Object struct) {
        this.byteBuf = byteBuf;
        this.struct = struct;
    }

    /**
     * convert byteBuf to struct object
     *
     * @param <T>     the type parameter
     * @param byteBuf the byte buf
     * @param struct  the struct
     * @return the t
     */
    public static <T> T read(ByteBuf byteBuf, T struct) {
        return new StructSerializer(byteBuf, struct).toObject();
    }

    /**
     * convert byteBuf to struct object by class
     *
     * @param <T>     the type parameter
     * @param byteBuf the byte buf
     * @param clazz   the clazz
     * @return the t
     */
    public static <T> T read(ByteBuf byteBuf, Class<T> clazz) {
        return read(byteBuf, newStruct(clazz));
    }

    /**
     * convert byte-array to struct object
     *
     * @param <T>    the type parameter
     * @param bytes  the bytes
     * @param struct the struct
     * @return the t
     */
    public static <T> T read(byte[] bytes, T struct) {
        return StructSerializer.read(Unpooled.wrappedBuffer(bytes), struct);
    }

    /**
     * convert byte-array to struct object by class
     *
     * @param <T>   the type parameter
     * @param bytes the bytes
     * @param clazz the clazz
     * @return the t
     */
    public static <T> T read(byte[] bytes, Class<T> clazz) {
        return read(bytes, newStruct(clazz));
    }

    /**
     * convert nio byteBuf to struct object
     *
     * @param <T>        the type parameter
     * @param byteBuffer the byte buffer
     * @param struct     the struct
     * @return the t
     */
    public static <T> T read(ByteBuffer byteBuffer, T struct) {
        return StructSerializer.read(Unpooled.wrappedBuffer(byteBuffer), struct);
    }

    /**
     * convert byteBuf to struct object by class
     *
     * @param <T>        the type parameter
     * @param byteBuffer the byte buffer
     * @param clazz      the clazz
     * @return the t
     */
    public static <T> T read(ByteBuffer byteBuffer, Class<T> clazz) {
        return read(byteBuffer, newStruct(clazz));
    }

    /**
     * convert InputStream to struct object
     *
     * @param <T>    the type parameter
     * @param is     the is
     * @param struct the struct
     * @return the t
     * @throws IOException the io exception
     */
    public static <T> T read(InputStream is, T struct) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int b = is.read(); b >= 0; b = is.read()) baos.write(b);
        is.close();
        return StructSerializer.read(baos.toByteArray(), struct);
    }

    /**
     * convert InputStream to struct object by class
     *
     * @param <T>         the type parameter
     * @param inputStream the input stream
     * @param clazz       the clazz
     * @return the t
     * @throws IOException the io exception
     */
    public static <T> T read(InputStream inputStream, Class<T> clazz) throws IOException {
        return read(inputStream, newStruct(clazz));
    }

    /**
     * convert struct to byteBuf
     *
     * @param <T>    the type parameter
     * @param struct the struct
     * @return the byte buf
     */
    public static <T> ByteBuf write(T struct) {
        return new StructSerializer(buffer(), struct).toByteBuf();
    }

    /**
     * convert struct to byte-array
     *
     * @param <T>    the type parameter
     * @param struct the object
     * @return the byte [ ]
     */
    public static <T> byte[] writeBytes(T struct) {
        ByteBuf writeBuf = StructSerializer.write(struct);
        try {
            return ByteBufUtil.getBytes(writeBuf);
        }
        finally {
            writeBuf.release();
        }
    }

    /**
     * convert struct to nio byteBuf
     *
     * @param <T>    the type parameter
     * @param struct the object
     * @return the byte buffer
     */
    public static <T> ByteBuffer writeNioBuffer(T struct) {
        return StructSerializer.write(struct).nioBuffer();
    }

    /**
     * convert struct to output stream
     *
     * @param <T>          the type parameter
     * @param struct       the object
     * @param outputStream the output stream
     * @throws IOException the io exception
     */
    public static <T> void writeStream(T struct, OutputStream outputStream) throws IOException {
        ByteBuf writeBuf = StructSerializer.write(struct);
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
        for (Field field : getStructFields(getStructType())) {
            try {
                Object fieldValue;
                // some fields may ignore
                if (isIgnore(field)) continue;

                if (useReadHandler(field)) fieldValue = readHandled(field, this);
                else if (isBasic(field))   fieldValue = readBasic(field,  this.getByteBuf());
                else if (isStruct(field))  fieldValue = readStruct(field, this.getByteBuf());
                else if (isArray(field))   fieldValue = readArray(field.getType().getComponentType(), getArrayLength(field), this.getByteBuf());
                else                       throw new TypeJudgmentException("can not determine field type, field is[" + field + "]");

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

                if (useWriteHandler(field)) writeHandled(field, this.struct, this, this.getByteBuf());
                else
                if (isBasic(field))         writeBasic((Basic<?>) nullDefault(fieldValue, () -> StructUtils.newBasic(field, buffer())), this.getByteBuf());
                else
                if (isStruct(field))        writeStruct(nullDefault(fieldValue, () -> StructUtils.newStruct(field)), this.getByteBuf());
                else
                if (isArray(field))         writeArray(nullDefault(fieldValue, () -> StructUtils.newArray(field)), getComponentType(field), getArrayLength(field), this.getByteBuf());

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

    /**
     * read array field
     *
     * @param <E>        the type parameter
     * @param arrayField the array field
     * @param byteBuf    the byte buf
     */
    public static <E> E[] readArray(Field arrayField, ByteBuf byteBuf) {
        E[] array = StructUtils.newArray(arrayField);

        Class<E> elementType = (Class<E>) arrayField.getType().getComponentType();

        Object[] arrayValue = isBasic(elementType) ?
                readBasicArray((Basic<?>[]) array, byteBuf) :
                readStructArray(array, byteBuf);

        return (E[]) arrayValue;
    }

    public static <E> E[] readArray(Class<E> elementType, int length, ByteBuf byteBuf) {
        E[] array = newArray(elementType, length);

        Object[] arrayValue = isBasic(elementType) ?
                readBasicArray((Basic<?>[]) array, byteBuf) :
                readStructArray(array, byteBuf);

        return (E[]) arrayValue;
    }

    /**
     * read the field with annotation {@link PropertyHandler}
     *
     * @param handledField    the field with the {@link PropertyHandler}
     * @param upperSerializer the upper serializer
     * @see PropertyHandler
     */
    public static Object readHandled(Field handledField, StructSerializer upperSerializer) {
        ReadHandler<?> readHandler = StructUtils.getPropertySerializerHandler(handledField);
        // TODO
        return readHandler.doRead(upperSerializer, handledField, null);
    }

    /**
     * convert to basic array
     */
    private static <B extends Basic<?>> B[] readBasicArray(B[] basics, ByteBuf arrayBuf) {
        Class<B> elementType = (Class<B>) basics.getClass().getComponentType();

        for (int i = 0; i < basics.length; i++) {
            basics[i] = newBasic(elementType, arrayBuf);
        }

        return basics;
    }

    /**
     * convert to struct array
     */
    private static <S> S[] readStructArray(S[] structs, ByteBuf arrayBuf) {
        Class<S> elementType = (Class<S>) structs.getClass().getComponentType();

        for (int i = 0; i < structs.length; i++) {
            structs[i] = StructSerializer.read(arrayBuf, elementType);
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
        writingBuf.writeBytes(StructSerializer.write(structValue));
    }

    public static <T> void writeArray(Object arrayValue, Class<T> elementType, int declaredLength, ByteBuf writingBuf) {
        // cast to array
        T[] array = (T[]) arrayValue;
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
    public static void writeHandled(Field handleField, Object fieldValue, StructSerializer upperSerializer,
        ByteBuf writingBuf) {
        WriteHandler<?> writeHandler = StructUtils.getPropertySerializerHandler(handleField);
        // TODO
        writeHandler.doWrite(upperSerializer, handleField, fieldValue, null, writingBuf);
    }

    /**
     * write basic array
     */
    private static void writeBasicArray(Basic<?>[] basicArray, Class<Basic<?>> basicType, ByteBuf writingBuf) {
        for (Basic<?> basic : basicArray) {
            writingBuf.writeBytes(nullDefault(basic, () -> newBasic(basicType, buffer())).getByteBuf());
        }
    }

    /**
     * write struct array
     */
    private static void writeStructArray(Object[] structArray, Class<?> structType, ByteBuf writingBuf) {
        for (Object struct : structArray) {
            writingBuf.writeBytes(StructSerializer.write(nullDefault(struct, () -> newStruct(structType))));
        }
    }

    public static <T> T nullDefault(T obj, Supplier<T> defSupplier) {
        if (obj == null) return defSupplier.get();
        else             return obj;
    }

    /**
     * Fill array object [ ].
     *
     * @param arrayValue the array value
     * @param elementType the element type
     * @param length the length
     * @return the object [ ]
     */
    public static <T> T[] fillArray(T[] arrayValue, Class<T> elementType, int length) {
        T[] filledArray = (T[]) Array.newInstance(elementType, length);
        System.arraycopy(arrayValue, 0, filledArray, 0, arrayValue.length);
        return filledArray;
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
        return StructUtils.isAnnotationPresent(clazz, Struct.class);
    }

    /**
     * Is array boolean.
     *
     * @param <T> the type parameter
     * @param object the object
     * @return the boolean
     */
    public static <T> boolean isArray(T object) {
        return isArray(object.getClass());
    }

    /**
     * Is array boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean isArray(Field field) {
        return field.getType().isArray();
    }

    /**
     * Is array boolean.
     *
     * @param clazz the clazz
     * @return the boolean
     */
    public static boolean isArray(Class<?> clazz) {
        return clazz.isArray();
    }

    /**
     * Is ignore boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean isIgnore(Field field) {
        return StructUtils.isAnnotationPresent(field, Ignore.class);
    }

    /**
     * Is read handleable boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean useReadHandler(AnnotatedElement field) {
        return isReadHandler((SerializerHandler) StructUtils.getPropertySerializerHandler(field));
    }

    /**
     * Is write handleable boolean.
     *
     * @param field the field
     * @return the boolean
     */
    public static boolean useWriteHandler(AnnotatedElement field) {
        return isWriteHandler((SerializerHandler) StructUtils.getPropertySerializerHandler(field));
    }

    /**
     * Early struct t.
     *
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
