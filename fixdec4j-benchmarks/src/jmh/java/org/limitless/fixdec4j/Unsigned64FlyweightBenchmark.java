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
@Measurement(iterations = 2, time = 10)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class Unsigned64FlyweightBenchmark {

    long value = 12321123132L;
    long decimal = 5674564L;

    @Benchmark
    public long baseline() {
        return 0;
    }

    @Benchmark
    public long add() {
        return Unsigned64Flyweight.add(value, decimal);
    }

    @Benchmark
    public long subtract() {
        return Unsigned64Flyweight.subtract(value, decimal);
    }

    @Benchmark
    public long multiply() {
        return Unsigned64Flyweight.multiply(value, decimal);
    }

    @Benchmark
    public long divide() {
        return Unsigned64Flyweight.divide(value, decimal);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(Unsigned64FlyweightBenchmark.class.getSimpleName()).build()).run();
    }
}
