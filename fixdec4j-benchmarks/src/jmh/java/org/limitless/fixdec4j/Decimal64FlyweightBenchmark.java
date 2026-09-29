package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class Decimal64FlyweightBenchmark {

    @Param
    Operands operands;

    long value;
    long decimal;
    Decimal64.Context context = new Decimal64.Context(DecimalRounding.UP);

    @Setup
    public void setup() {
        value = operands.value1();
        decimal = operands.value2();
        operands.verify(add(), subtract(), multiply(), divide());
    }

    @Benchmark
    public long baseline() {
        return value;
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
        return Decimal64Flyweight.multiply(value, decimal, context);
    }

    @Benchmark
    public long divide() {
        return Decimal64Flyweight.divide(value, decimal, context);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(Decimal64FlyweightBenchmark.class.getSimpleName()).build()).run();
    }
}
