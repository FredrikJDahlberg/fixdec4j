package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
public class DoubleBenchmark {

    @State(Scope.Benchmark)
    public static class AddState {
        double a;
        double b;
        @Setup
        public void setup() {
            a = 0.1D;
            b = 0.2D;
        }
        @TearDown
        public void tearDown() {
            a = 0D;
            b = 0D;
        }
    }

    @State(Scope.Benchmark)
    public static class MultState {
        double a;
        double b;
        @Setup
        public void setup() {
            a = 10_000_000_000.000000D;
            b = 1.1234567;
        }
        @TearDown
        public void tearDown() {
            a = 0D;
            b = 0D;
        }
    }

    @State(Scope.Benchmark)
    public static class DivState {
        double a;
        double b;
        @Setup
        public void setup() {
            a = 25_000_000_000.000000D;
            b = 23_123_456_000.000000D;
        }
        @TearDown
        public void tearDown() {
            a = 0D;
            b = 0D;
        }
    }


    @Benchmark
    public void add(AddState state, Blackhole black) {
        black.consume(Math.round((state.a + state.b) * 10_000D) / 10000D);
    }

    @Benchmark
    public void multiply(MultState state, Blackhole black) {
        black.consume(Math.round(state.a * state.b * 10_000D) / 10_000D);
    }

    @Benchmark
    public void divide(DivState state, Blackhole black) {
        black.consume( Math.round(10_000D * state.a / state.b) / 10_000D);
    }


}
