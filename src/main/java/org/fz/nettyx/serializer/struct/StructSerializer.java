package org.fz.nettyx.serializer.struct;

import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import org.fz.erwin.exception.Throws;
import org.fz.erwin.lang.TypeRefer;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.struct.StructSerializerContext.StructDefinition.StructField;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
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
        StructSerializerContext.StructDefinition structDef = getStructDefinition(structType);
        Throws.ifNull(structDef, () -> new StructDefinitionException("struct definition can not be null when read, root type: [" + structType + "]"));
        Object struct           = structDef.constructor().get();
        Type   actualStructType = TypeUtil.getActualType(root, structType);
        for (StructField field : structDef.fields()) {
            Type                  fieldType = field.type(actualStructType);
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
            int     length,
            boolean flexible)
    {
        if (isBasic(elementType))  return (T[]) readBasicArray((Class<? extends Basic<?>>) elementType, byteBuf, length, flexible);
        if (isStruct(elementType)) return readStructArray(elementType, byteBuf, length, flexible);
        else                       throw new TypeJudgmentException(elementType);
    }

    public <B extends Basic<?>> B[] readBasicArray(
            Class<?> elementType,
            ByteBuf  byteBuf,
            int      length,
            boolean  flexible)
    {
        if (!flexible) {
            B[] basics = newArray(elementType, length);
            for (int i = 0; i < length; i++) basics[i] = newBasic(elementType, byteBuf);
            return basics;
        }
        else {
            List<B> flexibleBasics = new ArrayList<>(8);
            while (byteBuf.isReadable()) flexibleBasics.add(newBasic(elementType, byteBuf));
            return flexibleBasics.toArray(newArray(elementType, flexibleBasics.size()));
        }
    }

    public <S> S[] readStructArray(
            Type    elementType,
            ByteBuf byteBuf,
            int     length,
            boolean flexible)
    {
        if (!flexible) {
            S[] structs = newArray(elementType, length);
            for (int i = 0; i < length; i++) structs[i] = readStruct(elementType, byteBuf);
            return structs;
        }
        else {
            List<S> flexibleStructs = new ArrayList<>(8);
            while (byteBuf.isReadable()) flexibleStructs.add(readStruct(elementType, byteBuf));
            return flexibleStructs.toArray(newArray(elementType, flexibleStructs.size()));
        }
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
        StructSerializerContext.StructDefinition structDef = getStructDefinition(structType);
        Throws.ifNull(structDef, () -> new StructDefinitionException("struct definition can not be null when write, " + "root type: [" + structType + "]"));
        Type actualStructType = TypeUtil.getActualType(root, structType);
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
            ByteBuf writing,
            boolean flexible)
    {
        if (isBasic(componentType)) {
            writeBasicArray((Basic<?>[]) arrayValue, StructHelper.findBasicSize(componentType), length, writing, flexible);
        }
        else
        if (isStruct(componentType))
            writeStructArray(arrayValue, componentType, length, writing, flexible);
        else
            throw new TypeJudgmentException(componentType);
    }

    public void writeBasicArray(
            Basic<?>[] basicArray,
            int        elementBytesSize,
            int        length,
            ByteBuf    writing,
            boolean    flexible)
    {
        if (basicArray == null) {
            writing.writeBytes(new byte[elementBytesSize * (flexible ? 0 : length)]);
            return;
        }

        for (int i = 0; i < (flexible ? basicArray.length : length); i++) {
            if (i < basicArray.length) {
                Basic<?> basic = basicArray[i];
                if (basic == null) writing.writeBytes(new byte[elementBytesSize]);
                else writing.writeBytes(basicArray[i].getBytes());
            }
            else writing.writeBytes(new byte[elementBytesSize]);
        }
    }

    public <T> void writeStructArray(
            Object  structArrayValue,
            Type    elementType,
            int     length,
            ByteBuf writing,
            boolean flexible)
    {
        T[] structArray = (T[]) defaultIfNull(structArrayValue, () -> newArray(elementType, (flexible ? 0 : length)));

        for (int i = 0; i < (flexible ? structArray.length : length); i++) {
            if (i < structArray.length)
                writeStruct(elementType, structNullDefault(structArray[i], elementType), writing);
            else
                writeStruct(elementType, newStruct(elementType), writing);
        }
    }

    public boolean isBasic(Type type)
    {
        if (type instanceof Class<?> clazz)
            return Basic.class.isAssignableFrom(clazz) && Basic.class != type;
        if (type instanceof TypeVariable<?>)
            return isBasic(TypeUtil.getActualType(root, type));

        return false;
    }

    public boolean isStruct(
            Type type)
    {
        if (type instanceof Class<?>)          return STRUCT_DEFINITION_CACHE.containsKey((Class<?>) type);
        if (type instanceof ParameterizedType) return isStruct(((ParameterizedType) type).getRawType());
        if (type instanceof TypeVariable<?>)   return isStruct(TypeUtil.getActualType(root, type));

        return false;
    }

    //******************************************      public end       ***********************************************//

}
