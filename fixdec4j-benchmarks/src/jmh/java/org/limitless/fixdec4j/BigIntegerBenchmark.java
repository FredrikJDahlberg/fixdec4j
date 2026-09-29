package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class BigIntegerBenchmark {

    BigInteger value = BigInteger.valueOf(IntegerOperands.VALUE);
    BigInteger operand = BigInteger.valueOf(IntegerOperands.OPERAND);

    @Benchmark
    public BigInteger baseline() {
        return value;
    }

    @Benchmark
    public BigInteger add() {
        return value.add(operand);
    }

    @Benchmark
    public BigInteger subtract() {
        return value.subtract(operand);
    }

    @Benchmark
    public BigInteger multiply() {
        return value.multiply(operand);
    }

    @Benchmark
    public BigInteger divide() {
        return value.divide(operand);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(BigIntegerBenchmark.class.getSimpleName()).build()).run();
    }
}
