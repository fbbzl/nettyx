package org.fz.nettyx.serializer.schema;

import org.fz.nettyx.exception.StructDefinitionException;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertThrows;

public class SchemaRegistryEdgeCoverageTest
{
    @Test
    public void rejectsMissingLocationsAndDuplicateDefinitions()
    {
        assertThrows(StructDefinitionException.class, () -> SchemaRegistry.load());
        assertThrows(StructDefinitionException.class, () -> SchemaRegistry.load("missing-schema.xml"));
        assertThrows(StructDefinitionException.class, () -> SchemaRegistry.load("classpath:missing-schema.xml"));
        assertThrows(StructDefinitionException.class,
                     () -> SchemaRegistry.load("configured/device.xml", "configured/device.xml"));
    }

    @Test
    public void rejectsUnresolvedReferencesAndMalformedFiles()
            throws Exception
    {
        Path temp = Files.createTempFile("invalid-schema", ".json");
        try {
            Files.writeString(temp, "{\"namespace\":\"x\",\"structs\":[{\"name\":\"S\",\"fields\":[{\"name\":\"nested\",\"struct\":\"Missing\"}]}]}", StandardCharsets.UTF_8);
            assertThrows(StructDefinitionException.class,
                         () -> SchemaRegistry.load(temp.toString()));
        }
        finally {
            Files.deleteIfExists(temp);
        }
    }
}
