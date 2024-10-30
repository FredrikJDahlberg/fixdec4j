package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
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
public class MutableDecimal64Benchmark {

    MutableDecimal64 value = MutableDecimal64.valueOf(12312310_00L, -2);
    MutableDecimal64 decimal = MutableDecimal64.valueOf(3452, 5);

    @Benchmark
    public MutableDecimal64 baseline() {
        return MutableDecimal64.ZERO;
    }

    @Benchmark
    public MutableDecimal64 add() {
        return value.add(decimal);
    }

    @Benchmark
    public MutableDecimal64 subtract() {
        return value.subtract(decimal);
    }

    @Benchmark
    public MutableDecimal64 multiply() {
        return value.multiply(decimal);
    }

    @Benchmark
    public MutableDecimal64 divide() {
        return value.divide(decimal, DecimalRounding.UP);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(MutableDecimal64Benchmark.class.getSimpleName()).build()).run();
    }
}
