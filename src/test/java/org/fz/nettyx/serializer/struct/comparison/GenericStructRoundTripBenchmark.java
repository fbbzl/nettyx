package org.fz.nettyx.serializer.struct.comparison;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.codec.model.User;
import org.fz.nettyx.serializer.struct.StructContext;
import org.fz.nettyx.serializer.struct.StructSerializer;
import org.openjdk.jmh.annotations.*;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Measures struct serialization and deserialization using different generic type resolvers.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-11
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(value = 2, jvmArgsAppend = {"-Xms512m", "-Xmx512m"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class GenericStructRoundTripBenchmark {

    private StructSerializer serializer;
    private ByteBuf input;
    private ByteBuf output;

    @Setup
    public void setup() {
        new StructContext("org.fz.nettyx.codec.model");
        serializer = new StructSerializer(GenericTypeContractsTest.ROOT);
        output = Unpooled.buffer();
        serializer.doSerialize(new User<>(), output);
        byte[] bytes = new byte[output.readableBytes()];
        Random random = new Random(4201);
        for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) (33 + random.nextInt(94));
        if (bytes.length == 0) throw new IllegalStateException("User payload is empty");
        input = Unpooled.wrappedBuffer(bytes);
        Object value = serializer.doDeserialize(input);
        output.clear();
        serializer.doSerialize(value, output);
        input.readerIndex(0);
        if (!ByteBufUtil.equals(input, output)) throw new IllegalStateException("User bytes changed in setup");
        System.out.println("roundTripPayloadBytes=" + bytes.length);
    }

    @Benchmark
    public int roundTrip() {
        input.readerIndex(0);
        Object value = serializer.doDeserialize(input);
        output.clear();
        serializer.doSerialize(value, output);
        return output.writerIndex();
    }

    @TearDown
    public void teardown() {
        if (input != null) input.release();
        if (output != null) output.release();
    }
}
