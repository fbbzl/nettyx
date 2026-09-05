package org.fz.nettyx.serializer.schema.parser;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class XmlStructConfigParserTest {

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
        String xml = "<!DOCTYPE structs PUBLIC \"" + XmlStructConfigParser.DOCTYPE_PUBLIC_ID
                     + "\" \"" + XmlStructConfigParser.DOCTYPE_SYSTEM_ID + "\">"
                     + "<structs namespace=\"bundled\"><struct name=\"Empty\"/></structs>";
        try (InputStream input = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            thread.setContextClassLoader(tracking);
            assertNotNull(new XmlStructConfigParser().parse("bundled-dtd-test", input).get("bundled.Empty"));
            assertTrue("Parsing must open the bundled DTD, not silently use an empty fallback", loaded.get());
        }
        finally {
            thread.setContextClassLoader(original);
        }
    }
}
