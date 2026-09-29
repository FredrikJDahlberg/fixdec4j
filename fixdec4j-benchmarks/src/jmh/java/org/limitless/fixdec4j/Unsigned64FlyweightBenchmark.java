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
public class Unsigned64FlyweightBenchmark {

    long value = IntegerOperands.VALUE;
    long operand = IntegerOperands.OPERAND;

    @Benchmark
    public long baseline() {
        return value;
    }

    @Benchmark
    public long add() {
        return Unsigned64Flyweight.add(value, operand);
    }

    @Benchmark
    public long subtract() {
        return Unsigned64Flyweight.subtract(value, operand);
    }

    @Benchmark
    public long multiply() {
        return Unsigned64Flyweight.multiply(value, operand);
    }

    @Benchmark
    public long divide() {
        return Unsigned64Flyweight.divide(value, operand);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(Unsigned64FlyweightBenchmark.class.getSimpleName()).build()).run();
    }
}
