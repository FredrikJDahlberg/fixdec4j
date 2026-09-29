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
public class DoubleBenchmark {

    @Param
    Operands operands;

    // Results are rounded to the largest number of decimals of the operands, matching the fixed
    // decimal implementations. Doubles are not exact: rounding a value above 2^53 (e.g. the LARGE
    // product scaled by 10^6) can be off in the last decimal, so results are not verified.
    double rounding;
    double value1;
    double value2;

    @Setup
    public void setup() {
        rounding = Math.pow(10, operands.decimals());
        value1 = operands.mantissa1 / Math.pow(10, operands.decimals1);
        value2 = operands.mantissa2 / Math.pow(10, operands.decimals2);
    }

    @Benchmark
    public double baseline() {
        return value1;
    }

    @Benchmark
    public double add() {
        return Math.round(rounding * (value1 + value2)) / rounding;
    }

    @Benchmark
    public double subtract() {
        return Math.round(rounding * (value1 - value2)) / rounding;
    }

    @Benchmark
    public double multiply() {
        return Math.round(rounding * (value1 * value2)) / rounding;
    }

    @Benchmark
    public double divide() {
        return Math.round(rounding * (value1 / value2)) / rounding;
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(DoubleBenchmark.class.getSimpleName()).build()).run();
    }
}
