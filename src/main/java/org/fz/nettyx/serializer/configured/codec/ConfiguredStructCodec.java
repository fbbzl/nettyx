package org.fz.nettyx.serializer.configured.codec;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.configured.ConfigField;
import org.fz.nettyx.serializer.configured.ConfigStruct;
import org.fz.nettyx.serializer.configured.StructConfigRegistry;
import org.fz.nettyx.serializer.configured.type.BasicTypeResolver;

import java.lang.reflect.Array;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public final class ConfiguredStructCodec {

    private final StructConfigRegistry registry;
    private final Map<ConfigStruct, Integer> fixedSizeCache = new HashMap<>();

    public ConfiguredStructCodec(StructConfigRegistry registry)
    {
        this.registry = registry;
    }

    public Map<String, Object> newReusableStruct(ConfigStruct struct)
    {
        return new ConfigStructMap(struct);
    }

    public Map<String, Object> readStruct(ConfigStruct struct, ByteBuf byteBuf)
    {
        List<ConfigField> fields = struct.fields();
        ConfigStructMap structMap = new ConfigStructMap(struct);
        for (int i = 0; i < fields.size(); i++) {
            ConfigField field = fields.get(i);
            structMap.put(i, readField(field, struct.byteOrder(), byteBuf));
        }
        return structMap;
    }

    public void readStructInto(ConfigStruct struct, ByteBuf byteBuf, Map<String, Object> target)
    {
        if (target instanceof ConfigStructMap reusable) {
            if (!reusable.belongsTo(struct))
                throw new SerializeException("reusable target belongs to a different configured struct");
            readStructInto(struct, reusable, byteBuf);
            return;
        }

        target.clear();
        target.putAll(readStruct(struct, byteBuf));
    }

    private void readStructInto(ConfigStruct struct, ConfigStructMap target, ByteBuf byteBuf)
    {
        List<ConfigField> fields = struct.fields();
        for (int i = 0; i < fields.size(); i++) {
            ConfigField field = fields.get(i);
            target.put(i, readFieldInto(field, struct.byteOrder(), byteBuf, target, i));
        }
    }

    public Object readField(ConfigField field, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        return switch (field.kind()) {
            case BASIC  -> readBasicValue(field, byteOrder, byteBuf);
            case CHAR   -> readCharString(field.length(), field.charset(), byteBuf);
            case BYTES  -> readBytes(field.length(), byteBuf);
            case STRUCT -> readStruct(registry.require(field.resolvedStructRef()), byteBuf);
            case ARRAY  -> readArray(field, byteOrder, byteBuf);
        };
    }

    private Object readFieldInto(
            ConfigField field,
            ByteOrder byteOrder,
            ByteBuf byteBuf,
            ConfigStructMap target,
            int index)
    {
        Object previous = target.valueAt(index);
        return switch (field.kind()) {
            case BASIC  -> readBasicValue(field, byteOrder, byteBuf);
            case CHAR   -> readCharStringInto(field.length(), field.charset(), byteBuf, target, index, (String) previous);
            case BYTES  -> readBytesInto(field.length(), byteBuf, (byte[]) previous);
            case STRUCT -> readNestedStructInto(field.resolvedStructRef(), byteBuf, previous);
            case ARRAY  -> readArrayInto(field, byteOrder, byteBuf, previous);
        };
    }

    public Object readArray(ConfigField field, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        List<Object> elements = new ArrayList<>(field.flexible() ? 10 : field.length());

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

    private List<Object> readArrayInto(ConfigField field, ByteOrder byteOrder, ByteBuf byteBuf, Object previous)
    {
        ArrayList<Object> elements = previous instanceof ArrayList<?> existing
                                     ? (ArrayList<Object>) existing
                                     : new ArrayList<>(field.flexible() ? 10 : field.length());
        int previousSize = elements.size();
        int count = field.flexible() ? -1 : field.length();
        int i = 0;
        for (; count < 0 ? byteBuf.isReadable() : i < count; i++) {
            int readerIndex = byteBuf.readerIndex();
            Object previousElement = i < previousSize ? elements.get(i) : null;
            Object element = readArrayElementInto(field, byteOrder, byteBuf, previousElement);
            if (count < 0 && byteBuf.readerIndex() == readerIndex)
                throw new SerializeException("flexible array element did not consume any bytes, field: [" + field.name() + "]");
            if (i < previousSize) elements.set(i, element);
            else                  elements.add(element);
        }
        if (i < previousSize) elements.subList(i, previousSize).clear();
        return elements;
    }

    private Object readArrayElement(ConfigField field, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        return switch (field.elementKind()) {
            case BASIC  -> readBasicValue(field, byteOrder, byteBuf);
            case STRUCT -> readStruct(registry.require(field.resolvedStructRef()), byteBuf);
        };
    }

    private Object readArrayElementInto(ConfigField field, ByteOrder byteOrder, ByteBuf byteBuf, Object previous)
    {
        return switch (field.elementKind()) {
            case BASIC  -> readBasicValue(field, byteOrder, byteBuf);
            case STRUCT -> readNestedStructInto(field.resolvedStructRef(), byteBuf, previous);
        };
    }

    private Object readBasicValue(ConfigField field, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        return field.readBasicValue(byteBuf, byteOrder);
    }

    private String readCharString(int length, Charset charset, ByteBuf byteBuf)
    {
        if (byteBuf.readableBytes() < length)
            throw new TooLessBytesException(length, byteBuf.readableBytes());

        int readerIndex = byteBuf.readerIndex();
        int end = readerIndex + length;
        while (end > readerIndex && byteBuf.getByte(end - 1) == 0) end--;

        String value;
        if (byteBuf.hasArray()) {
            value = new String(byteBuf.array(), byteBuf.arrayOffset() + readerIndex, end - readerIndex, charset);
            byteBuf.skipBytes(length);
        }
        else {
            byte[] bytes = readBytes(length, byteBuf);
            value = new String(bytes, 0, end - readerIndex, charset);
        }

        return value;
    }

    private String readCharStringInto(
            int length,
            Charset charset,
            ByteBuf byteBuf,
            ConfigStructMap target,
            int index,
            String previous)
    {
        if (byteBuf.readableBytes() < length)
            throw new TooLessBytesException(length, byteBuf.readableBytes());

        int readerIndex = byteBuf.readerIndex();
        byte[] cachedBytes = target.charBuffer(index, length);
        boolean unchanged = previous != null;
        for (int i = 0; i < length; i++) {
            byte value = byteBuf.getByte(readerIndex + i);
            if (cachedBytes[i] != value) unchanged = false;
            cachedBytes[i] = value;
        }
        byteBuf.skipBytes(length);
        if (unchanged) return previous;

        int end = cachedBytes.length;
        while (end > 0 && cachedBytes[end - 1] == 0) end--;
        return new String(cachedBytes, 0, end, charset);
    }

    private byte[] readBytes(int length, ByteBuf byteBuf)
    {
        if (byteBuf.readableBytes() < length)
            throw new TooLessBytesException(length, byteBuf.readableBytes());

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    private byte[] readBytesInto(int length, ByteBuf byteBuf, byte[] previous)
    {
        if (byteBuf.readableBytes() < length)
            throw new TooLessBytesException(length, byteBuf.readableBytes());

        byte[] bytes = previous != null && previous.length == length ? previous : new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    private Map<String, Object> readNestedStructInto(String structName, ByteBuf byteBuf, Object previous)
    {
        ConfigStruct nestedStruct = registry.require(structName);
        ConfigStructMap nested = previous instanceof ConfigStructMap reusable && reusable.belongsTo(nestedStruct)
                                  ? reusable
                                  : new ConfigStructMap(nestedStruct);
        readStructInto(nestedStruct, nested, byteBuf);
        return nested;
    }

    public synchronized int fixedSizeOf(ConfigStruct struct)
    {
        Integer cached = fixedSizeCache.get(struct);
        if (cached != null) return cached;

        int computed = computeFixedSize(struct);
        fixedSizeCache.put(struct, computed);
        return computed;
    }

    private int computeFixedSize(ConfigStruct struct)
    {
        int size = 0;
        for (ConfigField field : struct.fields()) {
            int fieldSize = switch (field.kind()) {
                case BASIC -> BasicTypeResolver.sizeOf(field.basicType());
                case CHAR, BYTES -> field.length();
                case STRUCT -> fixedSizeOf(registry.require(field.resolvedStructRef()));
                case ARRAY -> fixedArraySize(field);
            };
            if (fieldSize < 0) return -1;
            try {
                size = Math.addExact(size, fieldSize);
            }
            catch (ArithmeticException error) {
                throw new SerializeException("fixed struct size exceeds supported integer range: [" + struct.fqName() + "]", error);
            }
        }
        return size;
    }

    private int fixedArraySize(ConfigField field)
    {
        if (field.flexible()) return -1;

        int elementSize = switch (field.elementKind()) {
            case BASIC -> BasicTypeResolver.sizeOf(field.basicType());
            case STRUCT -> fixedSizeOf(registry.require(field.resolvedStructRef()));
        };
        if (elementSize < 0) return -1;
        try {
            return Math.multiplyExact(elementSize, field.length());
        }
        catch (ArithmeticException error) {
            throw new SerializeException("fixed array size exceeds supported integer range: field [" + field.name() + "]", error);
        }
    }

    public void writeStruct(ConfigStruct struct, Map<String, Object> structMap, ByteBuf writing)
    {
        List<ConfigField> fields = struct.fields();
        if (structMap instanceof ConfigStructMap configuredMap && configuredMap.belongsTo(struct)) {
            for (int i = 0; i < fields.size(); i++)
                writeField(fields.get(i), configuredMap.valueAt(i), struct.byteOrder(), writing);
            return;
        }

        for (ConfigField field : fields)
            writeField(field, structMap == null ? null : structMap.get(field.name()), struct.byteOrder(), writing);
    }

    public void writeField(ConfigField field, Object value, ByteOrder byteOrder, ByteBuf writing)
    {
        switch (field.kind()) {
            case BASIC  -> writeBasicValue(field, value, byteOrder, writing);
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
        Iterator<?> iterator = value instanceof Collection<?> collection && !(value instanceof List<?>)
                               ? collection.iterator()
                               : null;

        for (int i = 0; i < writeCount; i++) {
            Object element = i < valueCount
                             ? iterator == null ? elementAt(value, i) : iterator.next()
                             : null;
            writeArrayElement(field, element, byteOrder, writing);
        }
    }

    private void writeArrayElement(ConfigField field, Object element, ByteOrder byteOrder, ByteBuf writing)
    {
        switch (field.elementKind()) {
            case BASIC  -> writeBasicValue(field, element, byteOrder, writing);
            case STRUCT -> writeStruct(registry.require(field.resolvedStructRef()), (Map<String, Object>) element, writing);
        }
    }

    private void writeBasicValue(ConfigField field, Object value, ByteOrder byteOrder, ByteBuf writing)
    {
        if (value == null) writing.writeZero(BasicTypeResolver.sizeOf(field.basicType()));
        else               field.writeBasicValue(writing, byteOrder, value);
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
        if (value instanceof List<?>) return ((List<?>) value).get(index);
        return Array.get(value, index);
    }
}
