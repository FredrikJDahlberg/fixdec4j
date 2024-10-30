package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(jvmArgsAppend = "-server", value = 1)
@Warmup(iterations = 2, time = 5)
@Measurement(iterations = 2, time = 10)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class BigDecimalBenchmark {

    @State(Scope.Benchmark)
    public static class BigDecState {
        BigDecimal n;
        BigDecimal m;
        BigDecimal t;

        @Setup
        public void setup() {
            n = BigDecimal.ZERO;
            m = BigDecimal.valueOf(100_000_000_000L);
            t = BigDecimal.valueOf(2);
        }


        @TearDown
        public void tearDown() {
        }
    }

    @Benchmark
    public void baseline() {
    }

    @Benchmark
    public void add(BigDecState state, Blackhole black) {
        black.consume(state.n.add(state.m));
    }

    @Benchmark
    public void minus(BigDecState state, Blackhole black) {
        black.consume(state.n.subtract(state.m));
    }

    @Benchmark
    public void multiply(BigDecState state, Blackhole black) {
        black.consume(state.t.multiply(state.t));
    }

    @Benchmark
    public void divide(BigDecState state, Blackhole black) {
        black.consume(state.m.divide(state.t));
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(BigDecimalBenchmark.class.getSimpleName()).build()).run();
    }
}
