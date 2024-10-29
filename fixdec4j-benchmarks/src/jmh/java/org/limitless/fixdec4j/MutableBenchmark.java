package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
public class MutableBenchmark {

    public static class Mutable {

        private long value;

        public static Mutable valueOf(final long input) {
            Mutable value = new Mutable();
            value.value = input;
            return value;
        }

        public Mutable operation(final long input) {
            this.value *= input;
            return this;
        }
    }

    public static class Immutable {

        private final long value;

        public static Immutable valueOf(final long input) {
            return new Immutable(input);
        }

        private Immutable(final long input) {
            this.value = input;
        }

        public Immutable operation(final int input) {
            return new Immutable(input * value);
        }
    }

    @State(Scope.Benchmark)
    public static class ImmutableState {
        Immutable i;

        @Setup
        public void setup() {
            i = Immutable.valueOf(4711);
        }

        @TearDown
        public void tearDown() {
            i = null;
        }
    }

    @State(Scope.Benchmark)
    public static class MutableState {
        Mutable m;

        @Setup
        public void setup() {
            m = Mutable.valueOf(4712);
        }

        @TearDown
        public void tearDown() {
            m = null;
        }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void immutable(ImmutableState state, Blackhole black) {
        black.consume(state.i.operation(1000));
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    @OutputTimeUnit(TimeUnit.NANOSECONDS)
    public void mutable(MutableState state, Blackhole black) {
        black.consume(state.m.operation(6000));
    }
}
