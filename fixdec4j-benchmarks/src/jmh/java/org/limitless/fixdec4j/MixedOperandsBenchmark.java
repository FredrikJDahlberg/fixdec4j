package org.limitless.fixdec4j;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.SplittableRandom;
import java.util.concurrent.TimeUnit;

/**
 * Operations on arrays of random operands with random numbers of decimals, so that, unlike the
 * benchmarks of fixed operands, branches on the magnitudes and scales are not always predicted.
 * The time is per operation.
 */
@State(Scope.Thread)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class MixedOperandsBenchmark {
    private static final int COUNT = 1024;

    @Param({"HALF_UP", "HALF_EVEN"})
    DecimalRounding rounding;

    DecimalContext context;
    // results in 4 decimals
    final FixedDecimal<Object> scale = FixedDecimal.of(4);

    // Decimal64 flyweights with 0 to 7 decimals
    final long[] decimals1 = new long[COUNT];
    final long[] decimals2 = new long[COUNT];
    // fixed-scale values with 0 to 9 decimals, the scales in scales1 and scales2
    final long[] values1 = new long[COUNT];
    final long[] values2 = new long[COUNT];
    final FixedDecimal<?>[] scales1 = new FixedDecimal<?>[COUNT];
    final FixedDecimal<?>[] scales2 = new FixedDecimal<?>[COUNT];

    @Setup
    public void setup() {
        context = DecimalContext.of(rounding);
        final SplittableRandom random = new SplittableRandom(100);
        for (int i = 0; i < COUNT; i++) {
            // magnitudes of 1 to 40 bits, so that products need 64 or 128 bits
            decimals1[i] = Decimal64Flyweight.valueOf(randomValue(random, 40), -random.nextInt(8));
            decimals2[i] = Decimal64Flyweight.valueOf(randomValue(random, 40), -random.nextInt(8));
            scales1[i] = FixedDecimal.of(random.nextInt(10));
            scales2[i] = FixedDecimal.of(random.nextInt(10));
            values1[i] = randomValue(random, 48);
            values2[i] = randomValue(random, 48);
        }
    }

    private static long randomValue(final SplittableRandom random, final int bits) {
        final long magnitude = random.nextLong(1, 1L << random.nextInt(1, bits + 1) | 1);
        return random.nextBoolean() ? magnitude : -magnitude;
    }

    @Benchmark
    @OperationsPerInvocation(COUNT)
    public long decimal64Multiply() {
        long sum = 0;
        for (int i = 0; i < COUNT; i++) {
            sum += Decimal64Flyweight.multiply(decimals1[i], decimals2[i], context);
        }
        return sum;
    }

    @Benchmark
    @OperationsPerInvocation(COUNT)
    public long decimal64Divide() {
        long sum = 0;
        for (int i = 0; i < COUNT; i++) {
            sum += Decimal64Flyweight.divide(decimals1[i], decimals2[i], context);
        }
        return sum;
    }

    @Benchmark
    @OperationsPerInvocation(COUNT)
    public long fixedMultiply() {
        long sum = 0;
        for (int i = 0; i < COUNT; i++) {
            sum += scale.multiply(values1[i], scales1[i], values2[i], scales2[i], context);
        }
        return sum;
    }

    @Benchmark
    @OperationsPerInvocation(COUNT)
    public long fixedDivide() {
        long sum = 0;
        for (int i = 0; i < COUNT; i++) {
            sum += scale.divide(values1[i], scales1[i], values2[i], scales2[i], context);
        }
        return sum;
    }

    @Benchmark
    @OperationsPerInvocation(COUNT)
    public long fixedConvert() {
        long sum = 0;
        for (int i = 0; i < COUNT; i++) {
            sum += scale.convert(values1[i], scales1[i], context);
        }
        return sum;
    }

    public static void main(String[] args) throws RunnerException {
        new Runner(new OptionsBuilder().include(MixedOperandsBenchmark.class.getSimpleName()).build()).run();
    }
}
