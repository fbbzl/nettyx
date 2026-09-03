package org.fz.nettyx.serializer.configured;

import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.StructDefinitionException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.fz.nettyx.serializer.configured.parser.JsonStructConfigParser;
import org.fz.nettyx.serializer.configured.parser.StructConfigParser;
import org.fz.nettyx.serializer.configured.parser.XmlStructConfigParser;
import org.fz.nettyx.serializer.configured.parser.YamlStructConfigParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * registry of configured structs, loads config files, resolves cross-namespace struct references
 * like mybatis mapper statement: {@code namespace.name}
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-16
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StructConfigRegistry
{

    Map<String, ConfigStruct>         structCache;
    Map<String, ConfiguredSerializer> serializerCache = new ConcurrentHashMap<>();

    private StructConfigRegistry(Map<String, ConfigStruct> structCache)
    {
        this.structCache = structCache;
    }

    /**
     * Loads XML, JSON, or YAML struct config files. Locations may be classpath resources,
     * {@code classpath:} prefixed resources, or file paths; the extension selects JSON for
     * {@code .json}, YAML for {@code .yaml}/{@code .yml}, and XML otherwise.
     */
    public static StructConfigRegistry load(String... locations)
    {
        if (locations == null || locations.length == 0)
            throw new StructDefinitionException("at least one struct config location is required");

        Map<String, ConfigStruct> structs = new LinkedHashMap<>();
        for (String location : locations) {
            try (InputStream input = openStream(location)) {
                Map<String, ConfigStruct> parsed = parserFor(location).parse(location, input);
                for (Map.Entry<String, ConfigStruct> entry : parsed.entrySet()) {
                    if (structs.put(entry.getKey(), entry.getValue()) != null)
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

        resolveReferences(structs);
        checkCycles(structs);

        return new StructConfigRegistry(structs);
    }

    /**
     * get struct by fully qualified name {@code namespace.name}, a bare name is also accepted
     * when it is unique among all namespaces
     */
    public ConfigStruct require(String structName)
    {
        ConfigStruct struct = structCache.get(structName);
        if (struct != null) return struct;

        ConfigStruct matched = null;
        for (ConfigStruct candidate : structCache.values()) {
            if (!candidate.name().equals(structName)) continue;
            if (matched != null)
                throw new StructDefinitionException(
                        "ambiguous struct name [" + structName + "], use fully qualified name like [" + candidate.fqName() + "]");
            matched = candidate;
        }

        if (matched == null)
            throw new StructDefinitionException("struct [" + structName + "] is not configured");
        return matched;
    }

    public boolean contains(String structName)
    {
        return structCache.containsKey(structName);
    }

    ConfiguredSerializer serializer(String structName)
    {
        return serializerCache.computeIfAbsent(structName, name -> new ConfiguredSerializer(this, name));
    }

    private static void resolveReferences(Map<String, ConfigStruct> structs)
    {
        for (ConfigStruct struct : structs.values()) {
            for (ConfigField field : struct.fields()) {
                String structRef = structRefOf(field);
                if (structRef == null) continue;

                String resolved = structRef.contains(".") ? structRef : struct.namespace() + "." + structRef;
                if (!structs.containsKey(resolved))
                    throw new StructDefinitionException(
                            "can not resolve struct reference [" + structRef + "], field [" + field.name() + "] of struct [" + struct.fqName() + "]");

                field.resolvedStructRef(resolved);
            }
        }
    }

    private static void checkCycles(Map<String, ConfigStruct> structs)
    {
        Set<String> visited = new HashSet<>();
        for (String fqName : structs.keySet()) visit(fqName, structs, visited, new ArrayDeque<>());
    }

    private static void visit(String fqName, Map<String, ConfigStruct> structs, Set<String> visited, Deque<String> path)
    {
        if (visited.contains(fqName)) return;
        if (path.contains(fqName))
            throw new StructDefinitionException("cyclic struct reference detected: " + String.join(" -> ", path) + " -> " + fqName);

        path.addLast(fqName);
        for (ConfigField field : structs.get(fqName).fields()) {
            String resolved = field.resolvedStructRef();
            if (resolved != null) visit(resolved, structs, visited, path);
        }
        path.removeLast();
        visited.add(fqName);
    }

    private static String structRefOf(ConfigField field)
    {
        return switch (field.kind()) {
            case STRUCT -> field.structRef();
            case ARRAY -> field.elementKind() == ConfigField.ElementKind.STRUCT ? field.structRef() : null;
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

    private static StructConfigParser parserFor(String location)
    {
        String normalizedLocation = location.toLowerCase(Locale.ROOT);
        if (normalizedLocation.endsWith(".json")) return new JsonStructConfigParser();
        if (normalizedLocation.endsWith(".yaml") || normalizedLocation.endsWith(".yml"))
            return new YamlStructConfigParser();
        return new XmlStructConfigParser();
    }
}