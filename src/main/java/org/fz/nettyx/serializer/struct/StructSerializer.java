package org.fz.nettyx.serializer.struct;

import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.fz.erwin.exception.Throws;
import org.fz.erwin.lang.TypeRefer;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.struct.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.ByteBuffer;

import static io.netty.buffer.Unpooled.buffer;
import static org.fz.nettyx.serializer.struct.StructHelper.*;
import static org.fz.nettyx.serializer.struct.StructSerializerContext.STRUCT_DEFINITION_CACHE;
import static org.fz.nettyx.serializer.struct.StructSerializerContext.getStructDefinition;

/**
 * the struct serializer
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:18
 */
@SuppressWarnings("unchecked")
public final class StructSerializer implements Serializer {
    /**
     * root of struct
     */
    private final Type root;

    public Type getType()
    {
        return root;
    }

    StructSerializer(Type root)
    {
        this.root = root;
    }

    public static <T> T toStruct(
            Type    root,
            ByteBuf byteBuf)
    {
        if (root instanceof TypeRefer<?> typeRefer)
            return toStruct(typeRefer.getTypeValue(), byteBuf);
        else
            return new StructSerializer(root).doDeserialize(byteBuf);
    }

    public static <T> T toStruct(
            Type   type,
            byte[] bytes)
    {
        return toStruct(type, Unpooled.wrappedBuffer(bytes));
    }

    public static <T> T toStruct(
            Type       root,
            ByteBuffer byteBuffer)
    {
        return toStruct(root, Unpooled.wrappedBuffer(byteBuffer));
    }

    public static <T> T toStruct(
            Type        root,
            InputStream is) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (int b = is.read(); b >= 0; b = is.read()) baos.write(b);
        is.close();
        return toStruct(root, baos.toByteArray());
    }

    //*************************************       read write splitter     ********************************************//

    public static <T> ByteBuf toByteBuf(T struct)
    {
        return toByteBuf(struct.getClass(), struct);
    }

    public static <T> ByteBuf toByteBuf(
            Type root,
            T    struct)
    {
        Throws.ifNull(struct, () -> "struct can not be null when write, root type: [" + root + "]");

        if (root instanceof Class<?> || root instanceof ParameterizedType)
            return new StructSerializer(root).doSerialize(struct);
        else
        if (root instanceof TypeRefer<?> typeRefer)
            return toByteBuf(typeRefer.getTypeValue(), struct);
        else
            throw new TypeJudgmentException(root);
    }

    public static <T> byte[] toBytes(T struct)
    {
        return toBytes(struct.getClass(), struct);
    }

    public static <T> byte[] toBytes(
            Type root,
            T    struct)
    {
        ByteBuf writeBuf = toByteBuf(root, struct);
        try {
            byte[] bytes = new byte[writeBuf.readableBytes()];
            writeBuf.readBytes(bytes);
            return bytes;
        }
        finally {
            ReferenceCountUtil.release(writeBuf);
        }
    }

    public static <T> ByteBuffer toNioBuffer(T struct)
    {
        return toNioBuffer(struct.getClass(), struct);
    }

    public static <T> ByteBuffer toNioBuffer(Type root, T struct)
    {
        return ByteBuffer.wrap(toBytes(root, struct));
    }

    public static <T> void writeStream(T struct, OutputStream outputStream) throws IOException
    {
        outputStream.write(toBytes(struct.getClass(), struct));
    }

    public static <T> void writeStream(
            Type         root,
            T            struct,
            OutputStream outputStream) throws IOException
    {
        outputStream.write(toBytes(root, struct));
    }

    //*************************************      working code splitter      ******************************************//

    @Override
    public <S> S doDeserialize(ByteBuf byteBuf)
    {
        return readStruct(root, byteBuf);
    }

    @Override
    public ByteBuf doSerialize(Object struct)
    {
        ByteBuf writing;
        writeStruct(root, struct, writing = buffer());
        return writing;
    }

    public <B extends Basic<?>> B readBasic(
            Class<?> basicType,
            ByteBuf  byteBuf)
    {
        return newBasic(basicType, byteBuf);
    }

    public <S> S readStruct(Type structType, ByteBuf byteBuf)
    {
        StructDefinition structDef = getStructDefinition(structType);
        Object struct           = structDef.constructor().get();
        Type   actualStructType = TypeUtil.getActualType(root, structType);
        for (StructField field : structDef.fields()) {
            Type fieldType = field.type(actualStructType);
            StructFieldHandler<?> handler   = field.handler();
            try
            {
                Object fieldVal = handler.doRead(this, root, struct, field, fieldType, byteBuf, field.annotation());
                field.setter().accept(struct, fieldVal);
            }
            catch (Exception exception) {
                throw new SerializeException("read exception occur, field is [" + field + "]", exception);
            }
        }
        return (S) struct;
    }

    public  <T> T[] readArray(
            Type    elementType,
            ByteBuf byteBuf,
            int     length)
    {
        if (isBasic(elementType))  return (T[]) readBasicArray((Class<? extends Basic<?>>) elementType, byteBuf, length);
        if (isStruct(elementType)) return readStructArray(elementType, byteBuf, length);
        else                       throw new TypeJudgmentException(elementType);
    }

    public <B extends Basic<?>> B[] readBasicArray(
            Class<?> elementType,
            ByteBuf  byteBuf,
            int      length)
    {
        B[] basics = newArray(elementType, length);

        for (int i = 0; i < basics.length; i++) basics[i] = newBasic(elementType, byteBuf);

        return basics;
    }

    public <S> S[] readStructArray(
            Type    elementType,
            ByteBuf byteBuf,
            int     length)
    {
        S[] structs = newArray(elementType, length);

        for (int i = 0; i < structs.length; i++) structs[i] = readStruct(elementType, byteBuf);

        return structs;
    }

    public <B extends Basic<?>> void writeBasic(Object  basicValue, ByteBuf writingBuf)
    {
        writingBuf.writeBytes(((B) (basicValue)).getBytes());
    }

    public <S> void writeStruct(
            Type    structType,
            S       struct,
            ByteBuf writing)
    {
        StructDefinition structDef        = getStructDefinition(structType);
        Type             actualStructType = TypeUtil.getActualType(root, structType);
        for (StructField field : structDef.fields()) {
            Type                  fieldType = field.type(actualStructType);
            StructFieldHandler<?> handler   = field.handler();
            Object                fieldVal  = field.getter().apply(struct);

            try
            {
                handler.doWrite(this, root, struct, field, fieldType, fieldVal, writing, field.annotation());
            }
            catch (Exception exception) {
                throw new SerializeException("write exception occur, field [" + field + "]", exception);
            }
        }
    }

    public void writeArray(
            Object  arrayValue,
            Type    componentType,
            int     length,
            ByteBuf writing)
    {
        if (isBasic(componentType)) {
            int        basicElementSize = StructHelper.findBasicSize(componentType);
            Basic<?>[] basicArray       = (Basic<?>[]) arrayValue;

            if (basicArray == null) writing.writeBytes(new byte[basicElementSize * length]);
            else                    writeBasicArray(basicArray, basicElementSize, length, writing);
        }
        else
        if (isStruct(componentType))
            writeStructArray(arrayNullDefault(arrayValue, componentType, length), componentType, length, writing);
        else
            throw new TypeJudgmentException(componentType);
    }

    public void writeBasicArray(
            Basic<?>[] basicArray,
            int        elementBytesSize,
            int        length,
            ByteBuf    writing)
    {
        for (int i = 0; i < length; i++) {
            if (i < basicArray.length) {
                Basic<?> basic = basicArray[i];
                if (basic == null) writing.writeBytes(new byte[elementBytesSize]);
                else               writing.writeBytes(basicArray[i].getBytes());
            }
            else writing.writeBytes(new byte[elementBytesSize]);
        }
    }

    public <T> void writeStructArray(
            T[]     structArray,
            Type    elementType,
            int     length,
            ByteBuf writing)
    {
        for (int i = 0; i < length; i++) {
            if (i < structArray.length)
                writeStruct(elementType, structNullDefault(structArray[i], elementType), writing);
            else
                writeStruct(elementType, newStruct(elementType), writing);
        }
    }

    public boolean isBasic(Type type)
    {
        if (type instanceof Class<?>        clazz)        return Basic.class.isAssignableFrom(clazz) && Basic.class != clazz;
        if (type instanceof TypeVariable<?> typeVariable) return isBasic(TypeUtil.getActualType(root, typeVariable));

        return false;
    }

    public boolean isStruct(
            Type type)
    {
        if (type instanceof Class<?>          clazz)             return STRUCT_DEFINITION_CACHE.containsKey(clazz);
        if (type instanceof ParameterizedType parameterizedType) return isStruct(parameterizedType.getRawType());
        if (type instanceof TypeVariable<?>   typeVariable)      return isStruct(TypeUtil.getActualType(root, typeVariable));

        return false;
    }

    //******************************************      public end       ***********************************************//

}
