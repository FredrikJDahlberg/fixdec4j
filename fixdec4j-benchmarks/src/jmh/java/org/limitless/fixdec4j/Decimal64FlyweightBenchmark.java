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
@Measurement(iterations = 2, time = 10)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class Decimal64FlyweightBenchmark {

    long value = Decimal64Flyweight.valueOf(2394234_000000L, -6);
    long decimal = Decimal64Flyweight.valueOf(20_000000L, -6);

    @Benchmark
    public long baseline() {
        return 0;
    }

    @Benchmark
    public long add() {
        return Decimal64Flyweight.add(value, decimal);
    }

    @Benchmark
    public long subtract() {
        return Decimal64Flyweight.subtract(value, decimal);
    }

    @Benchmark
    public long multiply() {
        return Decimal64Flyweight.multiply(value, decimal, DecimalRounding.UP);
    }

    @Benchmark
    public long divide() {
        return Decimal64Flyweight.divide(value, decimal, DecimalRounding.UP);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(Decimal64FlyweightBenchmark.class.getSimpleName()).build()).run();
    }
}
