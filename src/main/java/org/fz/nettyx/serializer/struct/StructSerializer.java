package org.fz.nettyx.serializer.struct;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.exception.TypeJudgmentException;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.struct.StructContext.StructDefinition;
import org.fz.nettyx.serializer.struct.annotation.ToArray;
import org.fz.nettyx.serializer.basic.Basic;
import org.fz.nettyx.serializer.struct.accessor.StructAccessor;
import org.fz.nettyx.serializer.struct.accessor.StructAccessorFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import static cn.hutool.core.util.ObjectUtil.defaultIfNull;
import static org.fz.nettyx.serializer.struct.StructContext.getStructDefinition;

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

    public static <B extends Basic<?>> B newBasic(
            Class<?>  basicClass,
            ByteOrder byteOrder,
            ByteBuf   buf)
    {
        try
        {
            return (B) StructContext.getBasicConstructor(basicClass).apply(buf, byteOrder);
        }
        catch (Exception instanceError)
        {
            Throwable cause = instanceError.getCause();
            if (instanceError instanceof TooLessBytesException tooLessBytes)
                throw tooLessBytes;
            if (cause instanceof TooLessBytesException tooLessBytes)
                throw tooLessBytes;
            else
                throw new SerializeException("basic [" + basicClass + "] instantiate failed..., buffer hex is: [" + ByteBufUtil.hexDump(buf) + "]", instanceError);
        }
    }

    public static <S> S newStruct(Type structType)
    {
        return (S) newStruct(structType, structConstructor(structType));
    }

    private static StructAccessor structConstructor(Type structType)
    {
        try
        {
            StructDefinition definition = getStructDefinition(structType);
            if (definition == null)
                throw new SerializeException("uncached struct type: " + structType);
            return StructAccessorFactory.get(definition);
        }
        catch (Exception instanceError)
        {
            throw new SerializeException("struct [" + structType + "] instantiate failed...", instanceError);
        }
    }

    private static Object newStruct(Type structType, StructAccessor accessor)
    {
        try
        {
            return accessor.newInstance();
        }
        catch (Exception instanceError)
        {
            throw new SerializeException("struct [" + structType + "] instantiate failed...", instanceError);
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
        writeStruct(root, struct, writing);
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
        return (S) requireAccessor(structType, "read").read(this, root, structType, byteBuf);
    }

    private static StructAccessor requireAccessor(Type structType, String operation)
    {
        StructDefinition structDef = getStructDefinition(structType);
        if (structDef == null)
            throw new StructDefinitionException("struct definition can not be null when " + operation
                                                + ", root type: [" + structType + "]");
        return StructAccessorFactory.get(structDef);
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
            B[] basics = ToArray.ToArrayHandler.newArray(elementType, length);
            for (int i = 0; i < length; i++) basics[i] = newBasic(elementType, byteOrder, byteBuf);
            return basics;
        }
        else {
            List<B> flexibleBasics = new ArrayList<>();
            while (byteBuf.isReadable()) {
                int readerIndex = byteBuf.readerIndex();
                flexibleBasics.add(newBasic(elementType, byteOrder, byteBuf));
                ensureReadProgress(elementType, byteBuf, readerIndex);
            }
            return flexibleBasics.toArray(ToArray.ToArrayHandler.newArray(elementType, flexibleBasics.size()));
        }
    }

    public <S> S[] readStructArray(
            Type    elementType,
            ByteBuf byteBuf,
            int     length,
            boolean flexible)
    {
        if (!flexible) {
            S[] structs = ToArray.ToArrayHandler.newArray(elementType, length);
            if (length == 0) return structs;

            StructAccessor accessor = requireAccessor(elementType, "read");
            for (int i = 0; i < length; i++)
                structs[i] = (S) accessor.read(this, root, elementType, byteBuf);
            return structs;
        }
        else {
            if (!byteBuf.isReadable()) return ToArray.ToArrayHandler.newArray(elementType, 0);

            StructAccessor accessor = requireAccessor(elementType, "read");
            List<S> flexibleStructs = new ArrayList<>();
            do {
                int readerIndex = byteBuf.readerIndex();
                flexibleStructs.add((S) accessor.read(this, root, elementType, byteBuf));
                ensureReadProgress(elementType, byteBuf, readerIndex);
            } while (byteBuf.isReadable());
            return flexibleStructs.toArray(ToArray.ToArrayHandler.newArray(elementType, flexibleStructs.size()));
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
        requireAccessor(structType, "write").write(this, root, structType, struct, writing);
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
            writeBasicArray((Basic<?>[]) arrayValue, StructContext.getBasicSize(componentType), length, writing, flexible, byteOrder);
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
            writing.writeZero(computeArrayByteLength(elementBytesSize, flexible ? 0 : length));
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
        T[] structArray = (T[]) defaultIfNull(structArrayValue, () -> ToArray.ToArrayHandler.newArray(elementType, (flexible ? 0 : length)));

        int writeCount = flexible ? structArray.length : length;
        if (writeCount <= 0) return;

        // Preserve construction errors when the first element is supplied by default.
        StructAccessor accessor = structArray.length == 0 || structArray[0] == null
                                  ? structConstructor(elementType)
                                  : requireAccessor(elementType, "write");
        for (int i = 0; i < writeCount; i++) {
            Object element = i < structArray.length ? structArray[i] : null;
            if (element == null) element = newStruct(elementType, accessor);
            accessor.write(this, root, elementType, element, writing);
        }
    }

    public boolean isBasic(Type type)
    {
        return switch (type)
        {
            case Class<?>        clazz   -> Basic.class.isAssignableFrom(clazz) && Basic.class != type;
            case TypeVariable<?> ignored -> {
                Type actual = StructContext.resolveType(root, type);
                yield !(actual instanceof TypeVariable<?>) && isBasic(actual);
            }
            default                      -> false;
        };
    }

    public boolean isStruct(Type type)
    {
        return switch (type)
        {
            case Class<?>          clazz             -> getStructDefinition(clazz) != null;
            case ParameterizedType parameterizedType -> isStruct(parameterizedType.getRawType());
            case TypeVariable<?> ignored -> {
                Type actual = StructContext.resolveType(root, type);
                yield !(actual instanceof TypeVariable<?>) && isStruct(actual);
            }
            default                                  -> false;
        };
    }

    private static void ensureReadProgress(Type elementType, ByteBuf byteBuf, int previousReaderIndex)
    {
        if (byteBuf.readerIndex() == previousReaderIndex) {
            throw new SerializeException("flexible array element did not consume any bytes, element type: [" + elementType + "]");
        }
    }

    private static int computeArrayByteLength(int elementBytesSize, int length)
    {
        try {
            return Math.multiplyExact(elementBytesSize, length);
        }
        catch (ArithmeticException error) {
            throw new SerializeException("array byte length exceeds the supported integer range, element size: ["
                                         + elementBytesSize + "], length: [" + length + "]", error);
        }
    }

    //******************************************      public end       ***********************************************//

}
