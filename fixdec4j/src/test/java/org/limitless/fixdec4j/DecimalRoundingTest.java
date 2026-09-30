package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.SplittableRandom;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Verifies every rounding mode against BigDecimal with the corresponding java.math.RoundingMode.
 */
public class DecimalRoundingTest {

    private static final BigInteger MANTISSA_MIN = BigInteger.valueOf(Decimal64Flyweight.MANTISSA_MIN);
    private static final BigInteger MANTISSA_MAX = BigInteger.valueOf(Decimal64Flyweight.MANTISSA_MAX);

    @Test
    public void namesMatchRoundingMode() {
        for (final DecimalRounding mode : DecimalRounding.values()) {
            assertEquals(mode.name(), RoundingMode.valueOf(mode.name()).name());
        }
        assertEquals(RoundingMode.values().length, DecimalRounding.values().length);
    }

    @Test
    public void convertsToAndFromRoundingMode() {
        for (final DecimalRounding mode : DecimalRounding.values()) {
            assertEquals(mode.name(), mode.toRoundingMode().name());
            assertEquals(mode, DecimalRounding.valueOf(mode.toRoundingMode()));
        }
    }

    /**
     * The table from the java.math.RoundingMode documentation, rounding to zero decimals.
     */
    @Test
    public void roundingModeTable() {
        final String[] inputs = {"5.5", "2.5", "1.6", "1.1", "1.0", "-1.0", "-1.1", "-1.6", "-2.5", "-5.5"};
        final String[][] expected = {
            {"6", "3", "2", "2", "1", "-1", "-2", "-2", "-3", "-6"},   // UP
            {"5", "2", "1", "1", "1", "-1", "-1", "-1", "-2", "-5"},   // DOWN
            {"6", "3", "2", "2", "1", "-1", "-1", "-1", "-2", "-5"},   // CEILING
            {"5", "2", "1", "1", "1", "-1", "-2", "-2", "-3", "-6"},   // FLOOR
            {"6", "3", "2", "1", "1", "-1", "-1", "-2", "-3", "-6"},   // HALF_UP
            {"5", "2", "2", "1", "1", "-1", "-1", "-2", "-2", "-5"},   // HALF_DOWN
            {"6", "2", "2", "1", "1", "-1", "-1", "-2", "-2", "-6"},   // HALF_EVEN
            {"NaN", "NaN", "NaN", "NaN", "1", "-1", "NaN", "NaN", "NaN", "NaN"} // UNNECESSARY
        };
        final DecimalRounding[] modes = DecimalRounding.values();
        for (int m = 0; m < modes.length; m++) {
            final DecimalContext context = new DecimalContext(modes[m]);
            for (int i = 0; i < inputs.length; i++) {
                final long rounded = Decimal64Flyweight.round(Decimal64Flyweight.valueOf(inputs[i]), 0, context);
                final String actual = Decimal64Flyweight.isNaN(rounded) ? "NaN" : Decimal64Flyweight.toString(rounded);
                assertEquals(expected[m][i], actual, modes[m] + " round(" + inputs[i] + ")");
            }
        }
    }

    @Test
    public void roundMatchesBigDecimal() {
        final SplittableRandom random = new SplittableRandom(42);
        for (final DecimalRounding mode : DecimalRounding.values()) {
            final DecimalContext context = new DecimalContext(mode);
            for (int i = 0; i < 20_000; i++) {
                final long value = randomValue(random);
                final int decimals = random.nextInt(Decimal64Flyweight.DECIMALS_MAX + 1);
                final BigDecimal expected = expected(() -> toBigDecimal(value).setScale(decimals, roundingMode(mode)));
                assertResult(expected, Decimal64Flyweight.round(value, decimals, context),
                    mode + " round(" + Decimal64Flyweight.toString(value) + ", " + decimals + ")");
            }
        }
    }

    @Test
    public void multiplyMatchesBigDecimal() {
        final SplittableRandom random = new SplittableRandom(43);
        for (final DecimalRounding mode : DecimalRounding.values()) {
            final DecimalContext context = new DecimalContext(mode);
            for (int i = 0; i < 20_000; i++) {
                final long value = randomValue(random);
                final long factor = randomValue(random);
                final BigDecimal expected = expected(() -> toBigDecimal(value).multiply(toBigDecimal(factor))
                    .setScale(decimals(value, factor), roundingMode(mode)));
                assertResult(expected, Decimal64Flyweight.multiply(value, factor, context), mode + " " +
                    Decimal64Flyweight.toString(value) + " * " + Decimal64Flyweight.toString(factor));
            }
        }
    }

    @Test
    public void divideMatchesBigDecimal() {
        final SplittableRandom random = new SplittableRandom(44);
        for (final DecimalRounding mode : DecimalRounding.values()) {
            final DecimalContext context = new DecimalContext(mode);
            for (int i = 0; i < 20_000; i++) {
                final long dividend = randomValue(random);
                final long divisor = randomValue(random);
                if (Decimal64Flyweight.isZero(divisor)) {
                    continue;
                }
                final BigDecimal expected = expected(() -> toBigDecimal(dividend)
                    .divide(toBigDecimal(divisor), decimals(dividend, divisor), roundingMode(mode)));
                assertResult(expected, Decimal64Flyweight.divide(dividend, divisor, context), mode + " " +
                    Decimal64Flyweight.toString(dividend) + " / " + Decimal64Flyweight.toString(divisor));
            }
        }
    }

    @Test
    public void halfEvenTies() {
        final DecimalContext context = new DecimalContext(DecimalRounding.HALF_EVEN);
        // multiply: 0.5 * 0.5 = 0.25 -> 0.2, 0.5 * 0.7 = 0.35 -> 0.4
        assertEquals(Decimal64Flyweight.valueOf(2, -1), Decimal64Flyweight.multiply(
            Decimal64Flyweight.valueOf(5, -1), Decimal64Flyweight.valueOf(5, -1), context));
        assertEquals(Decimal64Flyweight.valueOf(4, -1), Decimal64Flyweight.multiply(
            Decimal64Flyweight.valueOf(5, -1), Decimal64Flyweight.valueOf(7, -1), context));
        // divide: 0.01 / 2 = 0.005 -> 0.00, -0.03 / 2 = -0.015 -> -0.02
        assertEquals(Decimal64Flyweight.valueOf(0, -2), Decimal64Flyweight.divide(
            Decimal64Flyweight.valueOf(1, -2), Decimal64Flyweight.valueOf(2, 0), context));
        assertEquals(Decimal64Flyweight.valueOf(-2, -2), Decimal64Flyweight.divide(
            Decimal64Flyweight.valueOf(-3, -2), Decimal64Flyweight.valueOf(2, 0), context));
    }

    @Test
    public void unnecessaryIsNaNWhenInexact() {
        final DecimalContext context = new DecimalContext(DecimalRounding.UNNECESSARY);
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.divide(
            Decimal64Flyweight.valueOf(1, 0), Decimal64Flyweight.valueOf(3, 0), context));
        assertEquals(Decimal64Flyweight.valueOf(5, -1), Decimal64Flyweight.divide(
            Decimal64Flyweight.valueOf(1, 0), Decimal64Flyweight.valueOf(20, -1), context));
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.round(Decimal64Flyweight.valueOf(125, -2), 1, context));
        assertEquals(Decimal64Flyweight.valueOf(12, -1),
            Decimal64Flyweight.round(Decimal64Flyweight.valueOf(120, -2), 1, context));
    }

    @Test
    public void objectsUseContextMode() {
        final DecimalContext context = new DecimalContext(DecimalRounding.FLOOR);
        final Decimal64 value = Decimal64.valueOf(-11, -1);
        assertEquals(Decimal64.valueOf(-2, 0).toLongBits(), value.round(0, context).toLongBits());
        final MutableDecimal64 mutable = MutableDecimal64.valueOf(-11, -1);
        assertEquals(Decimal64.valueOf(-2, 0).toLongBits(),
            mutable.round(0, new DecimalContext(DecimalRounding.FLOOR)).toLongBits());
    }

    /**
     * Returns a random value with a random number of decimals, sign and magnitude, so that both
     * the 64-bit and 128-bit intermediate paths are covered, as well as overflow.
     */
    private static long randomValue(final SplittableRandom random) {
        final int bits = random.nextInt(1, 61);
        long mantissa = random.nextLong() >>> (Long.SIZE - bits);
        if (mantissa > Decimal64Flyweight.MANTISSA_MAX) {
            mantissa = Decimal64Flyweight.MANTISSA_MAX;
        }
        if (random.nextBoolean()) {
            mantissa = -mantissa;
        }
        return Decimal64Flyweight.valueOf(mantissa, -random.nextInt(Decimal64Flyweight.DECIMALS_MAX + 1));
    }

    private static int decimals(final long value1, final long value2) {
        return Math.max(-Decimal64Flyweight.exponent(value1), -Decimal64Flyweight.exponent(value2));
    }

    private static BigDecimal toBigDecimal(final long value) {
        return BigDecimal.valueOf(Decimal64Flyweight.mantissa(value), -Decimal64Flyweight.exponent(value));
    }

    private static RoundingMode roundingMode(final DecimalRounding mode) {
        return RoundingMode.valueOf(mode.name());
    }

    /**
     * Returns the BigDecimal result, or null when rounding is necessary for UNNECESSARY.
     */
    private static BigDecimal expected(final Supplier<BigDecimal> result) {
        try {
            return result.get();
        } catch (final ArithmeticException e) {
            return null;
        }
    }

    /**
     * Asserts the result equals the BigDecimal result, or NaN when the BigDecimal result is
     * inexact (null) or out of range.
     */
    private static void assertResult(final BigDecimal expected, final long actual, final String message) {
        final long expectedBits;
        if (expected == null || expected.unscaledValue().compareTo(MANTISSA_MIN) < 0 ||
            expected.unscaledValue().compareTo(MANTISSA_MAX) > 0) {
            expectedBits = Decimal64Flyweight.NAN;
        } else {
            expectedBits = Decimal64Flyweight.valueOf(expected.unscaledValue().longValueExact(), -expected.scale());
        }
        assertEquals(expectedBits, actual, message + ": expected " + Decimal64Flyweight.toString(expectedBits) +
            " but was " + Decimal64Flyweight.toString(actual));
    }
}
