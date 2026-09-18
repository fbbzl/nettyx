package org.fz.nettyx.serializer.schema;

import io.netty.buffer.ByteBuf;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.serializer.schema.codec.SchemaCodec;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * An immutable, zero-copy view over one fixed-length configured struct.
 * The caller must keep the source buffer valid and unchanged while using the view.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-16
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SchemaView
{

    SchemaCodec codec;
    Schema      struct;
    ByteBuf     source;
    int         startIndex;
    int         byteLength;

    SchemaView(SchemaCodec codec, Schema struct, ByteBuf source, int startIndex, int byteLength)
    {
        this.codec      = codec;
        this.struct     = struct;
        this.source     = source;
        this.startIndex = startIndex;
        this.byteLength = byteLength;
    }

    public int byteLength()
    {
        return byteLength;
    }

    public byte getByte(String fieldName)
    {
        requireKind(fieldName, "byte", SchemaField.Kind.BASIC);
        return (byte) requireInteger(fieldName, "byte", Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public short getShort(String fieldName)
    {
        requireKind(fieldName, "short", SchemaField.Kind.BASIC);
        return (short) requireInteger(fieldName, "short", Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public int getInt(String fieldName)
    {
        requireKind(fieldName, "int", SchemaField.Kind.BASIC);
        return (int) requireInteger(fieldName, "int", Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public long getLong(String fieldName)
    {
        requireKind(fieldName, "long", SchemaField.Kind.BASIC);
        return requireInteger(fieldName, "long", Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public float getFloat(String fieldName)
    {
        requireKind(fieldName, "float", SchemaField.Kind.BASIC);
        Object value = readValue(fieldName);
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
        requireKind(fieldName, "double", SchemaField.Kind.BASIC);
        Object value = readValue(fieldName);
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
        requireKind(fieldName, "boolean", SchemaField.Kind.BASIC);
        Object value = readValue(fieldName);
        if (value instanceof Boolean bool) return bool;
        throw typeError(fieldName, "boolean", value);
    }

    public String getString(String fieldName)
    {
        requireKind(fieldName, "String", SchemaField.Kind.CHAR);
        Object value = readValue(fieldName);
        if (value instanceof String string) return string;
        throw typeError(fieldName, "String", value);
    }

    public byte[] getBytes(String fieldName)
    {
        requireKind(fieldName, "byte[]", SchemaField.Kind.BYTES);
        Object value = readValue(fieldName);
        if (value instanceof byte[] bytes) return bytes;
        throw typeError(fieldName, "byte[]", value);
    }

    public List<?> getList(String fieldName)
    {
        requireKind(fieldName, "List", SchemaField.Kind.ARRAY);
        Object value = readValue(fieldName);
        if (value instanceof List<?> list) return list;
        throw typeError(fieldName, "List", value);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getStruct(String fieldName)
    {
        requireKind(fieldName, "Map<String, Object>", SchemaField.Kind.STRUCT);
        Object value = readValue(fieldName);
        if (value instanceof Map<?, ?> map) return (Map<String, Object>) map;
        throw typeError(fieldName, "Map<String, Object>", value);
    }

    private long requireInteger(String fieldName, String expectedType, long minimum, long maximum)
    {
        Object value = readValue(fieldName);
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

    private Object readValue(String fieldName)
    {
        ByteBuf reading = source.duplicate();
        reading.readerIndex(startIndex);
        for (SchemaField field : struct.fields()) {
            Object value = codec.readField(field, struct.byteOrder(), reading);
            if (field.name().equals(fieldName)) return value;
        }
        throw typeError(fieldName, "existing field", MissingField.INSTANCE);
    }

    private void requireKind(String fieldName, String expectedType, SchemaField.Kind expectedKind)
    {
        if (fieldName == null) throw typeError(null, expectedType, MissingField.INSTANCE);

        for (SchemaField field : struct.fields()) {
            if (!field.name().equals(fieldName)) continue;
            if (field.kind() == expectedKind) return;

            Object value = readValue(fieldName);
            throw typeError(fieldName, expectedType, value);
        }
        throw typeError(fieldName, expectedType, MissingField.INSTANCE);
    }

    private SerializeException typeError(String fieldName, String expectedType, Object actual)
    {
        return new SerializeException(
                "struct [" + struct.fqName() + "], field [" + fieldName + "], expected [" + expectedType
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
            return new BigDecimal(value).toBigIntegerExact();
        }
        catch (ArithmeticException error) {
            return null;
        }
    }

    private static BigInteger decimalValue(double value)
    {
        try {
            return new BigDecimal(value).toBigIntegerExact();
        }
        catch (ArithmeticException error) {
            return null;
        }
    }

    private enum MissingField
    {
        INSTANCE
    }
}
