package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
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

    @State(Scope.Benchmark)
    public static class BigIntState {
        BigInteger n;
        BigInteger m;
        BigInteger t;

        @Setup
        public void setup() {
            n = BigInteger.ZERO;
            m = BigInteger.valueOf(Long.MAX_VALUE);
            t = BigInteger.valueOf(2);
        }


        @TearDown
        public void tearDown() {
        }
    }

    @Benchmark
    public void baseline() {
    }

    @Benchmark
    public void add(BigIntState state, Blackhole black) {
        black.consume(state.n.add(state.m));
    }

    @Benchmark
    public void minus(BigIntState state, Blackhole black) {
        black.consume(state.n.subtract(state.m));
    }

    @Benchmark
    public void multiply(BigIntState state, Blackhole black) {
        black.consume(state.t.multiply(state.t));
    }

    @Benchmark
    public void divide(BigIntState state, Blackhole black) {
        black.consume(state.m.divide(state.t));
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(BigIntegerBenchmark.class.getSimpleName()).build()).run();
    }
}
