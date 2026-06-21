package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.core.util.TypeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCountUtil;
import org.fz.erwin.exception.Throws;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.struct.StructSerializerContext.StructDefinition;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.serializer.struct.generator.StructReaderWriterGenerator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
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

    private final Type root;

    public Type getType()
    {
        return root;
    }

    public StructSerializer(Type root)
    {
        if (root instanceof TypeReference<?> typeRefer)
            this.root = typeRefer.getType();
        else
            this.root = root;
    }

    public static <T> T toStruct(
            Type    root,
            ByteBuf byteBuf)
    {
        return new StructSerializer(root).doDeserialize(byteBuf);
    }

    public static <T> void toByteBuf(
            T       struct,
            ByteBuf writing)
    {
        toByteBuf(struct.getClass(), struct, writing);
    }

    public static <T> void toByteBuf(
            Type    root,
            T       struct,
            ByteBuf writing)
    {
        if (struct == null) throw new SerializeException("struct can not be null when write, root type: [" + root + "]");

        Type structType = root instanceof TypeReference<?> typeRefer ? typeRefer.getType() : root;
        switch (structType) {
            case Class<?>          clazz             -> new StructSerializer(clazz).doSerialize(struct, writing);
            case ParameterizedType parameterizedType -> new StructSerializer(parameterizedType).doSerialize(struct, writing);
            default                                  -> throw new TypeJudgmentException(structType);
        }
    }

    //*************************************      working code splitter      ******************************************//

    @Override
    public <S> S doDeserialize(ByteBuf byteBuf)
    {
        return readStruct(root, byteBuf);
    }

    @Override
    public void doSerialize(Object struct, ByteBuf writing)
    {
        try {
            writeStruct(root, struct, writing);
        } catch (Exception error) {
            ReferenceCountUtil.release(writing);
            throw error;
        }
    }

    public <B extends Basic<?>> B readBasic(
            Class<?>  basicType,
            ByteOrder byteOrder,
            ByteBuf   byteBuf)
    {
        return newBasic(basicType, byteOrder, byteBuf);
    }

    public <S> S readStruct(
            Type      structType,
            ByteBuf   byteBuf)
    {
        StructDefinition structDef = getStructDefinition(structType);
        Throws.ifNull(structDef, () -> new StructDefinitionException("struct definition can not be null when read, root type: [" + structType + "]"));

        return (S) StructReaderWriterGenerator.getReaderWriter(structDef).read(this, root, structType, byteBuf);
    }

    public  <T> T[] readArray(
            Type      elementType,
            ByteOrder byteOrder,
            ByteBuf   byteBuf,
            int       length,
            boolean   flexible)
    {
        if (isBasic(elementType))  return (T[]) readBasicArray((Class<? extends Basic<?>>) elementType, byteOrder, byteBuf, length, flexible);
        if (isStruct(elementType)) return readStructArray(elementType, byteBuf, length, flexible);
        else                       throw new TypeJudgmentException(elementType);
    }

    public <B extends Basic<?>> B[] readBasicArray(
            Class<?>  elementType,
            ByteOrder byteOrder,
            ByteBuf   byteBuf,
            int       length,
            boolean   flexible)
    {
        if (!flexible) {
            B[] basics = newArray(elementType, length);
            for (int i = 0; i < length; i++) basics[i] = newBasic(elementType, byteOrder, byteBuf);
            return basics;
        }
        else {
            List<B> flexibleBasics = new ArrayList<>();
            while (byteBuf.isReadable()) flexibleBasics.add(newBasic(elementType, byteOrder, byteBuf));
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
            List<S> flexibleStructs = new ArrayList<>();
            while (byteBuf.isReadable()) flexibleStructs.add(readStruct(elementType, byteBuf));
            return flexibleStructs.toArray(newArray(elementType, flexibleStructs.size()));
        }
    }

    public <B extends Basic<?>> void writeBasic(
            B         basicValue,
            ByteOrder byteOrder,
            ByteBuf   writingBuf) {
        if (basicValue.value() == null) writingBuf.writeZero(basicValue.size());
        else                            basicValue.write(writingBuf, byteOrder);
    }

    public <S> void writeStruct(
            Type    structType,
            S       struct,
            ByteBuf writing)
    {
        StructDefinition structDef = getStructDefinition(structType);
        Throws.ifNull(structDef, () -> new StructDefinitionException("struct definition can not be null when write, " + "root type: [" + structType + "]"));

        StructReaderWriterGenerator.getReaderWriter(structDef).write(this, root, structType, struct, writing);
    }

    public void writeArray(
            Object    arrayValue,
            Type      componentType,
            int       length,
            ByteBuf   writing,
            boolean   flexible,
            ByteOrder byteOrder)
    {
        if (isBasic(componentType))
            writeBasicArray((Basic<?>[]) arrayValue, StructHelper.findBasicSize(componentType), length, writing, flexible, byteOrder);
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
            boolean    flexible,
            ByteOrder  byteOrder)
    {
        if (basicArray == null) {
            writing.writeZero(elementBytesSize * (flexible ? 0 : length));
            return;
        }

        for (int i = 0; i < (flexible ? basicArray.length : length); i++) {
            if (i < basicArray.length) {
                Basic<?> basic = basicArray[i];
                if (basic == null || basic.value() == null) writing.writeZero(elementBytesSize);
                else                                        writeBasic(basic, byteOrder, writing);
            }
            else writing.writeZero(elementBytesSize);
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
                writeStruct(elementType, defaultIfNull(structArray[i], () -> newStruct(elementType)), writing);
            else
                writeStruct(elementType, newStruct(elementType), writing);
        }
    }

    public boolean isBasic(Type type)
    {
        return switch (type)
        {
            case Class<?>        clazz   -> Basic.class.isAssignableFrom(clazz) && Basic.class != type;
            case TypeVariable<?> ignored -> isBasic(TypeUtil.getActualType(root, type));
            default                      -> false;
        };
    }

    public boolean isStruct(Type type)
    {
        return switch (type)
        {
            case Class<?>          clazz             -> STRUCT_DEFINITION_CACHE.containsKey(clazz);
            case ParameterizedType parameterizedType -> isStruct(parameterizedType.getRawType());
            case TypeVariable<?>   ignored           -> isStruct(TypeUtil.getActualType(root, type));
            default                                  -> false;
        };
    }

    //******************************************      public end       ***********************************************//

}
