package org.fz.nettyx.serializer.schema;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.StructDefinitionException;
import org.junit.Test;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Checks that the schema context rejects missing, duplicate, malformed, and unresolved definitions.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-09-14
 */
public class SchemaContextEdgeCoverageTest
{
    @Test
    public void rejectsMissingLocationsAndDuplicateDefinitions()
    {
        assertThrows(StructDefinitionException.class, () -> SchemaContext.load());
        assertThrows(StructDefinitionException.class, () -> SchemaContext.load("missing-schema.xml"));
        assertThrows(StructDefinitionException.class, () -> SchemaContext.load("classpath:missing-schema.xml"));
        assertThrows(StructDefinitionException.class,
                     () -> SchemaContext.load("configured/device.xml", "configured/device.xml"));
    }

    @Test
    public void loadsYamlSchemaFromClasspath()
    {
        SchemaContext context = SchemaContext.load("classpath:configured/packet.yaml");
        Schema packet = context.require("packet.Header");

        assertEquals(ByteOrder.LITTLE_ENDIAN, packet.byteOrder());
        assertEquals(6, context.fixedSizeOf(packet));
    }

    @Test
    public void precomputesNestedAndFlexibleSchemaSizesAtLoad()
    {
        SchemaContext context = SchemaContext.load("configured/device.json", "configured/geo.yml");

        assertEquals(38, context.fixedSizeOf(context.require("device.Device")));
        assertEquals(8, context.fixedSizeOf(context.require("geo.GpsPoint")));
        assertEquals(-1, context.fixedSizeOf(context.require("device.Flexible")));
        assertEquals(0, context.fixedSizeOf(context.require("device.Empty")));
    }

    @Test
    public void computesSizeOfStandaloneSchemaWithRegisteredNestedField()
    {
        SchemaContext     context = SchemaContext.load("configured/device.json", "configured/geo.yml");
        Schema.SchemaField point    = Schema.SchemaField.schemaField("point", "geo.GpsPoint");
        point.resolvedSchemaRef("geo.GpsPoint");
        Schema standalone = new Schema("device", "Device", ByteOrder.LITTLE_ENDIAN,
                                       List.of(Schema.SchemaField.bytesField("head", 3), point));

        assertEquals(11, context.fixedSizeOf(standalone));
        assertEquals(38, context.fixedSizeOf(context.require("device.Device")));
    }

    @Test
    public void viewRejectsFixedArrayOfVariableLengthSchemasWithoutConsumingInput() throws Exception
    {
        Path temp = Files.createTempFile("nested-flexible-schema", ".json");
        try {
            Files.writeString(temp, """
                    {"namespace":"packet","structs":[{"name":"Batch","fields":[
                      {"name":"entries","struct":"device.Flexible","array":2}
                    ]}]}
                    """, StandardCharsets.UTF_8);
            SchemaContext context = SchemaContext.load(temp.toString(), "configured/device.json", "configured/geo.yml");
            SchemaSerializer serializer = context.serializer("packet.Batch");

            ByteBuf source = Unpooled.buffer().writeLong(0);
            try {
                assertEquals(-1, context.fixedSizeOf(serializer.getSchema()));
                assertThrows(SerializeException.class, () -> serializer.view(source));
                assertEquals(0, source.readerIndex());
            }
            finally {
                source.release();
            }
        }
        finally {
            Files.deleteIfExists(temp);
        }
    }

    @Test
    public void rejectsFixedSizeOverflowDuringContextLoad() throws Exception
    {
        Path temp = Files.createTempFile("overflow-schema", ".json");
        try {
            Files.writeString(temp, """
                    {"namespace":"overflow","structs":[{"name":"Large","fields":[
                      {"name":"first","type":"byte","length":2147483647},
                      {"name":"second","type":"byte","length":1}
                    ]}]}
                    """, StandardCharsets.UTF_8);

            assertThrows(SerializeException.class, () -> SchemaContext.load(temp.toString()));
        }
        finally {
            Files.deleteIfExists(temp);
        }
    }

    @Test
    public void rejectsFixedArraySizeOverflowDuringContextLoad() throws Exception
    {
        Path temp = Files.createTempFile("overflow-array-schema", ".json");
        try {
            Files.writeString(temp, """
                    {"namespace":"overflow","structs":[{"name":"Large","fields":[
                      {"name":"values","type":"cint","array":536870912}
                    ]}]}
                    """, StandardCharsets.UTF_8);

            SerializeException overflow = assertThrows(SerializeException.class, () -> SchemaContext.load(temp.toString()));
            assertTrue(overflow.getMessage().contains("fixed array size exceeds supported integer range: field [values]"));
            assertTrue(overflow.getCause() instanceof ArithmeticException);
        }
        finally {
            Files.deleteIfExists(temp);
        }
    }

    @Test
    public void rejectsUnresolvedReferencesAndMalformedFiles()
            throws Exception
    {
        Path temp = Files.createTempFile("invalid-schema", ".json");
        try {
            Files.writeString(temp, "{\"namespace\":\"x\",\"structs\":[{\"name\":\"S\",\"fields\":[{\"name\":\"nested\",\"struct\":\"Missing\"}]}]}", StandardCharsets.UTF_8);
            assertThrows(StructDefinitionException.class,
                         () -> SchemaContext.load(temp.toString()));
        }
        finally {
            Files.deleteIfExists(temp);
        }
    }
}
