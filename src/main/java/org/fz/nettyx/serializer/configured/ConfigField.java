package org.fz.nettyx.serializer.configured;

import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.AccessLevel;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.fz.nettyx.serializer.configured.type.BasicTypeResolver.BasicValueReader;
import org.fz.nettyx.serializer.configured.type.BasicTypeResolver.BasicValueWriter;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * field definition of a configured struct
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-16
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConfigField {

    /**
     * field kind
     */
    public enum Kind { BASIC, CHAR, BYTES, STRUCT, ARRAY }

    /**
     * array element kind
     */
    public enum ElementKind { BASIC, STRUCT }

    Kind                      kind;
    String                    name;
    Class<? extends Basic<?>> basicType;
    @Getter(AccessLevel.PACKAGE)
    BasicValueReader          basicValueReader;
    @Getter(AccessLevel.PACKAGE)
    BasicValueWriter          basicValueWriter;
    Integer                   length;
    boolean                   flexible;
    String                    structRef;
    ElementKind               elementKind;
    Charset                   charset;

    @NonFinal
    String                   resolvedStructRef;

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
        this.kind             = kind;
        this.name             = name;
        this.basicType        = basicType;
        this.basicValueReader = basicType == null ? null : org.fz.nettyx.serializer.configured.type.BasicTypeResolver.valueReaderFor(basicType);
        this.basicValueWriter = basicType == null ? null : org.fz.nettyx.serializer.configured.type.BasicTypeResolver.valueWriterFor(basicType);
        this.length           = length;
        this.flexible         = flexible;
        this.structRef        = structRef;
        this.elementKind      = elementKind;
        this.charset          = charset;
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

    public Object readBasicValue(ByteBuf byteBuf, ByteOrder byteOrder)
    {
        return basicValueReader.read(byteBuf, byteOrder);
    }

    public void writeBasicValue(ByteBuf byteBuf, ByteOrder byteOrder, Object value)
    {
        basicValueWriter.write(byteBuf, byteOrder, value);
    }

    void resolvedStructRef(String resolvedStructRef)
    {
        this.resolvedStructRef = resolvedStructRef;
    }
}
