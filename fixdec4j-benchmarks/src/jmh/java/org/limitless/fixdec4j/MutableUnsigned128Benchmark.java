package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(jvmArgsAppend = "-server", value = 1)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 5)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class MutableUnsigned128Benchmark {

    MutableUnsigned128 unsigned = new MutableUnsigned128(100_0000L, 200_000L);
    MutableUnsigned128 factor = new MutableUnsigned128(400_000L);
    MutableUnsigned128.Context context = new MutableUnsigned128.Context(DecimalRounding.UP);

    @Benchmark
    public MutableUnsigned128 baseline() {
        return unsigned;
    }

    @Benchmark
    public MutableUnsigned128 add() {
        return unsigned.add(factor);
    }

    @Benchmark
    public MutableUnsigned128 subtract() {
        return unsigned.subtract(factor);
    }

    @Benchmark
    public MutableUnsigned128 multiply() {
        return unsigned.multiply(factor);
    }

    @Benchmark
    public MutableUnsigned128 divide() {
        return unsigned.divide(factor, context);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(MutableDecimal64Benchmark.class.getSimpleName()).build()).run();
    }
}
