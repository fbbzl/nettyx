package org.fz.nettyx.serializer.configured.parser;

import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.serializer.configured.ConfigField;
import org.fz.nettyx.serializer.configured.ConfigStruct;
import org.fz.nettyx.serializer.configured.type.BasicTypeResolver;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * parses xml struct config files:
 * <pre>{@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <!DOCTYPE structs PUBLIC "-//fbbzl//DTD Nettyx Struct Config 1.0//EN" "https://github.com/fbbzl/nettyx/dtd/struct-config.dtd">
 * <structs namespace="device">
 *     <struct name="Device" endian="LE">
 *         <field name="id"     type="cint"/>
 *         <field name="name"   type="char" length="16" charset="GBK"/>
 *         <field name="raw"    type="byte" length="4"/>
 *         <field name="values" type="cint" array="10"/>
 *         <field name="tail"   type="cuchar" array="*"/>
 *         <field name="gps"    struct="geo.GpsPoint"/>
 *     </struct>
 * </structs>
 * }</pre>
 * the DOCTYPE is optional, the dtd is bundled in the jar and resolved locally like mybatis mappers
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-16
 */
public class XmlStructConfigParser {

    public static final String DOCTYPE_PUBLIC_ID = "-//fbbzl//DTD Nettyx Struct Config 1.0//EN";
    public static final String DOCTYPE_SYSTEM_ID = "https://github.com/fbbzl/nettyx/dtd/struct-config.dtd";

    static final String DTD_RESOURCE = "org/fz/nettyx/serializer/configured/struct-config.dtd";

    static final String TAG_STRUCTS = "structs";
    static final String TAG_STRUCT  = "struct";
    static final String TAG_FIELD   = "field";

    static final String ATTR_NAMESPACE = "namespace";
    static final String ATTR_NAME      = "name";
    static final String ATTR_ENDIAN    = "endian";
    static final String ATTR_TYPE      = "type";
    static final String ATTR_STRUCT    = "struct";
    static final String ATTR_LENGTH    = "length";
    static final String ATTR_ARRAY     = "array";
    static final String ATTR_CHARSET   = "charset";

    static final String TYPE_CHAR = "char";
    static final String TYPE_BYTE = "byte";

    static final String ARRAY_FLEXIBLE = "*";

    public Map<String, ConfigStruct> parse(String location, InputStream input)
    {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            harden(factory);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(structDtdResolver());

            Element structsEl = builder.parse(input).getDocumentElement();
            if (!TAG_STRUCTS.equals(structsEl.getTagName()))
                throw new StructDefinitionException("root element must be <structs>, location: [" + location + "]");

            String namespace = requiredAttr(structsEl, ATTR_NAMESPACE, location);

            Map<String, ConfigStruct> structs     = new LinkedHashMap<>();
            NodeList                  structNodes = structsEl.getElementsByTagName(TAG_STRUCT);
            for (int i = 0; i < structNodes.getLength(); i++) {
                ConfigStruct struct = parseStruct(namespace, (Element) structNodes.item(i), location);
                if (structs.put(struct.fqName(), struct) != null)
                    throw new StructDefinitionException("duplicated struct [" + struct.fqName() + "], location: [" + location + "]");
            }
            return structs;
        }
        catch (StructDefinitionException definitionError) {
            throw definitionError;
        }
        catch (Exception parseError) {
            throw new StructDefinitionException("failed to parse struct config, location: [" + location + "]", parseError);
        }
    }

    private ConfigStruct parseStruct(String namespace, Element structEl, String location)
    {
        String    name      = requiredAttr(structEl, ATTR_NAME, location);
        ByteOrder byteOrder = parseEndian(attr(structEl, ATTR_ENDIAN), namespace + "." + name, location);

        List<ConfigField> fields     = new ArrayList<>();
        NodeList          fieldNodes = structEl.getElementsByTagName(TAG_FIELD);
        for (int i = 0; i < fieldNodes.getLength(); i++)
            fields.add(parseField((Element) fieldNodes.item(i), namespace + "." + name, location));

        return new ConfigStruct(namespace, name, byteOrder, fields);
    }

    private ConfigField parseField(Element fieldEl, String structName, String location)
    {
        String name      = requiredAttr(fieldEl, ATTR_NAME, location);
        String type      = attr(fieldEl, ATTR_TYPE);
        String structRef = attr(fieldEl, ATTR_STRUCT);
        String length    = attr(fieldEl, ATTR_LENGTH);
        String array     = attr(fieldEl, ATTR_ARRAY);
        String charset   = attr(fieldEl, ATTR_CHARSET);

        boolean hasType   = type != null;
        boolean hasStruct = structRef != null;
        boolean hasLength = length != null;
        boolean hasArray  = array != null;

        if (hasType == hasStruct)
            throw definitionError("field must declare exactly one of [" + ATTR_TYPE + "] or [" + ATTR_STRUCT + "]", fieldEl, structName, location);

        if (hasStruct) {
            if (hasLength)     throw definitionError("struct field can not declare [" + ATTR_LENGTH + "]", fieldEl, structName, location);
            if (charset != null) throw definitionError("struct field can not declare [" + ATTR_CHARSET + "]", fieldEl, structName, location);
            return hasArray
                   ? ConfigField.structArray(name, structRef, parseArrayLength(array, fieldEl, structName, location), isFlexible(array))
                   : ConfigField.structField(name, structRef);
        }

        if (TYPE_CHAR.equals(type)) {
            if (!hasLength) throw definitionError("char field must declare [" + ATTR_LENGTH + "]", fieldEl, structName, location);
            if (hasArray)   throw definitionError("char field can not declare [" + ATTR_ARRAY + "]", fieldEl, structName, location);
            return ConfigField.charField(name, parseLength(length, fieldEl, structName, location),
                                         parseCharset(charset, fieldEl, structName, location));
        }

        if (charset != null) throw definitionError("only char field can declare [" + ATTR_CHARSET + "]", fieldEl, structName, location);

        if (TYPE_BYTE.equals(type)) {
            if (!hasLength) throw definitionError("byte field must declare [" + ATTR_LENGTH + "]", fieldEl, structName, location);
            if (hasArray)   throw definitionError("byte field can not declare [" + ATTR_ARRAY + "]", fieldEl, structName, location);
            return ConfigField.bytesField(name, parseLength(length, fieldEl, structName, location));
        }

        if (hasLength) throw definitionError("basic field can not declare [" + ATTR_LENGTH + "]", fieldEl, structName, location);

        Class<? extends Basic<?>> basicType = BasicTypeResolver.resolve(type);
        return hasArray
               ? ConfigField.basicArray(name, basicType, parseArrayLength(array, fieldEl, structName, location), isFlexible(array))
               : ConfigField.basicField(name, basicType);
    }

    private static Charset parseCharset(String charset, Element fieldEl, String structName, String location)
    {
        if (charset == null) return StandardCharsets.UTF_8;

        try {
            return Charset.forName(charset);
        }
        catch (Exception charsetError) {
            throw definitionError("unknown [" + ATTR_CHARSET + "] value [" + charset + "]", fieldEl, structName, location);
        }
    }

    private static ByteOrder parseEndian(String endian, String structName, String location)
    {
        if (endian == null) return ByteOrder.BIG_ENDIAN;

        return switch (endian.toUpperCase()) {
            case "BE", "BIG_ENDIAN"    -> ByteOrder.BIG_ENDIAN;
            case "LE", "LITTLE_ENDIAN" -> ByteOrder.LITTLE_ENDIAN;
            case "NATIVE"              -> ByteOrder.nativeOrder();
            default -> throw new StructDefinitionException(
                    "unknown endian [" + endian + "] of struct [" + structName + "], location: [" + location + "]");
        };
    }

    private static boolean isFlexible(String array)
    {
        return ARRAY_FLEXIBLE.equals(array.trim());
    }

    private static int parseLength(String length, Element fieldEl, String structName, String location)
    {
        try {
            int value = Integer.parseInt(length.trim());
            if (value <= 0) throw new NumberFormatException("length must be positive");
            return value;
        }
        catch (NumberFormatException formatError) {
            throw definitionError("invalid [" + ATTR_LENGTH + "] value [" + length + "]", fieldEl, structName, location);
        }
    }

    private static Integer parseArrayLength(String array, Element fieldEl, String structName, String location)
    {
        if (isFlexible(array)) return null;

        try {
            int value = Integer.parseInt(array.trim());
            if (value <= 0) throw new NumberFormatException("array length must be positive");
            return value;
        }
        catch (NumberFormatException formatError) {
            throw definitionError("invalid [" + ATTR_ARRAY + "] value [" + array + "], use positive number or *", fieldEl, structName, location);
        }
    }

    private static String attr(Element element, String name)
    {
        String value = element.getAttribute(name);
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static String requiredAttr(Element element, String name, String location)
    {
        String value = attr(element, name);
        if (value == null)
            throw new StructDefinitionException(
                    "element <" + element.getTagName() + "> missing required attribute [" + name + "], location: [" + location + "]");
        return value;
    }

    private static StructDefinitionException definitionError(String message, Element fieldEl, String structName, String location)
    {
        return new StructDefinitionException(
                message + ", field [" + attr(fieldEl, ATTR_NAME) + "] of struct [" + structName + "], location: [" + location + "]");
    }

    private static void harden(DocumentBuilderFactory factory)
    {
        try {
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
        }
        catch (Exception ignored) {
        }
    }

    /**
     * resolves the declared struct config dtd to the bundled one like mybatis mapper entity resolver,
     * any other external entity is blocked with an empty source
     */
    private static EntityResolver structDtdResolver()
    {
        return (publicId, systemId) -> {
            if (DOCTYPE_PUBLIC_ID.equals(publicId)
                || systemId != null && systemId.toLowerCase().endsWith("struct-config.dtd")) {
                InputStream dtd = Thread.currentThread().getContextClassLoader().getResourceAsStream(DTD_RESOURCE);
                if (dtd != null) return new InputSource(dtd);
            }
            return new InputSource(new StringReader(""));
        };
    }
}
