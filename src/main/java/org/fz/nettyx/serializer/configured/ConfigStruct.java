package org.fz.nettyx.serializer.configured;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.nio.ByteOrder;
import java.util.List;

/**
 * struct definition parsed from config file, a struct is uniquely identified by {@code namespace.name}
 *
 * @author fengbinbin
 * @since 2026-08-16
 */
@Getter
@Accessors(fluent = true)
public class ConfigStruct {

    private final String namespace;
    private final String name;
    private final ByteOrder byteOrder;
    private final List<ConfigField> fields;

    public ConfigStruct(String namespace, String name, ByteOrder byteOrder, List<ConfigField> fields)
    {
        this.namespace = namespace;
        this.name = name;
        this.byteOrder = byteOrder;
        this.fields = fields;
    }

    /**
     * fully qualified name like mybatis mapper statement: {@code namespace.name}
     */
    public String fqName()
    {
        return namespace + "." + name;
    }
}
