package org.fz.nettyx.serializer.struct.jmh;

import cn.hutool.core.lang.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.codec.model.You;
import org.fz.nettyx.serializer.struct.StructSerializerContext;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static org.fz.nettyx.serializer.struct.StructSerializer.toByteBuf;
import static org.fz.nettyx.serializer.struct.StructSerializer.toStruct;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class StructSerializerBenchmark {

    private static final TypeReference<You> YOU_TYPE = new TypeReference<>() {};

    private byte[] bytes;
    private You    struct;

    static {
        new StructSerializerContext("org.fz.nettyx.codec.model");
    }

    @Setup
    public void setup() {
        bytes = new byte[900];
        Arrays.fill(bytes, (byte) 67);

        You you = toStruct(YOU_TYPE, Unpooled.wrappedBuffer(bytes));
        you.setC(null);
        you.setChunk(null);
        struct = you;
    }

    @Benchmark
    public You benchmarkDeserialize() {
        return toStruct(YOU_TYPE, Unpooled.wrappedBuffer(bytes));
    }

    @Benchmark
    public byte[] benchmarkSerialize() {
        ByteBuf buf = Unpooled.buffer();
        toByteBuf(YOU_TYPE, struct, buf);
        byte[] result = new byte[buf.readableBytes()];
        buf.readBytes(result);
        return result;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(StructSerializerBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
