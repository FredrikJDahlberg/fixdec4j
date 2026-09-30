package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

@State(Scope.Thread)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class FixedDecimalBenchmark {

    @Param
    Operands operands;

    // The scales are fields, not static final constants, so the JIT cannot fold the number of
    // decimals: the conservative case. The results have the largest number of decimals of the
    // operands, matching the Decimal64 benchmarks.
    FixedDecimal<Object> scale;
    FixedDecimal<Object> scale1;
    FixedDecimal<Object> scale2;
    DecimalContext context = new DecimalContext(DecimalRounding.HALF_UP);

    // operands in the result scale
    long value;
    long operand;
    // operands in their own scales
    long value1;
    long value2;

    MutableFixed64<Object> typedValue;
    MutableFixed64<Object> typedOperand;
    MutableFixed64<Object> result;

    @Setup
    public void setup() {
        scale = FixedDecimal.of(operands.decimals());
        scale1 = FixedDecimal.of(operands.decimals1);
        scale2 = FixedDecimal.of(operands.decimals2);
        value1 = operands.mantissa1;
        value2 = operands.mantissa2;
        value = scale.convert(value1, scale1, context);
        operand = scale.convert(value2, scale2, context);
        typedValue = MutableFixed64.fromRaw(scale, value);
        typedOperand = MutableFixed64.fromRaw(scale, operand);
        result = new MutableFixed64<>(scale);

        final BigDecimal big1 = operands.bigValue1();
        final BigDecimal big2 = operands.bigValue2();
        final int decimals = operands.decimals();
        verify("add", big1.add(big2), add());
        verify("subtract", big1.subtract(big2), subtract());
        verify("multiply", big1.multiply(big2).setScale(decimals, RoundingMode.HALF_UP), multiply());
        verify("divide", big1.divide(big2, decimals, RoundingMode.HALF_UP), divide());
        verify("multiplyMixed", big1.multiply(big2).setScale(decimals, RoundingMode.HALF_UP), multiplyMixed());
        verify("divideMixed", big1.divide(big2, decimals, RoundingMode.HALF_UP), divideMixed());
        verify("typedMultiply", big1.multiply(big2).setScale(decimals, RoundingMode.HALF_UP), typedMultiply().raw());
    }

    private void verify(final String operation, final BigDecimal expected, final long actual) {
        if (FixedDecimal.isNaN(actual) || !expected.setScale(operands.decimals()).equals(scale.toBigDecimal(actual))) {
            throw new IllegalStateException(operands + " " + operation + ": expected " + expected.toPlainString() +
                " but was " + scale.toString(actual));
        }
    }

    @Benchmark
    public long baseline() {
        return value;
    }

    @Benchmark
    public long add() {
        return scale.add(value, operand);
    }

    @Benchmark
    public long subtract() {
        return scale.subtract(value, operand);
    }

    /** Both operands and the result in the same scale. */
    @Benchmark
    public long multiply() {
        return scale.multiply(value, operand, context);
    }

    @Benchmark
    public long divide() {
        return scale.divide(value, operand, context);
    }

    /** Operands in their own scales, the result in the largest, like Decimal64. */
    @Benchmark
    public long multiplyMixed() {
        return scale.multiply(value1, scale1, value2, scale2, context);
    }

    @Benchmark
    public long divideMixed() {
        return scale.divide(value1, scale1, value2, scale2, context);
    }

    @Benchmark
    public MutableFixed64<Object> typedAdd() {
        return result.setRaw(value).add(typedOperand);
    }

    @Benchmark
    public MutableFixed64<Object> typedMultiply() {
        return result.multiply(typedValue, typedOperand, context);
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(FixedDecimalBenchmark.class.getSimpleName()).build()).run();
    }
}
