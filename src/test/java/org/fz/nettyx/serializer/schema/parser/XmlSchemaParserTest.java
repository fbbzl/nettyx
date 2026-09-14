package org.fz.nettyx.serializer.schema.parser;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import org.fz.nettyx.exception.StructDefinitionException;

public class XmlSchemaParserTest {

    @Test
    public void declaredDoctypeLoadsBundledDtd() throws Exception {
        Thread thread = Thread.currentThread();
        ClassLoader original = thread.getContextClassLoader();
        AtomicBoolean loaded = new AtomicBoolean();
        ClassLoader tracking = new ClassLoader(original) {
            @Override
            public InputStream getResourceAsStream(String name) {
                InputStream resource = super.getResourceAsStream(name);
                if (name.endsWith("/struct-config.dtd") && resource != null) loaded.set(true);
                return resource;
            }
        };
        String xml = "<!DOCTYPE structs PUBLIC \"" + XmlSchemaParser.DOCTYPE_PUBLIC_ID
                     + "\" \"" + XmlSchemaParser.DOCTYPE_SYSTEM_ID + "\">"
                     + "<structs namespace=\"bundled\"><struct name=\"Empty\"/></structs>";
        try (InputStream input = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            thread.setContextClassLoader(tracking);
            assertNotNull(new XmlSchemaParser().parse("bundled-dtd-test", input).get("bundled.Empty"));
            assertTrue("Parsing must open the bundled DTD, not silently use an empty fallback", loaded.get());
        }
        finally {
            thread.setContextClassLoader(original);
        }
    }

    @Test
    public void parsesNativeEndianAndRejectsInvalidXmlDefinitions()
    {
        String valid = "<structs namespace=\"x\"><struct name=\"S\" endian=\"NATIVE\">"
                       + "<field name=\"v\" type=\"cint\"/><field name=\"text\" type=\"char\" length=\"4\" charset=\"UTF-8\"/>"
                       + "<field name=\"raw\" type=\"byte\" length=\"2\"/><field name=\"values\" type=\"cint\" array=\"*\"/>"
                       + "</struct></structs>";
        assertEquals(1, parse(valid).size());

        String[] invalid = {
                "<wrong namespace=\"x\"/>",
                "<structs><struct name=\"S\"/></structs>",
                "<structs namespace=\"x\"><struct name=\"S\" endian=\"middle\"/></structs>",
                "<structs namespace=\"x\"><struct name=\"S\"/><struct name=\"S\"/></structs>",
                "<structs namespace=\"x\"><struct name=\"S\"><field name=\"f\" struct=\"P\" length=\"1\"/></struct></structs>",
                "<structs namespace=\"x\"><struct name=\"S\"><field name=\"f\" struct=\"P\" charset=\"UTF-8\"/></struct></structs>",
                "<structs namespace=\"x\"><struct name=\"S\"><field name=\"f\" type=\"char\" length=\"1\" array=\"1\"/></struct></structs>",
                "<structs namespace=\"x\"><struct name=\"S\"><field name=\"f\" type=\"byte\"/></struct></structs>",
                "<structs namespace=\"x\"><struct name=\"S\"><field name=\"f\" type=\"byte\" length=\"1\" array=\"1\"/></struct></structs>",
                "<structs namespace=\"x\"><struct name=\"S\"><field name=\"f\" type=\"cint\" length=\"1\"/></struct></structs>",
                "<structs namespace=\"x\"><struct name=\"S\"><field name=\"f\" type=\"cint\" array=\"bad\"/></struct></structs>",
                "<structs namespace=\"x\"><struct name=\"S\"><field type=\"cint\"/></struct></structs>"
        };
        for (String xml : invalid)
            assertThrows(StructDefinitionException.class, () -> parse(xml));
    }

    private static java.util.Map<String, org.fz.nettyx.serializer.schema.Schema> parse(String xml)
    {
        return new XmlSchemaParser().parse("edge.xml", new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }
}
