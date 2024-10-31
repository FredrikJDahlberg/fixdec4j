package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(jvmArgsAppend = "-server", value = 1)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 10)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class Decimal64Benchmark {

    Decimal64 value = Decimal64.valueOf(10_888_800L, -2);
    Decimal64 decimal = Decimal64.valueOf(289, 5);
    Decimal64.Context context = new Decimal64.Context(DecimalRounding.UP);

    @Benchmark
    public Decimal64 baseline() {
        return Decimal64.ZERO;
    }

    @Benchmark
    public Decimal64 add() {
        return value.add(decimal);
    }

    @Benchmark
    public Decimal64 minus() {
        return value.subtract(decimal);
    }

    @Benchmark
    public Decimal64 multiply() {
        return value.multiply(decimal, context);
    }

    @Benchmark
    public Decimal64 divide() {
        return value.divide(decimal, context);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(Decimal64Benchmark.class.getSimpleName()).build()).run();
    }
}
