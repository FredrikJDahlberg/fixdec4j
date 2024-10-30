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
@Measurement(iterations = 2, time = 5)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class DoubleBenchmark {

    double rounding = 10_000D;
    double value1 = 4711.2345D;
    double value2 = 34566.12344D;

    @Benchmark
    public double baseline() {
        return 0.0D;
    }

    @Benchmark
    public double add() {
        return Math.round(rounding * (value1 + value2)) / rounding;
    }

    @Benchmark
    public double minus() {
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
