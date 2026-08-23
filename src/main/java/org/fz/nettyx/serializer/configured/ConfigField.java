package org.fz.nettyx.serializer.configured;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * field definition of a configured struct
 *
 * @author fengbinbin
 * @since 2026-08-16
 */
@Getter
@Accessors(fluent = true)
public class ConfigField {

    /**
     * field kind
     */
    public enum Kind { BASIC, CHAR, BYTES, STRUCT, ARRAY }

    /**
     * array element kind
     */
    public enum ElementKind { BASIC, STRUCT }

    private final Kind kind;
    private final String name;

    private final Class<? extends Basic<?>> basicType;
    private final BasicTypeResolver.BasicValueReader basicValueReader;
    private final BasicTypeResolver.BasicValueWriter basicValueWriter;
    private final Integer length;
    private final boolean flexible;
    private final String structRef;
    private final ElementKind elementKind;
    private final Charset charset;

    private String resolvedStructRef;

    private ConfigField(
            Kind kind,
            String name,
            Class<? extends Basic<?>> basicType,
            Integer length,
            boolean flexible,
            String structRef,
            ElementKind elementKind,
            Charset charset)
    {
        this.kind = kind;
        this.name = name;
        this.basicType = basicType;
        this.basicValueReader = basicType == null ? null : BasicTypeResolver.valueReaderFor(basicType);
        this.basicValueWriter = basicType == null ? null : BasicTypeResolver.valueWriterFor(basicType);
        this.length = length;
        this.flexible = flexible;
        this.structRef = structRef;
        this.elementKind = elementKind;
        this.charset = charset;
    }

    public static ConfigField basicField(String name, Class<? extends Basic<?>> basicType)
    {
        return new ConfigField(Kind.BASIC, name, basicType, null, false, null, null, null);
    }

    public static ConfigField charField(String name, int length, Charset charset)
    {
        return new ConfigField(Kind.CHAR, name, null, length, false, null, null, charset == null ? StandardCharsets.UTF_8 : charset);
    }

    public static ConfigField bytesField(String name, int length)
    {
        return new ConfigField(Kind.BYTES, name, null, length, false, null, null, null);
    }

    public static ConfigField structField(String name, String structRef)
    {
        return new ConfigField(Kind.STRUCT, name, null, null, false, structRef, null, null);
    }

    public static ConfigField basicArray(String name, Class<? extends Basic<?>> basicType, Integer length, boolean flexible)
    {
        return new ConfigField(Kind.ARRAY, name, basicType, length, flexible, null, ElementKind.BASIC, null);
    }

    public static ConfigField structArray(String name, String structRef, Integer length, boolean flexible)
    {
        return new ConfigField(Kind.ARRAY, name, null, length, flexible, structRef, ElementKind.STRUCT, null);
    }

    void resolvedStructRef(String resolvedStructRef)
    {
        this.resolvedStructRef = resolvedStructRef;
    }
}
