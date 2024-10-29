package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
public class BaseBenchmark {

/*
    @State(Scope.Benchmark)
    public static class AddState {
        Fixed a;
        Fixed b;

        @Setup
        public void setup() {
            a = Fixed.valueOf(0.1D, 10);
            b = Fixed.valueOf(0.2D, 10);
        }
        @TearDown
        public void tearDown() {
            a = Fixed.valueOf(0D, 10);
            b = Fixed.valueOf(0D, 10);
        }
    }

    @State(Scope.Benchmark)
    public static class MultState {
        Fixed a;
        Fixed b;
        @Setup
        public void setup() {
            a = Fixed.valueOf(10_000_000_000.000000D, 1_000_000);
            b = Fixed.valueOf(1.1234567D, 1_000_000);
        }
        @TearDown
        public void tearDown() {
            a = Fixed.valueOf(0D, 0);
            b = Fixed.valueOf(0D, 0);
        }
    }

    @State(Scope.Benchmark)
    public static class DivState {
        Fixed a;
        Fixed b;
        @Setup
        public void setup() {
            a = Fixed.valueOf(25_000_000_000.000000D, 1_000_000);
            b = Fixed.valueOf(23_123_456_000.000000D, 1_000_000);
        }
        @TearDown
        public void tearDown() {
            a = Fixed.valueOf(0D, 0);
            b = Fixed.valueOf(0D, 0);
        }
    }

    @Benchmark
    public void add(AddState state, Blackhole black) {
        black.consume(state.a.add(state.b));
    }

    @Benchmark
    public void multiply(MultState state, Blackhole black) {
        black.consume(state.a.multiply(state.b));
    }

    @Benchmark
    public void divide(DivState state, Blackhole black) {
        black.consume(state.a.divide(state.b));
    }
*/

}
