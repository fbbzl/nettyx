package org.fz.nettyx.serializer.struct.comparison;

import com.google.common.reflect.TypeToken;
import org.fz.nettyx.codec.model.User;
import org.openjdk.jmh.annotations.*;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.util.concurrent.TimeUnit;

/**
 * Measures type resolution across different libraries and context lifetimes.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-11
 */
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Fork(value = 2, jvmArgsAppend = {"-Xms512m", "-Xmx512m"})
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
public class GenericTypeResolverBenchmark {

    /**
     * Provides benchmark inputs for resolving types with a new context on each call.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    @State(Scope.Thread)
    public static class FreshContext {
        @Param({"GEANTYREF", "GUAVA"})
        public GenericTypeCandidates.Provider provider;
        public Type declared;

        @Setup
        public void setup() throws Exception {
            if (!GenericTypeContractsTest.eligible(GenericTypeContractsTest.inspect(provider))) {
                throw new IllegalArgumentException("Provider does not preserve the nested Type contract: " + provider);
            }
            declared = ((GenericArrayType) User.class.getDeclaredField("bs2d").getGenericType()).getGenericComponentType();
        }
    }

    /**
     * Shares one Guava type context across benchmark calls.
     *
     * @author fengbinbin
     * @version 1.0
     * @since 2026-11-11
     */
    @State(Scope.Thread)
    public static class ReusedGuavaContext {
        public TypeToken<?> token;
        public Type declared;

        @Setup
        public void setup() throws Exception {
            token = TypeToken.of(GenericTypeContractsTest.ROOT);
            declared = ((GenericArrayType) User.class.getDeclaredField("bs2d").getGenericType()).getGenericComponentType();
        }
    }

    @Benchmark
    public Type resolveWithFreshContext(FreshContext state) {
        return GenericTypeCandidates.resolve(state.provider, GenericTypeContractsTest.ROOT, state.declared, User.class);
    }

    @Benchmark
    public Type resolveWithReusedGuavaToken(ReusedGuavaContext state) {
        return state.token.resolveType(state.declared).getType();
    }
}
