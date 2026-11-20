package org.fz.nettyx.serializer.schema;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.Serializer;
import org.fz.nettyx.serializer.schema.Schema.SchemaField;
import org.fz.nettyx.serializer.schema.type.BasicTypeResolver;

import java.lang.reflect.Array;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * Serializes values and reads binary data into a {@link Map} using a schema.
 * loaded from config files, no POJO and no annotation required
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-16
 */
@SuppressWarnings("unchecked")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SchemaSerializer implements Serializer
{

    SchemaContext context;
    Schema        root;

    SchemaSerializer(SchemaContext context, String schemaName)
    {
        this.context = context;
        this.root    = context.require(schemaName);
    }

    public Schema getSchema()
    {
        return root;
    }

    public static Map<String, Object> toMap(SchemaContext context, String schemaName, ByteBuf byteBuf)
    {
        return context.serializer(schemaName).doDeserialize(byteBuf);
    }

    public static void toByteBuf(SchemaContext context, String schemaName, Map<String, Object> values, ByteBuf writing)
    {
        context.serializer(schemaName).doSerialize(values, writing);
    }

    @Override
    public <S> S doDeserialize(ByteBuf reading)
    {
        return (S) readSchema(root, reading);
    }

    public SchemaView view(ByteBuf source)
    {
        Objects.requireNonNull(source, "source");

        int byteLength = context.fixedSizeOf(root);
        if (byteLength < 0)
            throw new SerializeException("zero-copy view only supports fixed-length struct: [" + root.fqName() + "]");
        if (source.readableBytes() < byteLength)
            throw new TooLessBytesException(byteLength, source.readableBytes());

        SchemaView view = new SchemaView(this, context, source, source.readerIndex(), byteLength);
        source.skipBytes(byteLength);
        return view;
    }

    @Override
    public <T> void doSerialize(T data, ByteBuf writing)
    {
        if (data != null && !(data instanceof Map))
            throw new SerializeException("configured serializer only accepts java.util.Map, but got [" + data.getClass().getName() + "]");

        writeSchema(root, (Map<String, Object>) data, writing);
    }

    private Map<String, Object> readSchema(Schema schema, ByteBuf byteBuf)
    {
        Map<String, Object> values = LinkedHashMap.newLinkedHashMap(schema.fields().size());
        for (SchemaField field : schema.fields())
            values.put(field.name(), readField(field, schema.byteOrder(), byteBuf));
        return values;
    }

    Object readField(SchemaField field, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        return switch (field.kind()) {
            case BASIC -> field.readBasicValue(byteBuf, byteOrder);
            case CHAR -> readCharString(field.length(), field.charset(), byteBuf);
            case BYTES -> readBytes(field.length(), byteBuf);
            case SCHEMA -> readSchema(context.require(field.resolvedSchemaRef()), byteBuf);
            case ARRAY -> readArray(field, byteOrder, byteBuf);
        };
    }

    private List<Object> readArray(SchemaField field, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        List<Object> elements = new ArrayList<>(field.flexible() ? 10 : field.length());

        if (field.flexible()) {
            while (byteBuf.isReadable()) {
                int readerIndex = byteBuf.readerIndex();
                elements.add(readArrayElement(field, byteOrder, byteBuf));
                if (byteBuf.readerIndex() == readerIndex)
                    throw new SerializeException("flexible array element did not consume any bytes, field: [" + field.name() + "]");
            }
        } else {
            for (int i = 0; i < field.length(); i++)
                 elements.add(readArrayElement(field, byteOrder, byteBuf));
        }

        return elements;
    }

    private Object readArrayElement(SchemaField field, ByteOrder byteOrder, ByteBuf byteBuf)
    {
        return switch (field.elementKind()) {
            case BASIC -> field.readBasicValue(byteBuf, byteOrder);
            case SCHEMA -> readSchema(context.require(field.resolvedSchemaRef()), byteBuf);
        };
    }

    private String readCharString(int length, Charset charset, ByteBuf byteBuf)
    {
        if (byteBuf.readableBytes() < length)
            throw new TooLessBytesException(length, byteBuf.readableBytes());

        int readerIndex = byteBuf.readerIndex();
        int end         = readerIndex + length;
        while (end > readerIndex && byteBuf.getByte(end - 1) == 0) end--;

        String value;
        if (byteBuf.hasArray()) {
            value = new String(byteBuf.array(), byteBuf.arrayOffset() + readerIndex, end - readerIndex, charset);
            byteBuf.skipBytes(length);
        } else {
            byte[] bytes = readBytes(length, byteBuf);
            value = new String(bytes, 0, end - readerIndex, charset);
        }

        return value;
    }

    private byte[] readBytes(int length, ByteBuf byteBuf)
    {
        if (byteBuf.readableBytes() < length)
            throw new TooLessBytesException(length, byteBuf.readableBytes());

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        return bytes;
    }

    private void writeSchema(Schema schema, Map<String, Object> values, ByteBuf writing)
    {
        for (SchemaField field : schema.fields())
            writeField(field, values == null ? null : values.get(field.name()), schema.byteOrder(), writing);
    }

    void writeField(SchemaField field, Object value, ByteOrder byteOrder, ByteBuf writing)
    {
        if (field.kind() == SchemaField.Kind.BASIC) writeBasicValue(field, value, byteOrder, writing);
        else if (field.kind() == SchemaField.Kind.CHAR) writeCharString(field.length(), (String) value, field.charset(), writing);
        else if (field.kind() == SchemaField.Kind.BYTES) writeBytes(field.length(), (byte[]) value, writing);
        else if (field.kind() == SchemaField.Kind.SCHEMA)
            writeSchema(context.require(field.resolvedSchemaRef()), (Map<String, Object>) value, writing);
        else writeArray(field, value, byteOrder, writing);
    }

    private void writeArray(SchemaField field, Object value, ByteOrder byteOrder, ByteBuf writing)
    {
        int valueCount = elementCount(value);
        int writeCount = field.flexible() ? valueCount : field.length();
        Iterator<?> iterator = value instanceof Collection<?> collection
                               && !(collection instanceof List<?> && collection instanceof RandomAccess)
                               ? collection.iterator()
                               : null;

        for (int i = 0; i < writeCount; i++) {
            Object element = i < valueCount
                             ? iterator == null ? elementAt(value, i) : iterator.next()
                             : null;
            writeArrayElement(field, element, byteOrder, writing);
        }
    }

    private void writeArrayElement(SchemaField field, Object element, ByteOrder byteOrder, ByteBuf writing)
    {
        if (field.elementKind() == SchemaField.ElementKind.BASIC) writeBasicValue(field, element, byteOrder, writing);
        else writeSchema(context.require(field.resolvedSchemaRef()), (Map<String, Object>) element, writing);
    }

    private void writeBasicValue(SchemaField field, Object value, ByteOrder byteOrder, ByteBuf writing)
    {
        if (value == null) writing.writeZero(BasicTypeResolver.sizeOf(field.basicType()));
        else field.writeBasicValue(writing, byteOrder, value);
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
        if (value == null) return 0;
        if (value instanceof Collection<?>) return ((Collection<?>) value).size();
        if (value.getClass().isArray()) return Array.getLength(value);

        throw new SerializeException("array field value must be a collection or an array, but got [" + value.getClass().getName() + "]");
    }

    private static Object elementAt(Object value, int index)
    {
        if (value instanceof List<?>) return ((List<?>) value).get(index);
        return Array.get(value, index);
    }
}
