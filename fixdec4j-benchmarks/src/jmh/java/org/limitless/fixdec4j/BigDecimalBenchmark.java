package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class BigDecimalBenchmark {

    @Param
    Operands operands;

    // Multiply and divide round to the largest number of decimals of the operands, matching the
    // fixed decimal implementations.
    BigDecimal value;
    BigDecimal decimal;
    int decimals;

    @Setup
    public void setup() {
        value = operands.bigValue1();
        decimal = operands.bigValue2();
        decimals = operands.decimals();
    }

    @Benchmark
    public BigDecimal baseline() {
        return value;
    }

    @Benchmark
    public BigDecimal add() {
        return value.add(decimal);
    }

    @Benchmark
    public BigDecimal subtract() {
        return value.subtract(decimal);
    }

    @Benchmark
    public BigDecimal multiply() {
        return value.multiply(decimal).setScale(decimals, RoundingMode.HALF_UP);
    }

    @Benchmark
    public BigDecimal divide() {
        return value.divide(decimal, decimals, RoundingMode.HALF_UP);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(BigDecimalBenchmark.class.getSimpleName()).build()).run();
    }
}
