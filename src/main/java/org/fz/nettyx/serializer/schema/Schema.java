package org.fz.nettyx.serializer.schema;

import cn.hutool.core.util.ObjectUtil;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.serializer.basic.Basic;
import org.fz.nettyx.serializer.schema.type.BasicTypeResolver;
import org.fz.nettyx.serializer.schema.type.BasicTypeResolver.BasicValueReader;
import org.fz.nettyx.serializer.schema.type.BasicTypeResolver.BasicValueWriter;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.fz.nettyx.serializer.schema.Schema.SchemaField.ElementKind.BASIC;
import static org.fz.nettyx.serializer.schema.Schema.SchemaField.ElementKind.SCHEMA;
import static org.fz.nettyx.serializer.schema.Schema.SchemaField.Kind.ARRAY;

/**
 * Defines the binary layout identified by {@code namespace.name}.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-16
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class Schema
{

    String            namespace;
    String            name;
    ByteOrder         byteOrder;
    List<SchemaField> fields;
    String[]          fieldNames;

    public Schema(String namespace, String name, ByteOrder byteOrder, List<SchemaField> fields)
    {
        this.namespace = namespace;
        this.name      = name;
        this.byteOrder = byteOrder;
        this.fields    = List.copyOf(fields);
        validateFieldNames();
        this.fieldNames = this.fields.stream().map(SchemaField::name).toArray(String[]::new);
    }

    private void validateFieldNames()
    {
        Set<String> names = HashSet.newHashSet(fields.size());
        for (SchemaField field : fields) {
            if (!names.add(field.name()))
                throw new StructDefinitionException("duplicated field name [" + field.name() + "] in struct [" + fqName() + "]");
        }
    }

    public String[] fieldNames()
    {
        return Arrays.copyOf(fieldNames, fieldNames.length);
    }

    /**
     * fully qualified name like mybatis mapper statement: {@code namespace.name}
     */
    public String fqName()
    {
        return namespace + "." + name;
    }

    /**
     * Defines a field within a schema.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-08-16
     */
    @Getter
    @Accessors(fluent = true)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    public static class SchemaField
    {

        /**
         * field kind
         *
         * @author fengbinbin
         * @version 1.0
         * @since 2026-09-14
         */
        public enum Kind
        {BASIC, CHAR, BYTES, SCHEMA, ARRAY}

        /**
         * array element kind
         *
         * @author fengbinbin
         * @version 1.0
         * @since 2026-09-14
         */
        public enum ElementKind
        {BASIC, SCHEMA}

        Kind                      kind;
        String                    name;
        Class<? extends Basic<?>> basicType;
        Integer                   length;
        boolean                   flexible;
        String                    schemaRef;
        ElementKind               elementKind;
        Charset                   charset;

        @Getter(AccessLevel.PACKAGE)
        BasicValueReader basicValueReader;
        @Getter(AccessLevel.PACKAGE)
        BasicValueWriter basicValueWriter;
        @NonFinal
        String           resolvedSchemaRef;

        private SchemaField(
                Kind kind,
                String name,
                Class<? extends Basic<?>> basicType,
                Integer length,
                boolean flexible,
                String schemaRef,
                ElementKind elementKind,
                Charset charset)
        {
            this.kind             = kind;
            this.name             = name;
            this.basicType        = basicType;
            this.basicValueReader = basicType == null ? null : BasicTypeResolver.valueReaderFor(basicType);
            this.basicValueWriter = basicType == null ? null : BasicTypeResolver.valueWriterFor(basicType);
            this.length           = length;
            this.flexible         = flexible;
            this.schemaRef        = schemaRef;
            this.elementKind      = elementKind;
            this.charset          = charset;
        }

        public static SchemaField fromConfig(
                String name,
                String type,
                String schemaRef,
                String length,
                String array,
                String charset,
                String schemaName,
                String location)
        {
            validateFieldAttributes(name, type, schemaRef, length, array, charset, schemaName, location);

            if (schemaRef != null) {
                if (array == null) return schemaField(name, schemaRef);
                Integer arrayLength = parseArrayLength(array, name, schemaName, location);
                return schemaArray(name, schemaRef, arrayLength, arrayLength == null);
            }

            return switch (type) {
                case "char" -> charField(name, parseLength(length, name, schemaName, location),
                                         parseCharset(charset, name, schemaName, location));
                case "byte" -> bytesField(name, parseLength(length, name, schemaName, location));
                default -> {
                    Class<? extends Basic<?>> basicType = BasicTypeResolver.resolve(type);
                    if (array == null) yield basicField(name, basicType);
                    Integer arrayLength = parseArrayLength(array, name, schemaName, location);
                    yield basicArray(name, basicType, arrayLength, arrayLength == null);
                }
            };
        }

        private static void validateFieldAttributes(
                String name,
                String type,
                String schemaRef,
                String length,
                String array,
                String charset,
                String schemaName,
                String location)
        {
            boolean hasSchemaRef = schemaRef != null;
            if ((type != null) == hasSchemaRef)
                throw definitionError("field must declare exactly one of [type] or [struct]", name, schemaName, location);

            if (hasSchemaRef) {
                if (length != null) throw definitionError("struct field can not declare [length]", name, schemaName, location);
                if (charset != null) throw definitionError("struct field can not declare [charset]", name, schemaName, location);
                return;
            }

            if (charset != null && !"char".equals(type))
                throw definitionError("only char field can declare [charset]", name, schemaName, location);

            switch (type) {
                case "char", "byte" -> {
                    if (length == null) throw definitionError(type + " field must declare [length]", name, schemaName, location);
                    if (array != null) throw definitionError(type + " field can not declare [array]", name, schemaName, location);
                }
                default -> {
                    if (length != null) throw definitionError("basic field can not declare [length]", name, schemaName, location);
                }
            }
        }

        private static Charset parseCharset(String charset, String fieldName, String schemaName, String location)
        {
            if (charset == null) return null;

            try {
                return Charset.forName(charset);
            }
            catch (Exception charsetError) {
                throw definitionError("unknown [charset] value [" + charset + "]", fieldName, schemaName, location);
            }
        }

        private static int parseLength(String length, String fieldName, String schemaName, String location)
        {
            try {
                int value = Integer.parseInt(length.trim());
                if (value <= 0) throw new NumberFormatException("length must be positive");
                return value;
            }
            catch (NumberFormatException formatError) {
                throw definitionError("invalid [length] value [" + length + "]", fieldName, schemaName, location);
            }
        }

        private static Integer parseArrayLength(String array, String fieldName, String schemaName, String location)
        {
            if ("*".equals(array.trim())) return null;

            try {
                int value = Integer.parseInt(array.trim());
                if (value <= 0) throw new NumberFormatException("array length must be positive");
                return value;
            }
            catch (NumberFormatException formatError) {
                throw definitionError("invalid [array] value [" + array + "], use positive number or *", fieldName, schemaName, location);
            }
        }

        private static StructDefinitionException definitionError(
                String message, String fieldName, String schemaName, String location)
        {
            return new StructDefinitionException(
                    message + ", field [" + fieldName + "] of struct [" + schemaName + "], location: [" + location + "]");
        }

        public static SchemaField basicField(String name, Class<? extends Basic<?>> basicType)
        {
            return new SchemaField(SchemaField.Kind.BASIC, name, basicType, null, false, null, null, null);
        }

        public static SchemaField charField(String name, int length, Charset charset)
        {
            return new SchemaField(SchemaField.Kind.CHAR, name, null, length, false, null, null, ObjectUtil.defaultIfNull(charset, StandardCharsets.UTF_8));
        }

        public static SchemaField bytesField(String name, int length)
        {
            return new SchemaField(SchemaField.Kind.BYTES, name, null, length, false, null, null, null);
        }

        public static SchemaField schemaField(String name, String schemaRef)
        {
            return new SchemaField(SchemaField.Kind.SCHEMA, name, null, null, false, schemaRef, null, null);
        }

        public static SchemaField basicArray(String name, Class<? extends Basic<?>> basicType, Integer length, boolean flexible)
        {
            return new SchemaField(ARRAY, name, basicType, length, flexible, null, BASIC, null);
        }

        public static SchemaField schemaArray(String name, String schemaRef, Integer length, boolean flexible)
        {
            return new SchemaField(ARRAY, name, null, length, flexible, schemaRef, SCHEMA, null);
        }

        public Object readBasicValue(ByteBuf byteBuf, ByteOrder byteOrder)
        {
            return basicValueReader.read(byteBuf, byteOrder);
        }

        public void writeBasicValue(ByteBuf byteBuf, ByteOrder byteOrder, Object value)
        {
            basicValueWriter.write(byteBuf, byteOrder, value);
        }

        void resolvedSchemaRef(String resolvedSchemaRef)
        {
            this.resolvedSchemaRef = resolvedSchemaRef;
        }
    }
}
