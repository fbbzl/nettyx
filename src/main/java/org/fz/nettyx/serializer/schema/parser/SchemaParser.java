package org.fz.nettyx.serializer.schema.parser;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.serializer.schema.Schema;
import org.fz.nettyx.serializer.schema.Schema.SchemaField;

import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Shared validation and mapping for tree-based schema configuration formats.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-09-02
 */
interface SchemaParser
{

    Set<String> ROOT_KEYS  = Set.of("namespace", "structs");
    Set<String> SCHEMA_KEYS = Set.of("name", "endian", "fields");
    Set<String> FIELD_KEYS = Set.of("name", "type", "struct", "length", "array", "charset");

    Map<String, Schema> parse(String location, InputStream input);

    default Map<String, Schema> parseTree(String location, JsonNode root)
    {
        ObjectNode rootObject = object(root, "root", location);
        rejectUnknownKeys(rootObject, ROOT_KEYS, "root", location);

        String    namespace = requiredText(rootObject, "namespace", "root", location);
        ArrayNode schemas   = arrayOrEmpty(rootObject.get("structs"), "root field [structs]", location);

        Map<String, Schema> parsed = new LinkedHashMap<>();
        for (int index = 0; index < schemas.size(); index++) {
            Schema schema = parseSchema(namespace, schemas.get(index), index, location);
            if (parsed.put(schema.fqName(), schema) != null)
                throw new StructDefinitionException("duplicated struct [" + schema.fqName() + "], location: [" + location + "]");
        }
        return parsed;
    }

    private Schema parseSchema(String namespace, JsonNode node, int index, String location)
    {
        String     context      = "struct at index [" + index + "]";
        ObjectNode schemaObject = object(node, context, location);
        rejectUnknownKeys(schemaObject, SCHEMA_KEYS, context, location);

        String    name      = requiredText(schemaObject, "name", context, location);
        ByteOrder byteOrder = parseEndian(optionalText(schemaObject, "endian", context, location), namespace + "." + name, location);
        ArrayNode fields    = arrayOrEmpty(schemaObject.get("fields"), context + " field [fields]", location);

        List<SchemaField> parsedFields = new ArrayList<>();
        for (int fieldIndex = 0; fieldIndex < fields.size(); fieldIndex++)
             parsedFields.add(parseField(fields.get(fieldIndex), fieldIndex, namespace + "." + name, location));

        return new Schema(namespace, name, byteOrder, parsedFields);
    }

    private SchemaField parseField(JsonNode node, int index, String schemaName, String location)
    {
        String     context     = "field at index [" + index + "] of struct [" + schemaName + "]";
        ObjectNode fieldObject = object(node, context, location);
        rejectUnknownKeys(fieldObject, FIELD_KEYS, context, location);

        String name      = requiredText(fieldObject, "name", context, location);
        String type      = optionalText(fieldObject, "type", context, location);
        String schemaRef = optionalText(fieldObject, "struct", context, location);
        String length    = optionalText(fieldObject, "length", context, location);
        String array     = optionalText(fieldObject, "array", context, location);
        String charset   = optionalText(fieldObject, "charset", context, location);

        return SchemaField.fromConfig(name, type, schemaRef, length, array, charset, schemaName, location);
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
        return StrUtil.emptyToNull(text);
    }

    static ByteOrder parseEndian(String endian, String schemaName, String location)
    {
        if (endian == null) return ByteOrder.BIG_ENDIAN;

        return switch (endian.toUpperCase(Locale.ROOT)) {
            case "BE", "BIG_ENDIAN" -> ByteOrder.BIG_ENDIAN;
            case "LE", "LITTLE_ENDIAN" -> ByteOrder.LITTLE_ENDIAN;
            case "NATIVE" -> ByteOrder.nativeOrder();
            default -> throw new StructDefinitionException(
                    "unknown endian [" + endian + "] of struct [" + schemaName + "], location: [" + location + "]");
        };
    }

}
