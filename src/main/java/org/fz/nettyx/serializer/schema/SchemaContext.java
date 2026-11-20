package org.fz.nettyx.serializer.schema;

import cn.hutool.core.util.ArrayUtil;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.StructDefinitionException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.serializer.schema.parser.JsonSchemaParser;
import org.fz.nettyx.serializer.schema.parser.XmlSchemaParser;
import org.fz.nettyx.serializer.schema.parser.YamlSchemaParser;
import org.fz.nettyx.serializer.schema.type.BasicTypeResolver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.ToIntFunction;

/**
 * Loads schema configuration and resolves references across namespaces
 * like mybatis mapper statement: {@code namespace.name}
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-16
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class SchemaContext
{

    Map<String, Schema>           schemaCache;
    Map<Schema, Integer>          fixedSchemaSizes;
    Map<String, SchemaSerializer> serializerCache = new ConcurrentHashMap<>();

    /**
     * Loads XML, JSON, or YAML schema files. Locations may be classpath resources,
     * {@code classpath:} prefixed resources, or file paths; the extension selects JSON for
     * {@code .json}, YAML for {@code .yaml}/{@code .yml}, and XML otherwise.
     */
    public static SchemaContext load(String... locations)
    {
        if (ArrayUtil.isEmpty(locations))
            throw new StructDefinitionException("at least one struct config location is required");

        Map<String, Schema> schemas = new LinkedHashMap<>();
        for (String location : locations) {
            try (InputStream input = openStream(location)) {
                Map<String, Schema> parsed = parseResource(location, input);
                for (Map.Entry<String, Schema> entry : parsed.entrySet()) {
                    if (schemas.put(entry.getKey(), entry.getValue()) != null)
                        throw new StructDefinitionException("duplicated struct [" + entry.getKey() + "], location: [" + location + "]");
                }
            }
            catch (StructDefinitionException definitionError) {
                throw definitionError;
            }
            catch (Exception loadError) {
                throw new StructDefinitionException("failed to load struct config, location: [" + location + "]", loadError);
            }
        }

        resolveReferences(schemas);
        checkCycles(schemas);

        return new SchemaContext(schemas, calculateFixedSizes(schemas));
    }

    /**
     * Returns the fixed byte length of a schema, or -1 when it is variable-length.
     * Loaded schemas use their precomputed lengths.
     */
    public int fixedSizeOf(Schema schema)
    {
        Integer size = fixedSchemaSizes.get(schema);
        return size == null ? computeFixedSize(schema, this::referencedSchemaSize) : size;
    }

    /**
     * Returns the fixed length of a field, or -1 when its size depends on the input.
     */
    public int fixedFieldSizeOf(Schema.SchemaField field)
    {
        return computeFixedSize(field, this::referencedSchemaSize);
    }

    private int referencedSchemaSize(String name)
    {
        return fixedSchemaSizes.get(require(name));
    }

    /**
     * Gets a schema by fully qualified name {@code namespace.name}, a bare name is also accepted
     * when it is unique among all namespaces
     */
    public Schema require(String schemaName)
    {
        Schema schema = schemaCache.get(schemaName);
        if (schema != null) return schema;

        Schema matched = null;
        for (Schema candidate : schemaCache.values()) {
            if (!candidate.name().equals(schemaName)) continue;
            if (matched != null)
                throw new StructDefinitionException(
                        "ambiguous struct name [" + schemaName + "], use fully qualified name like [" + candidate.fqName() + "]");
            matched = candidate;
        }

        if (matched == null)
            throw new StructDefinitionException("struct [" + schemaName + "] is not configured");
        return matched;
    }

    public SchemaSerializer serializer(String schemaName)
    {
        return serializerCache.computeIfAbsent(schemaName, name -> new SchemaSerializer(this, name));
    }

    private static void resolveReferences(Map<String, Schema> schemas)
    {
        for (Schema schema : schemas.values()) {
            for (Schema.SchemaField field : schema.fields()) {
                String schemaRef = schemaRefOf(field);
                if (schemaRef == null) continue;

                String resolved = schemaRef.contains(".") ? schemaRef : schema.namespace() + "." + schemaRef;
                if (!schemas.containsKey(resolved))
                    throw new StructDefinitionException(
                            "can not resolve struct reference [" + schemaRef + "], field [" + field.name() + "] of struct [" + schema.fqName() + "]");

                field.resolvedSchemaRef(resolved);
            }
        }
    }

    private static void checkCycles(Map<String, Schema> schemas)
    {
        Set<String> visited = new HashSet<>();
        for (String fqName : schemas.keySet()) visit(fqName, schemas, visited, new ArrayDeque<>());
    }

    private static Map<Schema, Integer> calculateFixedSizes(Map<String, Schema> schemas)
    {
        Map<Schema, Integer> fixedSizes = new HashMap<>(schemas.size());
        for (Schema schema : schemas.values()) precomputeFixedSize(schema, schemas, fixedSizes);
        return Map.copyOf(fixedSizes);
    }

    private static int precomputeFixedSize(Schema schema, Map<String, Schema> schemas, Map<Schema, Integer> fixedSizes)
    {
        Integer cached = fixedSizes.get(schema);
        if (cached != null) return cached;

        int size = computeFixedSize(schema, name -> precomputeFixedSize(schemas.get(name), schemas, fixedSizes));
        fixedSizes.put(schema, size);
        return size;
    }

    private static int computeFixedSize(Schema schema, ToIntFunction<String> referencedSize)
    {
        int size = 0;
        for (Schema.SchemaField field : schema.fields()) {
            int fieldSize = computeFixedSize(field, referencedSize);
            if (fieldSize < 0) return -1;
            try {
                size = Math.addExact(size, fieldSize);
            }
            catch (ArithmeticException error) {
                throw new SerializeException("fixed struct size exceeds supported integer range: [" + schema.fqName() + "]", error);
            }
        }
        return size;
    }

    private static int computeFixedSize(Schema.SchemaField field, ToIntFunction<String> referencedSize)
    {
        return switch (field.kind()) {
            case BASIC -> BasicTypeResolver.sizeOf(field.basicType());
            case CHAR, BYTES -> field.length();
            case SCHEMA -> referencedSize.applyAsInt(field.resolvedSchemaRef());
            case ARRAY -> computeFixedArraySize(field, referencedSize);
        };
    }

    private static int computeFixedArraySize(Schema.SchemaField field, ToIntFunction<String> referencedSize)
    {
        if (field.flexible()) return -1;

        int elementSize = switch (field.elementKind()) {
            case BASIC -> BasicTypeResolver.sizeOf(field.basicType());
            case SCHEMA -> referencedSize.applyAsInt(field.resolvedSchemaRef());
        };
        if (elementSize < 0) return -1;
        try {
            return Math.multiplyExact(elementSize, field.length());
        }
        catch (ArithmeticException error) {
            throw new SerializeException("fixed array size exceeds supported integer range: field [" + field.name() + "]", error);
        }
    }

    private static void visit(String fqName, Map<String, Schema> schemas, Set<String> visited, Deque<String> path)
    {
        if (visited.contains(fqName)) return;
        if (path.contains(fqName))
            throw new StructDefinitionException("cyclic struct reference detected: " + String.join(" -> ", path) + " -> " + fqName);

        path.addLast(fqName);
        for (Schema.SchemaField field : schemas.get(fqName).fields()) {
            String resolved = field.resolvedSchemaRef();
            if (resolved != null) visit(resolved, schemas, visited, path);
        }
        path.removeLast();
        visited.add(fqName);
    }

    private static String schemaRefOf(Schema.SchemaField field)
    {
        return switch (field.kind()) {
            case SCHEMA -> field.schemaRef();
            case ARRAY -> field.elementKind() == Schema.SchemaField.ElementKind.SCHEMA ? field.schemaRef() : null;
            default -> null;
        };
    }

    private static InputStream openStream(String location) throws Exception
    {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

        if (location.startsWith("classpath:")) {
            InputStream input = contextClassLoader.getResourceAsStream(location.substring("classpath:".length()));
            if (input == null) throw new SerializeException("classpath resource not found: [" + location + "]");
            return input;
        }

        File file = new File(location);
        if (file.exists()) return new FileInputStream(file);

        InputStream input = contextClassLoader.getResourceAsStream(location);
        if (input != null) return input;

        throw new SerializeException("struct config location not found: [" + location + "]");
    }

    private static Map<String, Schema> parseResource(String location, InputStream input)
    {
        String normalizedLocation = location.toLowerCase(Locale.ROOT);
        if (normalizedLocation.endsWith(".json")) return new JsonSchemaParser().parse(location, input);
        if (normalizedLocation.endsWith(".yaml") || normalizedLocation.endsWith(".yml"))
            return new YamlSchemaParser().parse(location, input);
        return new XmlSchemaParser().parse(location, input);
    }
}
