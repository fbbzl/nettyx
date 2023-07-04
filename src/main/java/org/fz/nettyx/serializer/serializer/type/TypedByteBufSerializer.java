package org.fz.nettyx.serializer.serializer.type;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.Serializers;
import org.fz.nettyx.serializer.annotation.FieldHandler;
import org.fz.nettyx.serializer.exception.SerializeException;
import org.fz.nettyx.serializer.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.handler.ByteBufHandler;
import org.fz.nettyx.serializer.handler.ReadHandler;
import org.fz.nettyx.serializer.handler.WriteHandler;
import org.fz.nettyx.serializer.serializer.ByteBufSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.stream.Stream;

import static org.fz.nettyx.serializer.Serializers.*;


/**
 * the basic serializer of byte-work Provides a protocol based on byte offset partitioning fields
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021/10/22 13:18
 */
@SuppressWarnings("unchecked")
public final class TypedByteBufSerializer implements ByteBufSerializer {

    private final ByteBuf byteBuf;

    private final Object domain;

    TypedByteBufSerializer(ByteBuf byteBuf, Object domain) {
        this.byteBuf = byteBuf;
        this.domain = domain;
    }

    public static <T> T read(ByteBuf byteBuf, T domain) {
        return new TypedByteBufSerializer(byteBuf, domain).parse();
    }

    public static <T> T read(ByteBuf byteBuf, Class<T> clazz) {
        return read(byteBuf, newInstance(clazz));
    }

    public static <T> T read(byte[] bytes, T domain) {
        return TypedByteBufSerializer.read(Unpooled.wrappedBuffer(bytes), domain);
    }

    public static <T> T read(byte[] bytes, Class<T> clazz) {
        return read(bytes, newInstance(clazz));
    }

    public static <T> T read(ByteBuffer byteBuffer, T domain) {
        return TypedByteBufSerializer.read(Unpooled.wrappedBuffer(byteBuffer), domain);
    }

    public static <T> T read(ByteBuffer byteBuffer, Class<T> clazz) {
        return read(byteBuffer, newInstance(clazz));
    }

    public static <T> T read(InputStream is, T domain) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int b = is.read(); b >= 0; b = is.read()) baos.write(b);
        is.close();
        return TypedByteBufSerializer.read(baos.toByteArray(), domain);
    }

    public static <T> T read(InputStream inputStream, Class<T> clazz) throws IOException {
        return read(inputStream, newInstance(clazz));
    }

    public static <T> ByteBuf write(T struct) {
        return new TypedByteBufSerializer(Unpooled.buffer(structSize(struct.getClass())), struct).from();
    }

    public static <T> byte[] writeBytes(T object) {
        ByteBuf writeBuf = TypedByteBufSerializer.write(object);
        try {
            return ByteBufUtil.getBytes(writeBuf);
        }
        finally {
            writeBuf.release();
        }
    }

    public static <T> ByteBuffer writeNioBuffer(T object) {
        return TypedByteBufSerializer.write(object).nioBuffer();
    }

    public static <T> void writeStream(T object, OutputStream outputStream) throws IOException {
        ByteBuf writeBuf = TypedByteBufSerializer.write(object);
        try {
            outputStream.write(ByteBufUtil.getBytes(writeBuf));
        }
        finally {
            writeBuf.release();
        }
    }

    /**
     * parse ByteBuf to Object
     */
    <T> T parse() {
        for (Field field : getInstantiateFields(getDomainType())) {
            try {
                Object value;
                // some fields may skip
                if (isIgnore(field))         {            continue;             }

                if (isReadHandleable(field)) { value = this.readHandled(field); }
                else
                if (isBasic(field))          { value = this.readBasic(field);   }
                else
                if (isStruct(field))         { value = this.readStruct(field);  }
                else
                if (isArray(field))          { value = this.readArray(field);   }
                else throw new TypeJudgmentException("can not determine field type, field is[" + field + "]");

                this.setFieldValue(field, value);
            }
            catch (Exception exception) { throw new SerializeException("field read exception, field is[" + field + "]", exception); }
        }
        return (T) domain;
    }

    /**
     * convert Object to ByteBuf
     */
    ByteBuf from() {
        for (Field field : getInstantiateFields(getDomainType())) {
            try {
                // some fields may skip
                if (isIgnore(field))          {         continue;         }

                if (isWriteHandleable(field)) { this.writeHandled(field); }
                else
                if (isBasic(field))           { this.writeBasic(field);   }
                else
                if (isStruct(field))          { this.writeStruct(field);  }
                else
                if (isArray(field))           { this.writeArray(field);   }
                else throw new TypeJudgmentException("can not determine field type, field is[" + field + "]");
            }
            catch (Exception exception) { throw new SerializeException("field write exception, field [" + field + "]", exception); }
        }
        return byteBuf;
    }

    private <B extends Basic<?>> B readBasic(Field basicField) {
        ByteBuf basicBuf = this.byteBuf.readBytes(basicSize(basicField));
        return createBasic(basicField).setByteBuf(basicBuf);
    }

    private <S> S readStruct(Field structField) {
        S struct = Serializers.createStruct(structField);
        ByteBuf structBuf = this.byteBuf.readBytes(structSize(structField));
        return TypedByteBufSerializer.read(structBuf, struct);
    }

    private <E> E[] readArray(Field arrayField) {
        E[] array = createArray(arrayField);

        ByteBuf arrayBuf = this.byteBuf.readBytes(arraySize(arrayField));

        Class<?> elementType = arrayField.getType().getComponentType();

        return isBasic(elementType) ?
            (E[]) toBasicArray((Basic<?>[]) array, arrayBuf) :
            toStructArray(array, arrayBuf);
    }

    private <T> T readHandled(Field handledField) {
        final Class<? extends ByteBufHandler> handlerClass = handledField.getAnnotation(FieldHandler.class).value();
        return (T) ((ReadHandler) newInstance(handlerClass)).doRead(this, handledField);
    }

    private <B extends Basic<?>> B[] toBasicArray(B[] basics, ByteBuf arrayBuf) {
        Class<B> elementType = (Class<B>) basics.getClass().getComponentType();
        int elementSize = basicSize(elementType);

        for (int i = 0; i < basics.length; i++) {
            basics[i] = createBasic(elementType).setByteBuf(arrayBuf.readBytes(elementSize));
        }

        return basics;
    }

    private <S> S[] toStructArray(S[] structs, ByteBuf arrayBuf) {
        Class<S> elementType = (Class<S>) structs.getClass().getComponentType();
        int elementSize = structSize(elementType);

        for (int i = 0; i < structs.length; i++) {
            structs[i] = TypedByteBufSerializer.read(arrayBuf.readBytes(elementSize), elementType);
        }

        return structs;
    }

    //************************************************      Read-write splitter      ************************************************//

    private void writeBasic(Field basicField) {
        Basic<?> basicValue = getFieldValue(basicField);
        if (basicValue == null) {
            this.byteBuf.writeBytes(new byte[basicSize(basicField)]);
        }
        else this.byteBuf.writeBytes(basicValue.getByteBuf());
    }

    private void writeStruct(Field structField) {
        Object structValue = getFieldValue(structField);

        if (structValue == null) {
            this.byteBuf.writeBytes(new byte[structSize(structField)]);
        }
        else this.byteBuf.writeBytes(TypedByteBufSerializer.write(structValue));
    }

    private void writeArray(Field arrayField) {
        Object[] arrayValue = getFieldValue(arrayField);

        if (arrayValue == null) {
            this.byteBuf.writeBytes(new byte[arraySize(arrayField)]);
            return;
        }

        Class<?> elementType = arrayField.getType().getComponentType();

        if (isBasic(elementType)) {
            fromBasicArray((Basic<?>[]) arrayValue);
        }
        else fromStructArray(arrayValue);
    }

    private void writeHandled(Field handledField) {
        final Class<? extends ByteBufHandler> handlerClass = handledField.getAnnotation(FieldHandler.class).value();
        ((WriteHandler) newInstance(handlerClass)).doWrite(this, handledField, getFieldValue(handledField), byteBuf);
    }

    private void fromBasicArray(Basic<?>[] basicArray) {
        Stream.of(basicArray).map(Basic::getByteBuf).forEach(this.byteBuf::writeBytes);
    }

    private void fromStructArray(Object[] structArray) {
        Stream.of(structArray).map(TypedByteBufSerializer::write).forEach(this.byteBuf::writeBytes);
    }

    public <V> V getFieldValue(Field field) {
        return Serializers.readField(this.earlyDomain(), field);
    }

    public <V> void setFieldValue(Field field, V value) {
        Serializers.writeField(this.earlyDomain(), field, value);
    }

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
