package org.fz.nettyx.serializer.schema.jmh;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.schema.SchemaSerializer;
import org.fz.nettyx.serializer.schema.SchemaContext;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.fz.nettyx.serializer.basic.c.signed.cint;
import org.fz.nettyx.serializer.basic.cpp.signed.cppint;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Measures schema serialization and deserialization without including XML configuration loading.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-08-22
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class SchemaSerializerBenchmark
{

    private static final String UTF8_MESSAGE = "Nettyx \u4E32\u53E3 message payload";
    private static final String CHAR_MESSAGE = "Nettyx hot path";
    private static final int    CHAR_FIELD_LENGTH = 32;
    private static final int    BASIC_VALUE = 0x12345678;

    private static final SchemaContext CONTEXT = SchemaContext.load(
            "configured/device.xml", "configured/geo.xml");
    private static final SchemaContext HOT_PATH_CONTEXT = SchemaContext.load(
            "configured/benchmark-hot-paths.xml");

    private byte[]              bytes;
    private ByteBuf             reading;
    private ByteBuf             writing;
    private ByteBuf             stringWriting;
    private Map<String, Object> message;
    private SchemaSerializer    charSerializer;
    private SchemaSerializer    cintSerializer;
    private SchemaSerializer    cppintSerializer;
    private ByteBuf              heapCharReading;
    private ByteBuf              directCharReading;
    private ByteBuf              cintWriting;
    private ByteBuf              cppintWriting;
    private Map<String, Object>  cintMessage;
    private Map<String, Object>  cppintMessage;

    @Setup
    public void setup()
    {
        setupConfiguredSchemaBenchmark();
        setupCharReadBenchmarks();
        setupBasicWriteBenchmarks();
    }

    private void setupConfiguredSchemaBenchmark()
    {
        bytes   = Arrays.copyOf(new byte[]{
                0x44, 0x33, 0x22, 0x11,
                (byte) 0xBB, (byte) 0xAA,
                (byte) 0xCD, (byte) 0xCC, (byte) 0xCC, (byte) 0xCC, (byte) 0xCC, 0x4C, 0x42, 0x40,
                'n', 'e', 't', 't', 'y', 0, 0, 0,
                0x11, 0x22,
                1, 0, 2, 0, 3, 0,
                0, 0, 0, 100,
                0, 0, 0, (byte) 200
        }, 122);
        reading = Unpooled.wrappedBuffer(bytes);
        writing = Unpooled.buffer(bytes.length);
        stringWriting = Unpooled.buffer(UTF8_MESSAGE.length() * 3);
        message = SchemaSerializer.toMap(CONTEXT, "device.BenchmarkDevice", reading);
    }

    private void setupCharReadBenchmarks()
    {
        charSerializer = HOT_PATH_CONTEXT.serializer("benchmark.CharMessage");
        byte[] textBytes = CHAR_MESSAGE.getBytes(StandardCharsets.UTF_8);
        byte[] charPayload = new byte[CHAR_FIELD_LENGTH];
        System.arraycopy(textBytes, 0, charPayload, 0, textBytes.length);

        heapCharReading = Unpooled.wrappedBuffer(charPayload);
        directCharReading = Unpooled.directBuffer(CHAR_FIELD_LENGTH, CHAR_FIELD_LENGTH);
        directCharReading.writeBytes(charPayload);
        if (directCharReading.hasArray())
            throw new IllegalStateException("direct CHAR benchmark buffer unexpectedly has an array");

        Map<String, Object> heapValue = charSerializer.doDeserialize(heapCharReading);
        Map<String, Object> directValue = charSerializer.doDeserialize(directCharReading);
        if (!CHAR_MESSAGE.equals(heapValue.get("value"))
            || !heapValue.equals(directValue))
            throw new IllegalStateException("heap and direct CHAR benchmark inputs decoded differently");

        heapCharReading.readerIndex(0);
        directCharReading.readerIndex(0);
    }

    private void setupBasicWriteBenchmarks()
    {
        cintSerializer = HOT_PATH_CONTEXT.serializer("benchmark.CIntMessage");
        cppintSerializer = HOT_PATH_CONTEXT.serializer("benchmark.CppIntMessage");
        if (cintSerializer.getSchema().fields().get(0).basicType() != cint.class
            || cppintSerializer.getSchema().fields().get(0).basicType() != cppint.class)
            throw new IllegalStateException("Basic benchmark schemas resolved to unexpected types");

        cintMessage = Map.of("value", BASIC_VALUE);
        cppintMessage = Map.of("value", BASIC_VALUE);
        cintWriting = Unpooled.buffer(Integer.BYTES, Integer.BYTES);
        cppintWriting = Unpooled.buffer(Integer.BYTES, Integer.BYTES);
        cintSerializer.doSerialize(cintMessage, cintWriting);
        cppintSerializer.doSerialize(cppintMessage, cppintWriting);
        if (!Arrays.equals(copyReadableBytes(cintWriting), copyReadableBytes(cppintWriting)))
            throw new IllegalStateException("cint and cppint benchmark schemas produced different bytes");

        cintWriting.clear();
        cppintWriting.clear();
    }

    @Benchmark
    public Map<String, Object> benchmarkDeserialize()
    {
        reading.readerIndex(0);
        return SchemaSerializer.toMap(CONTEXT, "device.BenchmarkDevice", reading);
    }

    @Benchmark
    public Map<String, Object> benchmarkDeserializeCharHeap()
    {
        heapCharReading.readerIndex(0);
        return charSerializer.doDeserialize(heapCharReading);
    }

    @Benchmark
    public Map<String, Object> benchmarkDeserializeCharDirect()
    {
        directCharReading.readerIndex(0);
        return charSerializer.doDeserialize(directCharReading);
    }

    @Benchmark
    public ByteBuf benchmarkSerialize()
    {
        writing.clear();
        SchemaSerializer.toByteBuf(CONTEXT, "device.BenchmarkDevice", message, writing);
        return writing;
    }

    @Benchmark
    public int benchmarkSerializeCint()
    {
        cintWriting.clear();
        cintSerializer.doSerialize(cintMessage, cintWriting);
        return cintWriting.readableBytes();
    }

    @Benchmark
    public int benchmarkSerializeCppint()
    {
        cppintWriting.clear();
        cppintSerializer.doSerialize(cppintMessage, cppintWriting);
        return cppintWriting.readableBytes();
    }

    @Benchmark
    public int benchmarkUtf8GetBytes()
    {
        stringWriting.clear();
        stringWriting.writeBytes(UTF8_MESSAGE.getBytes(StandardCharsets.UTF_8));
        return stringWriting.readableBytes();
    }

    @Benchmark
    public int benchmarkUtf8WriteCharSequence()
    {
        stringWriting.clear();
        stringWriting.writeCharSequence(UTF8_MESSAGE, StandardCharsets.UTF_8);
        return stringWriting.readableBytes();
    }

    @TearDown(Level.Trial)
    public void tearDown()
    {
        release(reading);
        release(writing);
        release(stringWriting);
        release(heapCharReading);
        release(directCharReading);
        release(cintWriting);
        release(cppintWriting);
    }

    private static byte[] copyReadableBytes(ByteBuf buffer)
    {
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.getBytes(buffer.readerIndex(), bytes);
        return bytes;
    }

    private static void release(ByteBuf buffer)
    {
        if (buffer != null) buffer.release();
    }

    public static void main(String[] args) throws RunnerException
    {
        Options options = new OptionsBuilder()
                .include(SchemaSerializerBenchmark.class.getSimpleName())
                .addProfiler("gc")
                .build();
        new Runner(options).run();
    }
}
