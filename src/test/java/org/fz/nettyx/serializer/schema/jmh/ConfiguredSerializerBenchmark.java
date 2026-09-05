package org.fz.nettyx.serializer.schema.jmh;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.schema.ConfiguredSerializer;
import org.fz.nettyx.serializer.schema.StructConfigRegistry;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Measures configured-struct deserialization without including XML configuration loading.
 *
 * @author fengbinbin
 * @since 2026-08-22
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class ConfiguredSerializerBenchmark
{

    private static final StructConfigRegistry REGISTRY = StructConfigRegistry.load(
            "configured/device.xml", "configured/geo.xml");

    private byte[]              bytes;
    private ByteBuf             reading;
    private ByteBuf             writing;
    private Map<String, Object> message;

    @Setup
    public void setup()
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
        message = ConfiguredSerializer.toStruct(REGISTRY, "device.BenchmarkDevice", reading);
    }

    @Benchmark
    public Map<String, Object> benchmarkDeserialize()
    {
        reading.readerIndex(0);
        return ConfiguredSerializer.toStruct(REGISTRY, "device.BenchmarkDevice", reading);
    }

    @Benchmark
    public ByteBuf benchmarkSerialize()
    {
        writing.clear();
        ConfiguredSerializer.toByteBuf(REGISTRY, "device.BenchmarkDevice", message, writing);
        return writing;
    }

    public static void main(String[] args) throws RunnerException
    {
        Options options = new OptionsBuilder()
                .include(ConfiguredSerializerBenchmark.class.getSimpleName())
                .build();
        new Runner(options).run();
    }
}