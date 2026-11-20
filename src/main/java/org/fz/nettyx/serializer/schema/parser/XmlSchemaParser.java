package org.fz.nettyx.serializer.schema.parser;

import lombok.experimental.UtilityClass;
import org.fz.nettyx.exception.StructDefinitionException;
import org.fz.nettyx.serializer.schema.Schema.SchemaField;
import org.fz.nettyx.serializer.schema.Schema;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses XML schema configuration:
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
public class XmlSchemaParser
{

    public static final String DOCTYPE_PUBLIC_ID = "-//fbbzl//DTD Nettyx Struct Config 1.0//EN";
    public static final String DOCTYPE_SYSTEM_ID = "https://github.com/fbbzl/nettyx/dtd/struct-config.dtd";

    static final String DTD_RESOURCE = "org/fz/nettyx/serializer/schema/struct-config.dtd";

    /**
     * Defines the element names recognized in an XML schema document.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-09-12
     */
    @UtilityClass
    private class Tag
    {
        private static final String
                SCHEMAS = "structs",
                SCHEMA  = "struct",
                FIELD   = "field";
    }

    /**
     * Defines the attribute names recognized in an XML schema document.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-09-12
     */
    @UtilityClass
    private class Attributes
    {

        private static final String
                NAMESPACE  = "namespace",
                NAME       = "name",
                ENDIAN     = "endian",
                TYPE       = "type",
                SCHEMA_REF = "struct",
                LENGTH     = "length",
                ARRAY      = "array",
                CHARSET    = "charset";
    }

    public Map<String, Schema> parse(String location, InputStream input)
    {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            harden(factory);

            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setEntityResolver(schemaDtdResolver());

            Element schemasEl = builder.parse(input).getDocumentElement();
            if (!Tag.SCHEMAS.equals(schemasEl.getTagName()))
                throw new StructDefinitionException("root element must be <structs>, location: [" + location + "]");

            String namespace = requiredAttr(schemasEl, Attributes.NAMESPACE, location);

            Map<String, Schema> schemas     = new LinkedHashMap<>();
            NodeList            schemaNodes = schemasEl.getElementsByTagName(Tag.SCHEMA);
            for (int i = 0; i < schemaNodes.getLength(); i++) {
                Schema schema = parseSchema(namespace, (Element) schemaNodes.item(i), location);
                if (schemas.put(schema.fqName(), schema) != null)
                    throw new StructDefinitionException("duplicated struct [" + schema.fqName() + "], location: [" + location + "]");
            }
            return schemas;
        }
        catch (StructDefinitionException definitionError) {
            throw definitionError;
        }
        catch (Exception parseError) {
            throw new StructDefinitionException("failed to parse struct config, location: [" + location + "]", parseError);
        }
    }

    private Schema parseSchema(String namespace, Element schemaEl, String location)
    {
        String    name      = requiredAttr(schemaEl, Attributes.NAME, location);
        ByteOrder byteOrder = SchemaParser.parseEndian(attr(schemaEl, Attributes.ENDIAN), namespace + "." + name, location);

        List<SchemaField> fields     = new ArrayList<>();
        NodeList          fieldNodes = schemaEl.getElementsByTagName(Tag.FIELD);
        for (int i = 0; i < fieldNodes.getLength(); i++)
             fields.add(parseField((Element) fieldNodes.item(i), namespace + "." + name, location));

        return new Schema(namespace, name, byteOrder, fields);
    }

    private SchemaField parseField(Element fieldEl, String schemaName, String location)
    {
        String name      = requiredAttr(fieldEl, Attributes.NAME, location);
        String type      = attr(fieldEl, Attributes.TYPE);
        String schemaRef = attr(fieldEl, Attributes.SCHEMA_REF);
        String length    = attr(fieldEl, Attributes.LENGTH);
        String array     = attr(fieldEl, Attributes.ARRAY);
        String charset   = attr(fieldEl, Attributes.CHARSET);

        return SchemaField.fromConfig(name, type, schemaRef, length, array, charset, schemaName, location);
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
     * resolves the declared schema configuration DTD to the bundled one like mybatis mapper entity resolver,
     * any other external entity is blocked with an empty source
     */
    private static EntityResolver schemaDtdResolver()
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
