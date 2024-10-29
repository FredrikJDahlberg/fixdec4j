package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Thread)
@Fork(jvmArgsAppend = "-server")
public class FixedBenchmark {

    @State(Scope.Benchmark)
    public static class AddState {
        long a;
        long b;
        @Setup
        public void setup() {
            a = DecimalFlyweight.valueOf(10_000_000_000_000000L, -6);
            b = DecimalFlyweight.valueOf(20_000_000_000_000000L, -6);
        }
        @TearDown
        public void tearDown() {
            a = 0;
            b = 0;
        }
    }

    @State(Scope.Benchmark)
    public static class MultState {
        long a;
        long b;
        @Setup
        public void setup() {
//            a = FixedDecimal.of(10_000_000_000_000000L, -6);
//            b = FixedDecimal.of(1_1234567, -6);
//            a = FixedDecimal.of(10_000_000_000000L, -6);
//            b = FixedDecimal.of(1_1234567, -6);
            a = DecimalFlyweight.valueOf(1_000_000_0000L, -4);
            b = DecimalFlyweight.valueOf(1_1234, -4);

        }
        @TearDown
        public void tearDown() {
            a = 0;
            b = 0;
        }
    }

    @State(Scope.Benchmark)
    public static class DivState {
        long a;
        long b;
        @Setup
        public void setup() {
//            a = FixedDecimal.of(25_000_000_000_000000L, -6);
//            b = FixedDecimal.of(23_123_456_000_000000L, -6);
            a = DecimalFlyweight.valueOf(25_000_000_000L, -3);
            b = DecimalFlyweight.valueOf(23_123_000_000L, -3);
        }

        @TearDown
        public void tearDown() {
            a = 0;
            b = 0;
        }
    }

    //    @CompilerControl(CompilerControl.Mode.INLINE)
    public long add(final long a, final long b) {
        return DecimalFlyweight.add(a, b);
    }

    //    @CompilerControl(CompilerControl.Mode.INLINE)
    public long multiply(final long a, final long b) {
        return DecimalFlyweight.multiply(a, b, DecimalRounding.UP);
    }

    //    @CompilerControl(CompilerControl.Mode.INLINE)
    public long divide(final long a, final long b) {
        return DecimalFlyweight.divide(a, b, DecimalRounding.UP);
    }

    @Benchmark
    public final void add(AddState state, Blackhole black) {
        black.consume(add(state.a, state.b));
    }

    @Benchmark
    public final void multiply(MultState state, Blackhole black) {
        black.consume(multiply(state.a, state.b));
    }

    @Benchmark
    public final void divide(DivState state, Blackhole black) {
        black.consume(divide(state.a, state.b));
    }
}
