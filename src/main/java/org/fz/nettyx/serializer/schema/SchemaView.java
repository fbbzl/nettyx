package org.fz.nettyx.serializer.schema;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.exception.SerializeException;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An immutable, zero-copy view over a fixed-length schema.
 * The caller must keep the source buffer valid and unchanged while using the view.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-16
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SchemaView
{

    SchemaSerializer     serializer;
    Schema               schema;
    ByteBuf              source;
    Map<String, Integer> fieldIndexes;
    int[]                fieldOffsets;
    @Getter
    @Accessors(fluent = true)
    int byteLength;

    SchemaView(SchemaSerializer serializer, SchemaContext context, ByteBuf source, int startIndex, int byteLength)
    {
        this.serializer   = serializer;
        this.schema       = serializer.getSchema();
        this.source       = source;
        this.byteLength   = byteLength;
        this.fieldIndexes = new HashMap<>();
        List<Schema.SchemaField> fields = schema.fields();
        this.fieldOffsets = new int[fields.size()];
        int offset = startIndex;
        for (int i = 0; i < fields.size(); i++) {
            this.fieldIndexes.putIfAbsent(fields.get(i).name(), i);
            this.fieldOffsets[i] = offset;
            int fieldSize = context.fixedFieldSizeOf(fields.get(i));
            if (fieldSize < 0)
                throw new SerializeException("zero-copy view only supports fixed-length struct: [" + schema.fqName() + "]");
            offset += fieldSize;
        }
    }

    public byte getByte(String fieldName)
    {
        int fieldIndex = requireKind(fieldName, "byte", Schema.SchemaField.Kind.BASIC);
        return (byte) requireInteger(fieldIndex, fieldName, "byte", Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public short getShort(String fieldName)
    {
        int fieldIndex = requireKind(fieldName, "short", Schema.SchemaField.Kind.BASIC);
        return (short) requireInteger(fieldIndex, fieldName, "short", Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public int getInt(String fieldName)
    {
        int fieldIndex = requireKind(fieldName, "int", Schema.SchemaField.Kind.BASIC);
        return (int) requireInteger(fieldIndex, fieldName, "int", Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public long getLong(String fieldName)
    {
        int fieldIndex = requireKind(fieldName, "long", Schema.SchemaField.Kind.BASIC);
        return requireInteger(fieldIndex, fieldName, "long", Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public float getFloat(String fieldName)
    {
        int fieldIndex = requireKind(fieldName, "float", Schema.SchemaField.Kind.BASIC);
        Object value = readValue(fieldIndex);
        if (value instanceof Float number) return number;
        if (value instanceof Double) throw typeError(fieldName, "float", value);

        BigInteger integer = requireIntegralValue(fieldName, "float", value);
        float converted = integer.floatValue();
        if (!Float.isFinite(converted) || !integer.equals(decimalValue(converted)))
            throw typeError(fieldName, "float", value);
        return converted;
    }

    public double getDouble(String fieldName)
    {
        int fieldIndex = requireKind(fieldName, "double", Schema.SchemaField.Kind.BASIC);
        Object value = readValue(fieldIndex);
        if (value instanceof Double number) return number;
        if (value instanceof Float number) return number.doubleValue();

        BigInteger integer = requireIntegralValue(fieldName, "double", value);
        double converted = integer.doubleValue();
        if (!Double.isFinite(converted) || !integer.equals(decimalValue(converted)))
            throw typeError(fieldName, "double", value);
        return converted;
    }

    public boolean getBoolean(String fieldName)
    {
        int fieldIndex = requireKind(fieldName, "boolean", Schema.SchemaField.Kind.BASIC);
        Object value = readValue(fieldIndex);
        if (value instanceof Boolean bool) return bool;
        throw typeError(fieldName, "boolean", value);
    }

    public String getString(String fieldName)
    {
        int fieldIndex = requireKind(fieldName, "String", Schema.SchemaField.Kind.CHAR);
        return (String) readValue(fieldIndex);
    }

    public byte[] getBytes(String fieldName)
    {
        int fieldIndex = requireKind(fieldName, "byte[]", Schema.SchemaField.Kind.BYTES);
        return (byte[]) readValue(fieldIndex);
    }

    public List<?> getList(String fieldName)
    {
        int fieldIndex = requireKind(fieldName, "List", Schema.SchemaField.Kind.ARRAY);
        return (List<?>) readValue(fieldIndex);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getMap(String fieldName)
    {
        int fieldIndex = requireKind(fieldName, "Map<String, Object>", Schema.SchemaField.Kind.SCHEMA);
        return (Map<String, Object>) readValue(fieldIndex);
    }

    private long requireInteger(int fieldIndex, String fieldName, String expectedType, long minimum, long maximum)
    {
        Object value = readValue(fieldIndex);
        BigInteger integer = requireIntegralValue(fieldName, expectedType, value);
        if (integer.compareTo(BigInteger.valueOf(minimum)) < 0
            || integer.compareTo(BigInteger.valueOf(maximum)) > 0)
            throw typeError(fieldName, expectedType, value);
        return integer.longValue();
    }

    private BigInteger requireIntegralValue(String fieldName, String expectedType, Object value)
    {
        if (value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)
            return BigInteger.valueOf(((Number) value).longValue());
        if (value instanceof BigInteger integer) return integer;
        throw typeError(fieldName, expectedType, value);
    }

    private Object readValue(int fieldIndex)
    {
        ByteBuf reading = source.duplicate();
        reading.readerIndex(fieldOffsets[fieldIndex]);
        return serializer.readField(schema.fields().get(fieldIndex), schema.byteOrder(), reading);
    }

    private int requireKind(String fieldName, String expectedType, Schema.SchemaField.Kind expectedKind)
    {
        if (fieldName == null) throw typeError(null, expectedType, MissingField.INSTANCE);

        Integer fieldIndex = fieldIndexes.get(fieldName);
        if (fieldIndex != null) {
            Schema.SchemaField field = schema.fields().get(fieldIndex);
            if (field.kind() == expectedKind) return fieldIndex;

            Object value = readValue(fieldIndex);
            throw typeError(fieldName, expectedType, value);
        }
        throw typeError(fieldName, expectedType, MissingField.INSTANCE);
    }

    private SerializeException typeError(String fieldName, String expectedType, Object actual)
    {
        return new SerializeException(
                "struct [" + schema.fqName() + "], field [" + fieldName + "], expected [" + expectedType
                + "], actual [" + actualType(actual) + "]");
    }

    private static String actualType(Object value)
    {
        if (value == MissingField.INSTANCE) return "missing";
        if (value == null) return "null";
        return value.getClass().getTypeName();
    }

    private static BigInteger decimalValue(float value)
    {
        try {
            return BigDecimal.valueOf(value).toBigIntegerExact();
        }
        catch (ArithmeticException error) {
            return null;
        }
    }

    private static BigInteger decimalValue(double value)
    {
        try {
            return BigDecimal.valueOf(value).toBigIntegerExact();
        }
        catch (ArithmeticException error) {
            return null;
        }
    }

    /**
     * Marks a field lookup that found no matching schema field.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-09-18
     */
    private enum MissingField
    {
        INSTANCE
    }
}
