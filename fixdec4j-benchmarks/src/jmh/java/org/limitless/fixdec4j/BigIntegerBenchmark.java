package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(jvmArgsAppend = "-server", value = 1)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 10)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class BigIntegerBenchmark {

    BigInteger value = BigInteger.valueOf(123123132);
    BigInteger integer = BigInteger.valueOf(4711);

    @Benchmark
    public BigInteger baseline() {
        return BigInteger.ONE;
    }

    @Benchmark
    public BigInteger add() {
        return value.add(integer);
    }

    @Benchmark
    public BigInteger subtract() {
        return value.subtract(integer);
    }

    @Benchmark
    public BigInteger multiply() {
        return value.multiply(integer);
    }

    @Benchmark
    public BigInteger divide() {
        return value.divide(integer);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(BigIntegerBenchmark.class.getSimpleName()).build()).run();
    }
}
