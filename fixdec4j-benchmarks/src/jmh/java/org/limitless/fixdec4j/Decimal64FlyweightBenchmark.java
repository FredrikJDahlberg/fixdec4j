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
public class Decimal64FlyweightBenchmark {

    long value64 = Decimal64Flyweight.valueOf(1231231231, -5);
    long decimal64 = Decimal64Flyweight.valueOf(12334, -2);

    long value128 = Decimal64Flyweight.valueOf(1231231231, -5);
    long decimal128 = Decimal64Flyweight.valueOf(12334, 2);

    long value = Decimal64Flyweight.valueOf(50_00000, -5);
    long decimal = Decimal64Flyweight.valueOf(1_25000, -5);

    Decimal64.Context context = new Decimal64.Context(DecimalRounding.UP);

    @Benchmark
    public long baseline() {
        return 0;
    }

    @Benchmark
    public long add() {
        return Decimal64Flyweight.add(value64, decimal64);
    }

    @Benchmark
    public long subtract() {
        return Decimal64Flyweight.subtract(value64, decimal64);
    }

    @Benchmark
    public long multiply() {
        return Decimal64Flyweight.multiply(value, decimal, context);
    }

    @Benchmark
    public long multiply64() {
        return Decimal64Flyweight.multiply(value64, decimal64, context);
    }

    @Benchmark
    public long divide64() {
        return Decimal64Flyweight.divide(value64, decimal64, context);
    }

    @Benchmark
    public long multiply128() {
        return Decimal64Flyweight.multiply(value128, decimal128, context);
    }

    @Benchmark
    public long divide128() {
        return Decimal64Flyweight.divide(value128, decimal128, context);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(Decimal64FlyweightBenchmark.class.getSimpleName()).build()).run();
    }
}
