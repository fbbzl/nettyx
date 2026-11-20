package org.fz.nettyx.serializer.struct.comparison;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.nio.file.Path;

/**
 * Starts the JMH benchmarks for the generic type resolver comparison.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-11-11
 */
public final class RunGenericLibraryBenchmark {

    private RunGenericLibraryBenchmark() {}

    public static void main(String[] args) throws Exception {
        String variant = args[0];
        Path result = Path.of(args[1]);
        String benchmark = GenericStructRoundTripBenchmark.class.getSimpleName() + ".*";
        if (variant.equals("geantyref")) {
            benchmark = GenericTypeResolverBenchmark.class.getSimpleName() + ".*|" + benchmark;
        }
        Options options = new OptionsBuilder()
                .include(benchmark)
                .addProfiler("gc")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .result(result.toString())
                .build();
        new Runner(options).run();
    }
}
