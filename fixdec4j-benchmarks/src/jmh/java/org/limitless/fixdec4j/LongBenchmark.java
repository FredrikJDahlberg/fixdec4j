package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(jvmArgsAppend = "-server", value = 1)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 10)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class LongBenchmark {

    long value1 = 4_711_000_000L;
    long value2 = 125_245L;

    @Benchmark
    public long baseline() {
        return 0;
    }

    @Benchmark
    public long add() {
        return value1 + value2;
    }

    @Benchmark
    public long subtract() {
        return value1 - value2;
    }

    @Benchmark
    public long multiply() {
        return value1 * value2;
    }

    @Benchmark
    public long divide() {
        return value1 / value2;
    }
}
