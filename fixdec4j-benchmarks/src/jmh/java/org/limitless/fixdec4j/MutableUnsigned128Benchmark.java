package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class MutableUnsigned128Benchmark {

    // Zero high bits are comparable with the 64-bit integer benchmarks, non-zero high bits
    // exercise the 128-bit arithmetic.
    @Param({"0", "1000000"})
    long highBits;

    // The operations mutate their receiver, so each invocation starts by resetting the result
    // to the value; otherwise the value drifts (e.g. divide converges to zero).
    MutableUnsigned128 value;
    MutableUnsigned128 operand = new MutableUnsigned128(IntegerOperands.OPERAND);
    MutableUnsigned128 result = new MutableUnsigned128();
    MutableUnsigned128.Context context = new MutableUnsigned128.Context();

    @Setup
    public void setup() {
        value = new MutableUnsigned128(highBits, IntegerOperands.VALUE);
    }

    @Benchmark
    public MutableUnsigned128 baseline() {
        return result.set(value);
    }

    @Benchmark
    public MutableUnsigned128 add() {
        return result.set(value).add(operand);
    }

    @Benchmark
    public MutableUnsigned128 subtract() {
        return result.set(value).subtract(operand);
    }

    @Benchmark
    public MutableUnsigned128 multiply() {
        return result.set(value).multiply(operand);
    }

    @Benchmark
    public MutableUnsigned128 divide() {
        return result.set(value).divide(operand, context);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(MutableUnsigned128Benchmark.class.getSimpleName()).build()).run();
    }
}
