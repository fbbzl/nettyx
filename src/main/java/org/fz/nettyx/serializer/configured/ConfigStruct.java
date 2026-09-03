package org.fz.nettyx.serializer.configured;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.nio.ByteOrder;
import java.util.List;

/**
 * struct definition parsed from config file, a struct is uniquely identified by {@code namespace.name}
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-16
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConfigStruct
{

    String            namespace;
    String            name;
    ByteOrder         byteOrder;
    List<ConfigField> fields;
    String[]          fieldNames;

    public ConfigStruct(String namespace, String name, ByteOrder byteOrder, List<ConfigField> fields)
    {
        this.namespace  = namespace;
        this.name       = name;
        this.byteOrder  = byteOrder;
        this.fields     = fields;
        this.fieldNames = fields.stream().map(ConfigField::name).toArray(String[]::new);
    }

    /**
     * fully qualified name like mybatis mapper statement: {@code namespace.name}
     */
    public String fqName()
    {
        return namespace + "." + name;
    }
}