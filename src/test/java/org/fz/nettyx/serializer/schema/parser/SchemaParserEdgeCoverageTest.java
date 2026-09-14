package org.fz.nettyx.serializer.schema.parser;

import org.fz.nettyx.exception.StructDefinitionException;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/** Covers validation branches shared by JSON and YAML schema parsers. */
public class SchemaParserEdgeCoverageTest
{
    @Test
    public void acceptsNativeEndianAndStructArrays()
    {
        String json = "{\"namespace\":\"edge\",\"structs\":["
                      + "{\"name\":\"Point\",\"endian\":\"NATIVE\",\"fields\":[{\"name\":\"x\",\"type\":\"cint\"}]},"
                      + "{\"name\":\"Line\",\"fields\":[{\"name\":\"points\",\"struct\":\"Point\",\"array\":\"2\"}]}]}";

        assertEquals(2, new JsonSchemaParser().parse("native.json", input(json)).size());
    }

    @Test
    public void rejectsMalformedRootAndNodes()
    {
        assertParseError("[]");
        assertParseError("{\"namespace\":\"x\",\"structs\":{}}");
        assertParseError("{\"namespace\":\"x\",\"structs\":[1]}");
        assertParseError("{\"namespace\":\"x\",\"structs\":[{\"name\":\"S\",\"fields\":[1]}]}");
        assertParseError("{\"structs\":[]}");
        assertParseError("{\"namespace\":\"x\",\"unknown\":1}");
        assertParseError("{\"namespace\":\"x\",\"structs\":[{\"name\":\"S\",\"unknown\":1}]}");
        assertParseError("{\"namespace\":\"x\",\"structs\":[{\"name\":\"S\",\"fields\":[{\"type\":\"cint\"}]}]}");
        assertParseError("{\"namespace\":\"x\",\"structs\":[{\"name\":\"S\",\"fields\":\"not-an-array\"}]}");
        assertParseError("{\"namespace\":\"x\",\"structs\":[{\"name\":\"S\",\"fields\":[{\"name\":\"f\",\"type\":[] }]}]}");
    }

    @Test
    public void rejectsInvalidFieldCombinationsAndValues()
    {
        String[] invalid = {
                "{\"name\":\"f\"}",
                "{\"name\":\"f\",\"type\":\"cint\",\"struct\":\"P\"}",
                "{\"name\":\"f\",\"struct\":\"P\",\"charset\":\"UTF-8\"}",
                "{\"name\":\"f\",\"struct\":\"P\",\"length\":\"1\"}",
                "{\"name\":\"f\",\"type\":\"char\"}",
                "{\"name\":\"f\",\"type\":\"char\",\"length\":\"1\",\"array\":\"1\"}",
                "{\"name\":\"f\",\"type\":\"byte\"}",
                "{\"name\":\"f\",\"type\":\"byte\",\"length\":\"1\",\"array\":\"1\"}",
                "{\"name\":\"f\",\"type\":\"cint\",\"charset\":\"UTF-8\"}",
                "{\"name\":\"f\",\"type\":\"cint\",\"length\":\"1\"}",
                "{\"name\":\"f\",\"type\":\"char\",\"length\":\"0\"}",
                "{\"name\":\"f\",\"type\":\"cint\",\"array\":\"0\"}",
                "{\"name\":\"f\",\"type\":\"char\",\"length\":\"1\",\"charset\":\"no-such-charset\"}",
                "{\"name\":\"f\",\"type\":\"cint\",\"array\":\"no\"}",
                "{\"name\":\"f\",\"type\":\"cint\",\"array\":\"*\",\"extra\":1}"
        };
        for (String field : invalid) {
            String json = "{\"namespace\":\"x\",\"structs\":[{\"name\":\"S\",\"fields\":[" + field + "]}]}";
            assertParseError(json);
        }
    }

    @Test
    public void rejectsUnknownEndianAndDuplicateStructs()
    {
        assertParseError("{\"namespace\":\"x\",\"structs\":[{\"name\":\"S\",\"endian\":\"middle\"}]}");
        assertParseError("{\"namespace\":\"x\",\"structs\":[{\"name\":\"S\"},{\"name\":\"S\"}]}");
    }

    @Test
    public void acceptsExplicitCharset()
    {
        String json = "{\"namespace\":\"x\",\"structs\":[{\"name\":\"S\",\"fields\":["
                      + "{\"name\":\"text\",\"type\":\"char\",\"length\":\"4\",\"charset\":\"UTF-16\"}]}]}";
        assertEquals(1, new JsonSchemaParser().parse("charset.json", input(json)).size());
    }

    private static void assertParseError(String json)
    {
        assertThrows(StructDefinitionException.class,
                     () -> new JsonSchemaParser().parse("edge.json", input(json)));
    }

    private static ByteArrayInputStream input(String json)
    {
        return new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
    }
}
