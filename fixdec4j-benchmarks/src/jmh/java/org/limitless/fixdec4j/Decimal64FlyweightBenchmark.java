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
public class Decimal64FlyweightBenchmark {

    @State(Scope.Benchmark)
    public static class LongState {
        long a;
        long b;

        @Setup
        public void setup() {
            a = Decimal64Flyweight.valueOf(10_000000L, -6);
            b = Decimal64Flyweight.valueOf(20_000000L, -6);
        }

        @TearDown
        public void tearDown() {
            a = 0;
            b = 0;
        }
    }

    @Benchmark
    public void baseline(LongState state, Blackhole black) {
        black.consume(++state.a);
    }

    @Benchmark
    public void add(LongState state, Blackhole black) {
        black.consume(Decimal64Flyweight.add(state.a, 1));
    }

    @Benchmark
    public void subtract(LongState state, Blackhole black) {
        black.consume(Decimal64Flyweight.subtract(state.a, state.b));
    }

    @Benchmark
    public void multiply(LongState state, Blackhole black) {
        black.consume(Decimal64Flyweight.multiply(state.a, state.b, DecimalRounding.UP));
    }

    @Benchmark
    public void divide(LongState state, Blackhole black) {
        black.consume(Decimal64Flyweight.divide(state.a, state.b, DecimalRounding.UP));
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(Decimal64FlyweightBenchmark.class.getSimpleName()).build()).run();
    }
}
