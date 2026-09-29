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
public class Decimal64Benchmark {

    @Param
    Operands operands;

    Decimal64 value;
    Decimal64 decimal;
    Decimal64.Context context = new Decimal64.Context(DecimalRounding.UP);

    @Setup
    public void setup() {
        value = Decimal64.fromLongBits(operands.value1());
        decimal = Decimal64.fromLongBits(operands.value2());
        operands.verify(add().toLongBits(), subtract().toLongBits(), multiply().toLongBits(), divide().toLongBits());
    }

    @Benchmark
    public Decimal64 baseline() {
        return value;
    }

    @Benchmark
    public Decimal64 add() {
        return value.add(decimal);
    }

    @Benchmark
    public Decimal64 subtract() {
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
