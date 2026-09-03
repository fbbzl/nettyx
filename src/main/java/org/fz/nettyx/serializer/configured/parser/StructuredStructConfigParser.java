package org.fz.nettyx.serializer.configured.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.serializer.configured.ConfigField;
import org.fz.nettyx.serializer.configured.ConfigStruct;
import org.fz.nettyx.serializer.configured.type.BasicTypeResolver;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Shared validation and mapping for tree-based struct configuration formats.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-09-02
 */
abstract class StructuredStructConfigParser implements StructConfigParser
{

    private static final Set<String> ROOT_KEYS   = Set.of("namespace", "structs");
    private static final Set<String> STRUCT_KEYS = Set.of("name", "endian", "fields");
    private static final Set<String> FIELD_KEYS  = Set.of("name", "type", "struct", "length", "array", "charset");

    private static final String TYPE_CHAR      = "char";
    private static final String TYPE_BYTE      = "byte";
    private static final String ARRAY_FLEXIBLE = "*";

    protected final Map<String, ConfigStruct> parseTree(String location, JsonNode root)
    {
        ObjectNode rootObject = object(root, "root", location);
        rejectUnknownKeys(rootObject, ROOT_KEYS, "root", location);

        String    namespace = requiredText(rootObject, "namespace", "root", location);
        ArrayNode structs   = arrayOrEmpty(rootObject.get("structs"), "root field [structs]", location);

        Map<String, ConfigStruct> parsed = new LinkedHashMap<>();
        for (int index = 0; index < structs.size(); index++) {
            ConfigStruct struct = parseStruct(namespace, structs.get(index), index, location);
            if (parsed.put(struct.fqName(), struct) != null)
                throw new StructDefinitionException("duplicated struct [" + struct.fqName() + "], location: [" + location + "]");
        }
        return parsed;
    }

    private ConfigStruct parseStruct(String namespace, JsonNode node, int index, String location)
    {
        String     context      = "struct at index [" + index + "]";
        ObjectNode structObject = object(node, context, location);
        rejectUnknownKeys(structObject, STRUCT_KEYS, context, location);

        String    name      = requiredText(structObject, "name", context, location);
        ByteOrder byteOrder = parseEndian(optionalText(structObject, "endian", context, location), namespace + "." + name, location);
        ArrayNode fields    = arrayOrEmpty(structObject.get("fields"), context + " field [fields]", location);

        List<ConfigField> parsedFields = new ArrayList<>();
        for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++)
             parsedFields.add(parseField(fields.get(fieldIndex), fieldIndex, namespace + "." + name, location));

        return new ConfigStruct(namespace, name, byteOrder, parsedFields);
    }

    private ConfigField parseField(JsonNode node, int index, String structName, String location)
    {
        String     context     = "field at index [" + index + "] of struct [" + structName + "]";
        ObjectNode fieldObject = object(node, context, location);
        rejectUnknownKeys(fieldObject, FIELD_KEYS, context, location);

        String name      = requiredText(fieldObject, "name", context, location);
        String type      = optionalText(fieldObject, "type", context, location);
        String structRef = optionalText(fieldObject, "struct", context, location);
        String length    = optionalText(fieldObject, "length", context, location);
        String array     = optionalText(fieldObject, "array", context, location);
        String charset   = optionalText(fieldObject, "charset", context, location);

        boolean hasType   = type != null;
        boolean hasStruct = structRef != null;
        boolean hasLength = length != null;
        boolean hasArray  = array != null;

        if (hasType == hasStruct)
            throw definitionError("field must declare exactly one of [type] or [struct]", name, structName, location);

        if (hasStruct) {
            if (hasLength) throw definitionError("struct field can not declare [length]", name, structName, location);
            if (charset != null)
                throw definitionError("struct field can not declare [charset]", name, structName, location);
            return hasArray
                   ? ConfigField.structArray(name, structRef, parseArrayLength(array, name, structName, location), isFlexible(array))
                   : ConfigField.structField(name, structRef);
        }

        if (TYPE_CHAR.equals(type)) {
            if (!hasLength) throw definitionError("char field must declare [length]", name, structName, location);
            if (hasArray) throw definitionError("char field can not declare [array]", name, structName, location);
            return ConfigField.charField(name, parseLength(length, name, structName, location),
                                         parseCharset(charset, name, structName, location));
        }

        if (charset != null) throw definitionError("only char field can declare [charset]", name, structName, location);

        if (TYPE_BYTE.equals(type)) {
            if (!hasLength) throw definitionError("byte field must declare [length]", name, structName, location);
            if (hasArray) throw definitionError("byte field can not declare [array]", name, structName, location);
            return ConfigField.bytesField(name, parseLength(length, name, structName, location));
        }

        if (hasLength) throw definitionError("basic field can not declare [length]", name, structName, location);

        Class<? extends Basic<?>> basicType = BasicTypeResolver.resolve(type);
        return hasArray
               ? ConfigField.basicArray(name, basicType, parseArrayLength(array, name, structName, location), isFlexible(array))
               : ConfigField.basicField(name, basicType);
    }

    private static ObjectNode object(JsonNode node, String context, String location)
    {
        if (node instanceof ObjectNode object) return object;
        throw new StructDefinitionException(context + " must be an object, location: [" + location + "]");
    }

    private static ArrayNode array(JsonNode node, String context, String location)
    {
        if (node instanceof ArrayNode array) return array;
        throw new StructDefinitionException(context + " must be an array, location: [" + location + "]");
    }

    private static ArrayNode arrayOrEmpty(JsonNode node, String context, String location)
    {
        if (node == null || node.isNull()) return JsonNodeFactory.instance.arrayNode();
        return array(node, context, location);
    }

    private static void rejectUnknownKeys(ObjectNode node, Set<String> allowedKeys, String context, String location)
    {
        Iterator<String> keys = node.fieldNames();
        while (keys.hasNext()) {
            String key = keys.next();
            if (!allowedKeys.contains(key))
                throw new StructDefinitionException("unknown field [" + key + "] in " + context + ", location: [" + location + "]");
        }
    }

    private static String requiredText(ObjectNode node, String key, String context, String location)
    {
        String value = optionalText(node, key, context, location);
        if (value != null) return value;
        throw new StructDefinitionException(context + " missing required field [" + key + "], location: [" + location + "]");
    }

    private static String optionalText(ObjectNode node, String key, String context, String location)
    {
        JsonNode value = node.get(key);
        if (value == null || value.isNull()) return null;
        if (!value.isValueNode())
            throw new StructDefinitionException(context + " field [" + key + "] must be a scalar value, location: [" + location + "]");

        String text = value.asText().trim();
        return text.isEmpty() ? null : text;
    }

    private static ByteOrder parseEndian(String endian, String structName, String location)
    {
        if (endian == null) return ByteOrder.BIG_ENDIAN;

        return switch (endian.toUpperCase(Locale.ROOT)) {
            case "BE", "BIG_ENDIAN" -> ByteOrder.BIG_ENDIAN;
            case "LE", "LITTLE_ENDIAN" -> ByteOrder.LITTLE_ENDIAN;
            case "NATIVE" -> ByteOrder.nativeOrder();
            default -> throw new StructDefinitionException(
                    "unknown endian [" + endian + "] of struct [" + structName + "], location: [" + location + "]");
        };
    }

    private static Charset parseCharset(String charset, String fieldName, String structName, String location)
    {
        if (charset == null) return StandardCharsets.UTF_8;

        try {
            return Charset.forName(charset);
        }
        catch (Exception charsetError) {
            throw definitionError("unknown [charset] value [" + charset + "]", fieldName, structName, location);
        }
    }

    private static boolean isFlexible(String array)
    {
        return ARRAY_FLEXIBLE.equals(array.trim());
    }

    private static int parseLength(String length, String fieldName, String structName, String location)
    {
        try {
            int value = Integer.parseInt(length.trim());
            if (value <= 0) throw new NumberFormatException("length must be positive");
            return value;
        }
        catch (NumberFormatException formatError) {
            throw definitionError("invalid [length] value [" + length + "]", fieldName, structName, location);
        }
    }

    private static Integer parseArrayLength(String array, String fieldName, String structName, String location)
    {
        if (isFlexible(array)) return null;

        try {
            int value = Integer.parseInt(array.trim());
            if (value <= 0) throw new NumberFormatException("array length must be positive");
            return value;
        }
        catch (NumberFormatException formatError) {
            throw definitionError("invalid [array] value [" + array + "], use positive number or *", fieldName, structName, location);
        }
    }

    private static StructDefinitionException definitionError(String message, String fieldName, String structName, String location)
    {
        return new StructDefinitionException(
                message + ", field [" + fieldName + "] of struct [" + structName + "], location: [" + location + "]");
    }
}