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
public class MutableDecimal64Benchmark {

    @Param
    Operands operands;

    // The operations mutate their receiver, so each invocation starts by resetting the result
    // to the value; otherwise the value drifts and quickly becomes NaN.
    long value;
    MutableDecimal64 decimal;
    MutableDecimal64 result = new MutableDecimal64();
    DecimalContext context = DecimalContext.HALF_UP;

    @Setup
    public void setup() {
        value = operands.value1();
        decimal = MutableDecimal64.fromLongBits(operands.value2());
        operands.verify(add().toLongBits(), subtract().toLongBits(), multiply().toLongBits(), divide().toLongBits());
    }

    @Benchmark
    public MutableDecimal64 baseline() {
        return result.setLongBits(value);
    }

    @Benchmark
    public MutableDecimal64 add() {
        return result.setLongBits(value).add(decimal);
    }

    @Benchmark
    public MutableDecimal64 subtract() {
        return result.setLongBits(value).subtract(decimal);
    }

    @Benchmark
    public MutableDecimal64 multiply() {
        return result.setLongBits(value).multiply(decimal, context);
    }

    @Benchmark
    public MutableDecimal64 divide() {
        return result.setLongBits(value).divide(decimal, context);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(MutableDecimal64Benchmark.class.getSimpleName()).build()).run();
    }
}
