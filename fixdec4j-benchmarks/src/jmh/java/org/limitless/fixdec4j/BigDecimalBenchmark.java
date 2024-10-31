package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(jvmArgsAppend = "-server", value = 1)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 10)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class BigDecimalBenchmark {

    BigDecimal value = BigDecimal.valueOf(1231231231, -5);
    BigDecimal decimal = BigDecimal.valueOf(12334, 2);

    @Benchmark
    public BigDecimal baseline() {
        return BigDecimal.ZERO;
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
        return value.multiply(decimal);
    }

    @Benchmark
    public BigDecimal divide() {
        return value.divide(decimal, RoundingMode.UP);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(BigDecimalBenchmark.class.getSimpleName()).build()).run();
    }
}
