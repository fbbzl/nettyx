package org.fz.nettyx.serializer.configured;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.lang.reflect.Array;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * the configured struct serializer, parses binary data into {@link Map} by struct definitions
 * loaded from config files, no POJO and no annotation required
 *
 * @author fengbinbin
 * @since 2026-08-16
 */
@SuppressWarnings("unchecked")
public final class ConfiguredSerializer implements Serializer {

    private final StructConfigRegistry registry;
    private final ConfigStruct root;

    public ConfiguredSerializer(StructConfigRegistry registry, String structName)
    {
        this.registry = registry;
        this.root = registry.require(structName);
    }

    public ConfigStruct getStruct()
    {
        return root;
    }

    public static Map<String, Object> toStruct(StructConfigRegistry registry, String structName, ByteBuf byteBuf)
    {
        return new ConfiguredSerializer(registry, structName).doDeserialize(byteBuf);
    }

    public static void toByteBuf(StructConfigRegistry registry, String structName, Map<String, Object> structMap, ByteBuf writing)
    {
        new ConfiguredSerializer(registry, structName).doSerialize(structMap, writing);
    }

    //*************************************      working code splitter      ******************************************//

    @Override
    public <S> S doDeserialize(ByteBuf reading)
    {
        return (S) readStruct(root, reading);
    }

    @Override
    public <T> void doSerialize(T struct, ByteBuf writing)
    {
        if (struct != null && !(struct instanceof Map))
            throw new SerializeException("configured serializer only accepts java.util.Map, but got [" + struct.getClass().getName() + "]");

        writeStruct(root, (Map<String, Object>) struct, writing);
    }

    public Map<String, Object> readStruct(ConfigStruct struct, ByteBuf byteBuf)
    {
        Map<String, Object> structMap = new LinkedHashMap<>(struct.fields().size() * 2);
        for (ConfigField field : struct.fields())
            structMap.put(field.name(), readField(field, struct.byteOrder(), byteBuf));
        return structMap;
    }

    public Object readField(ConfigField field, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        return switch (field.kind()) {
            case BASIC  -> readBasicValue(field.basicType(), byteOrder, byteBuf);
            case CHAR   -> readCharString(field.length(), field.charset(), byteBuf);
            case BYTES  -> readBytes(field.length(), byteBuf);
            case STRUCT -> readStruct(registry.require(field.resolvedStructRef()), byteBuf);
            case ARRAY  -> readArray(field, byteOrder, byteBuf);
        };
    }

    public Object readArray(ConfigField field, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        List<Object> elements = new ArrayList<>();

        if (field.flexible()) {
            while (byteBuf.isReadable()) {
                int readerIndex = byteBuf.readerIndex();
                elements.add(readArrayElement(field, byteOrder, byteBuf));
                if (byteBuf.readerIndex() == readerIndex)
                    throw new SerializeException("flexible array element did not consume any bytes, field: [" + field.name() + "]");
            }
        }
        else {
            for (int i = 0; i < field.length(); i++)
                elements.add(readArrayElement(field, byteOrder, byteBuf));
        }

        return elements;
    }

    private Object readArrayElement(ConfigField field, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        return switch (field.elementKind()) {
            case BASIC  -> readBasicValue(field.basicType(), byteOrder, byteBuf);
            case STRUCT -> readStruct(registry.require(field.resolvedStructRef()), byteBuf);
        };
    }

    private Object readBasicValue(Class<? extends Basic<?>> basicType, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        return BasicTypeResolver.readBasic(basicType, byteOrder, byteBuf).value();
    }

    private String readCharString(int length, Charset charset, ByteBuf byteBuf)
    {
        byte[] bytes = readBytes(length, byteBuf);

        int end = bytes.length;
        while (end > 0 && bytes[end - 1] == 0) end--;

        return new String(bytes, 0, end, charset);
    }

    private byte[] readBytes(int length, ByteBuf byteBuf)
    {
        if (byteBuf.readableBytes() < length)
            throw new TooLessBytesException(length, byteBuf.readableBytes());

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    public void writeStruct(ConfigStruct struct, Map<String, Object> structMap, ByteBuf writing)
    {
        for (ConfigField field : struct.fields())
            writeField(field, structMap == null ? null : structMap.get(field.name()), struct.byteOrder(), writing);
    }

    public void writeField(ConfigField field, Object value, ByteOrder byteOrder, ByteBuf writing)
    {
        switch (field.kind()) {
            case BASIC  -> writeBasicValue(field.basicType(), value, byteOrder, writing);
            case CHAR   -> writeCharString(field.length(), (String) value, field.charset(), writing);
            case BYTES  -> writeBytes(field.length(), (byte[]) value, writing);
            case STRUCT -> writeStruct(registry.require(field.resolvedStructRef()), (Map<String, Object>) value, writing);
            case ARRAY  -> writeArray(field, value, byteOrder, writing);
        }
    }

    public void writeArray(ConfigField field, Object value, ByteOrder byteOrder, ByteBuf writing)
    {
        int valueCount = elementCount(value);
        int writeCount = field.flexible() ? valueCount : field.length();

        for (int i = 0; i < writeCount; i++) {
            Object element = i < valueCount ? elementAt(value, i) : null;
            writeArrayElement(field, element, byteOrder, writing);
        }
    }

    private void writeArrayElement(ConfigField field, Object element, ByteOrder byteOrder, ByteBuf writing)
    {
        switch (field.elementKind()) {
            case BASIC  -> writeBasicValue(field.basicType(), element, byteOrder, writing);
            case STRUCT -> writeStruct(registry.require(field.resolvedStructRef()), (Map<String, Object>) element, writing);
        }
    }

    private void writeBasicValue(Class<? extends Basic<?>> basicType, Object value, ByteOrder byteOrder, ByteBuf writing)
    {
        if (value == null) writing.writeZero(BasicTypeResolver.sizeOf(basicType));
        else               BasicTypeResolver.valueBasic(basicType, value).write(writing, byteOrder);
    }

    private void writeCharString(int length, String value, Charset charset, ByteBuf writing)
    {
        writeBytes(length, value == null ? null : value.getBytes(charset), writing);
    }

    private void writeBytes(int length, byte[] value, ByteBuf writing)
    {
        if (value == null) {
            writing.writeZero(length);
            return;
        }

        int writeLength = Math.min(value.length, length);
        writing.writeBytes(value, 0, writeLength);
        if (writeLength < length) writing.writeZero(length - writeLength);
    }

    private static int elementCount(Object value)
    {
        if (value == null)                  return 0;
        if (value instanceof Collection<?>) return ((Collection<?>) value).size();
        if (value.getClass().isArray())     return Array.getLength(value);

        throw new SerializeException("array field value must be a collection or an array, but got [" + value.getClass().getName() + "]");
    }

    private static Object elementAt(Object value, int index)
    {
        if (value instanceof List<?>)         return ((List<?>) value).get(index);
        if (value instanceof Collection<?>)   return ((Collection<?>) value).toArray()[index];
        return Array.get(value, index);
    }
}
