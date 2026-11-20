package org.fz.nettyx.serializer.schema;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.exception.SerializeException;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Checks typed reads, independent offsets, and buffer ownership for schema views.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-09-18
 */
public class SchemaViewTest
{
    private static final SchemaContext CONTEXT = SchemaContext.load("schema/device.json");
    private static final SchemaContext UNSIGNED_CONTEXT = SchemaContext.load("schema/device.json", "schema/unsigned.json");
    private static final int MESSAGE_LENGTH = 67;

    @Test
    public void contextCachesSerializersAndConstructorIsNotPublic()
    {
        SchemaSerializer first = CONTEXT.serializer("device.Message");
        SchemaSerializer second = CONTEXT.serializer("device.Message");

        assertSame(first, second);
        assertFalse(Modifier.isPublic(SchemaSerializer.class.getDeclaredConstructors()[0].getModifiers()));
    }

    @Test
    public void schemaMetadataCannotBeChangedThroughCollectionOrArrayAliases()
    {
        List<Schema.SchemaField> fields = new ArrayList<>(List.of(Schema.SchemaField.basicField("id", cint.class)));
        Schema                   schema = new Schema("device", "Message", ByteOrder.BIG_ENDIAN, fields);

        fields.clear();
        assertEquals(1, schema.fields().size());
        assertThrows(UnsupportedOperationException.class, () -> schema.fields().clear());

        String[] names = schema.fieldNames();
        names[0] = "changed";
        assertArrayEquals(new String[]{"id"}, schema.fieldNames());
    }

    @Test
    public void documentedUsageAndAllTypedGettersWork()
    {
        SchemaContext context = SchemaContext.load("schema/device.json");
        SchemaSerializer serializer = context.serializer("device.Message");
        SchemaView view = serializer.view(messageBuffer(false, 7, 1_234_567_890_123L));

        int id = view.getInt("id");
        long timestamp = view.getLong("timestamp");
        String name = view.getString("name");
        byte[] payload = view.getBytes("payload");

        assertEquals(7, id);
        assertEquals(1_234_567_890_123L, timestamp);
        assertEquals("netty", name);
        assertArrayEquals(new byte[]{9, 8, 7}, payload);
        assertEquals(-5, view.getByte("byteValue"));
        assertEquals(1234, view.getShort("shortValue"));
        assertEquals(1234.0F, view.getFloat("shortValue"), 0.0F);
        assertEquals(7.0F, view.getFloat("id"), 0.0F);
        assertEquals(7.0D, view.getDouble("id"), 0.0D);
        assertEquals(1.5F, view.getFloat("ratio"), 0.0F);
        assertEquals(36.6D, view.getDouble("temperature"), 0.0D);
        assertEquals(1.5D, view.getDouble("ratio"), 0.0D);
        assertEquals(1_234_567_890_123D, view.getDouble("timestamp"), 0.0D);
        assertTrue(view.getBoolean("active"));
        assertEquals(List.of((short) 11, (short) 22), view.getList("values"));
        assertEquals(Map.of("x", 100, "y", 200), view.getMap("point"));
        assertEquals(2, view.getList("points").size());
        assertEquals(MESSAGE_LENGTH, view.byteLength());
    }

    @Test
    public void viewAdvancesReaderIndexAndIndependentViewsKeepTheirOwnStart()
    {
        SchemaSerializer serializer = CONTEXT.serializer("device.Message");
        ByteBuf source = Unpooled.buffer();
        source.writeByte(99);
        source.writeBytes(messageBytes(1, 10L));
        source.writeBytes(messageBytes(2, 20L));
        source.readerIndex(1);

        SchemaView first = serializer.view(source);
        SchemaView second = serializer.view(source);

        assertEquals(1 + MESSAGE_LENGTH * 2, source.readerIndex());
        assertEquals(1, first.getInt("id"));
        assertEquals(10L, first.getLong("timestamp"));
        assertEquals(2, second.getInt("id"));
        assertEquals(20L, second.getLong("timestamp"));
    }

    @Test
    public void concurrentViewDoesNotWaitForSchemaContextMonitor() throws Exception
    {
        SchemaSerializer serializer = CONTEXT.serializer("device.Message");
        ExecutorService executor = Executors.newSingleThreadExecutor();

        try {
            synchronized (CONTEXT) {
                Future<Integer> result = executor.submit(() -> {
                    ByteBuf source = messageBuffer(false, 19, 190L);
                    try {
                        return serializer.view(source).getInt("id");
                    }
                    finally {
                        source.release();
                    }
                });
                assertEquals(Integer.valueOf(19), result.get(5, TimeUnit.SECONDS));
            }
        }
        finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    public void viewReadsOutOfOrderFieldsWithoutSharingMutableValues()
    {
        ByteBuf source = messageBuffer(false, 7, 70L);
        SchemaView view = CONTEXT.serializer("device.Message").view(source);
        int readerIndexAfterView = source.readerIndex();

        assertEquals(2, view.getList("points").size());
        assertEquals(7, view.getInt("id"));
        source.setIntLE(3, 17);
        assertEquals(17, view.getInt("id"));
        byte[] payload = view.getBytes("payload");
        payload[0] = 0;
        assertArrayEquals(new byte[]{9, 8, 7}, view.getBytes("payload"));
        assertEquals(readerIndexAfterView, source.readerIndex());
    }

    @Test
    public void heapAndDirectBuffersAreSupportedWithoutReferenceCountChanges()
    {
        SchemaSerializer serializer = CONTEXT.serializer("device.Message");
        ByteBuf heap = messageBuffer(false, 3, 30L);
        ByteBuf direct = messageBuffer(true, 4, 40L);
        try {
            int directRefCount = direct.refCnt();
            assertEquals(3, serializer.view(heap).getInt("id"));
            assertEquals(4, serializer.view(direct).getInt("id"));
            assertEquals(directRefCount, direct.refCnt());
        }
        finally {
            direct.release();
        }
    }

    @Test
    public void invalidViewsAndTypedReadsReportTheirContracts()
    {
        SchemaSerializer serializer = CONTEXT.serializer("device.Message");
        assertThrows(NullPointerException.class, () -> serializer.view(null));
        assertThrows(TooLessBytesException.class,
                     () -> serializer.view(Unpooled.wrappedBuffer(new byte[MESSAGE_LENGTH - 1])));

        SchemaContext configured = SchemaContext.load("configured/device.json", "configured/geo.yml");
        assertThrows(SerializeException.class,
                     () -> configured.serializer("device.Flexible").view(Unpooled.wrappedBuffer(new byte[8])));

        SchemaView view = serializer.view(messageBuffer(false, 5, Long.MAX_VALUE));
        assertTypeError(() -> view.getInt("missing"), "device.Message", "missing", "int", "missing");
        assertTypeError(() -> view.getInt("name"), "device.Message", "name", "int", "java.lang.String");
        assertTypeError(() -> view.getInt("temperature"), "device.Message", "temperature", "int", "java.lang.Double");
        assertTypeError(() -> view.getInt("timestamp"), "device.Message", "timestamp", "int", "java.lang.Long");
        assertTypeError(() -> view.getDouble("timestamp"), "device.Message", "timestamp", "double", "java.lang.Long");
        assertTypeError(() -> view.getFloat("temperature"), "device.Message", "temperature", "float", "java.lang.Double");
        assertTypeError(() -> view.getBoolean("id"), "device.Message", "id", "boolean", "java.lang.Integer");
        assertTypeError(() -> view.getInt(null), "device.Message", "null", "int", "missing");
        assertTypeError(() -> view.getString("payload"), "device.Message", "payload", "String", "byte[]");

        SchemaView floatPrecision = serializer.view(messageBuffer(false, 5, 1_000_000_000_000_000_000L));
        assertTypeError(() -> floatPrecision.getFloat("timestamp"),
                        "device.Message", "timestamp", "float", "java.lang.Long");

        SchemaView nullable = CONTEXT.serializer("device.Nullable").view(Unpooled.wrappedBuffer(new byte[1]));
        assertTypeError(() -> nullable.getInt("value"), "device.Nullable", "value", "int", "null");
    }

    @Test
    public void viewCoversIntegerBoundsAndUnsignedBigIntegerConversions()
    {
        ByteBuf bounds = messageBuffer(false, 5, 50L);
        try {
            SchemaView view = CONTEXT.serializer("device.Message").view(bounds);
            bounds.setByte(0, Byte.MIN_VALUE);
            assertEquals(Byte.MIN_VALUE, view.getByte("byteValue"));
            bounds.setByte(0, Byte.MAX_VALUE);
            assertEquals(Byte.MAX_VALUE, view.getByte("byteValue"));

            bounds.setIntLE(3, 128);
            assertTypeError(() -> view.getByte("id"), "device.Message", "id", "byte", "java.lang.Integer");
            bounds.setIntLE(3, -129);
            assertTypeError(() -> view.getByte("id"), "device.Message", "id", "byte", "java.lang.Integer");
        }
        finally {
            bounds.release();
        }

        ByteBuf unsigned = Unpooled.buffer(8).writeLong(1L << 32);
        try {
            SchemaView unsignedView = UNSIGNED_CONTEXT.serializer("device.Unsigned").view(unsigned);
            assertEquals((float) (1L << 32), unsignedView.getFloat("value"), 0.0F);
            assertEquals((double) (1L << 32), unsignedView.getDouble("value"), 0.0D);

            unsigned.setLong(0, -1L);
            assertTypeError(() -> unsignedView.getFloat("value"), "device.Unsigned", "value", "float", "java.math.BigInteger");
            assertTypeError(() -> unsignedView.getDouble("value"), "device.Unsigned", "value", "double", "java.math.BigInteger");
        }
        finally {
            unsigned.release();
        }
    }

    @Test
    public void integerToFloatingPointGettersRejectOverflow()
    {
        ByteBuf source = Unpooled.buffer(8).writeZero(8);
        try {
            SchemaView view = UNSIGNED_CONTEXT.serializer("device.HugeInteger").view(source);
            assertTypeError(() -> view.getFloat("value"), "device.HugeInteger", "value", "float", "java.math.BigInteger");
            assertTypeError(() -> view.getDouble("value"), "device.HugeInteger", "value", "double", "java.math.BigInteger");
        }
        finally {
            source.release();
        }
    }

    @Test
    public void integerToFloatingPointGettersRejectFractionalConversionFromBasicExtension()
    {
        ByteBuf source = Unpooled.buffer(Long.BYTES).writeLong(1L);
        try {
            SchemaView view = UNSIGNED_CONTEXT.serializer("device.HugeInteger").view(source);
            assertTypeError(() -> view.getFloat("value"), "device.HugeInteger", "value", "float",
                            "FractionalConversion");
            assertTypeError(() -> view.getDouble("value"), "device.HugeInteger", "value", "double",
                            "FractionalConversion");
        }
        finally {
            source.release();
        }
    }

    @Test
    public void directViewConstructionRejectsVariableLengthFields()
    {
        SchemaContext configured = SchemaContext.load("configured/device.json", "configured/geo.yml");
        ByteBuf source = Unpooled.buffer(2);
        try {
            assertThrows(SerializeException.class,
                         () -> new SchemaView(configured.serializer("device.Flexible"), configured, source, 0, 2));
        }
        finally {
            source.release();
        }
    }

    @Test
    public void schemaViewExposesOnlyTheTypedReadApi()
    {
        Set<String> methods = Arrays.stream(SchemaView.class.getDeclaredMethods())
                                    .filter(method -> Modifier.isPublic(method.getModifiers()))
                                    .map(Method::getName)
                                    .collect(Collectors.toSet());

        assertEquals(Set.of("byteLength", "getByte", "getShort", "getInt", "getLong", "getFloat",
                            "getDouble", "getBoolean", "getString", "getBytes", "getList", "getMap"), methods);
    }

    private static ByteBuf messageBuffer(boolean direct, int id, long timestamp)
    {
        ByteBuf buffer = direct ? Unpooled.directBuffer(MESSAGE_LENGTH) : Unpooled.buffer(MESSAGE_LENGTH);
        writeMessage(buffer, id, timestamp);
        return buffer;
    }

    private static byte[] messageBytes(int id, long timestamp)
    {
        ByteBuf buffer = messageBuffer(false, id, timestamp);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        return bytes;
    }

    private static void writeMessage(ByteBuf buffer, int id, long timestamp)
    {
        buffer.writeByte(-5);
        buffer.writeShortLE(1234);
        buffer.writeIntLE(id);
        buffer.writeLongLE(timestamp);
        buffer.writeFloatLE(1.5F);
        buffer.writeDoubleLE(36.6D);
        buffer.writeByte(1);
        buffer.writeBytes(new byte[]{'n', 'e', 't', 't', 'y', 0, 0, 0});
        buffer.writeBytes(new byte[]{9, 8, 7});
        buffer.writeShortLE(11);
        buffer.writeShortLE(22);
        buffer.writeInt(100);
        buffer.writeInt(200);
        buffer.writeInt(1);
        buffer.writeInt(2);
        buffer.writeInt(3);
        buffer.writeInt(4);
    }

    private static void assertTypeError(
            Runnable action,
            String schema,
            String field,
            String expected,
            String actual)
    {
        SerializeException error = assertThrows(SerializeException.class, action::run);
        assertTrue(error.getMessage(), error.getMessage().contains(schema));
        assertTrue(error.getMessage(), error.getMessage().contains(field));
        assertTrue(error.getMessage(), error.getMessage().contains(expected));
        assertTrue(error.getMessage(), error.getMessage().contains(actual));
    }
}
