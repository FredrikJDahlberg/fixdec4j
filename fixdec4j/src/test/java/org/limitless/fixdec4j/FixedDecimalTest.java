package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.SplittableRandom;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FixedDecimalTest {

    interface Price {
    }

    interface Qty {
    }

    interface Notional {
    }

    static final FixedDecimal<Price> PRICE = FixedDecimal.of(8);
    static final FixedDecimal<Qty> QTY = FixedDecimal.of(2);
    static final FixedDecimal<Notional> NOTIONAL = FixedDecimal.of(2);

    private static final DecimalContext HALF_UP = new DecimalContext(DecimalRounding.HALF_UP);
    private static final BigInteger MAX = BigInteger.valueOf(Long.MAX_VALUE);
    private static final BigInteger MIN = MAX.negate();

    @Test
    public void example() {
        final long price = PRICE.valueOf("101.25", HALF_UP);
        final long quantity = QTY.valueOf("300", HALF_UP);
        assertEquals(10_125_000_000L, price);
        assertEquals(30_000L, quantity);
        assertEquals("30375.00", NOTIONAL.toString(NOTIONAL.multiply(price, PRICE, quantity, QTY, HALF_UP)));
        assertEquals("202.50000000", PRICE.toString(PRICE.add(price, price)));
        assertEquals("101.25", QTY.toString(QTY.convert(price, PRICE, HALF_UP)));
    }

    @Test
    public void typedValues() {
        final MutableFixed64<Price> price = MutableFixed64.valueOf(PRICE, "101.25", HALF_UP);
        final MutableFixed64<Qty> quantity = MutableFixed64.valueOf(QTY, "300", HALF_UP);
        final MutableFixed64<Notional> notional = new MutableFixed64<>(NOTIONAL);
        // price.add(quantity) does not compile
        assertEquals("30375.00", notional.multiply(price, quantity, HALF_UP).toString());
        assertEquals("202.50000000", price.add(MutableFixed64.valueOf(PRICE, "101.25", HALF_UP)).toString());
        assertEquals("0.66666667", new MutableFixed64<>(PRICE)
            .divide(MutableFixed64.valueOf(QTY, "2", HALF_UP), MutableFixed64.valueOf(QTY, "3", HALF_UP), HALF_UP)
            .toString());
        assertEquals(new BigDecimal("30375.00"), notional.toBigDecimal());
        assertTrue(notional.negate().compareTo(new MutableFixed64<>(NOTIONAL)) < 0);
    }

    @Test
    public void rejectsInvalidDecimals() {
        assertThrows(IllegalArgumentException.class, () -> FixedDecimal.of(-1));
        assertThrows(IllegalArgumentException.class, () -> FixedDecimal.of(FixedDecimal.DECIMALS_MAX + 1));
    }

    @Test
    public void addAndSubtractOverflowIsNaN() {
        assertEquals(FixedDecimal.NAN, PRICE.add(Long.MAX_VALUE, 1));
        assertEquals(FixedDecimal.NAN, PRICE.add(-Long.MAX_VALUE, -1));
        assertEquals(FixedDecimal.NAN, PRICE.subtract(-Long.MAX_VALUE, 1));
        assertEquals(FixedDecimal.NAN, PRICE.subtract(Long.MAX_VALUE, -1));
        assertEquals(FixedDecimal.NAN, PRICE.add(FixedDecimal.NAN, 0));
        assertEquals(FixedDecimal.NAN, PRICE.subtract(0, FixedDecimal.NAN));
        assertEquals(Long.MAX_VALUE, PRICE.add(Long.MAX_VALUE - 1, 1));
        assertEquals(FixedDecimal.NAN, PRICE.negate(FixedDecimal.NAN));
        assertEquals(FixedDecimal.NAN, PRICE.abs(FixedDecimal.NAN));
    }

    @Test
    public void addAndSubtractMatchBigInteger() {
        final SplittableRandom random = new SplittableRandom(50);
        for (int i = 0; i < 100_000; i++) {
            final long a = randomValue(random);
            final long b = randomValue(random);
            assertEquals(expected(() -> BigInteger.valueOf(a).add(BigInteger.valueOf(b)), a, b), PRICE.add(a, b));
            assertEquals(expected(() -> BigInteger.valueOf(a).subtract(BigInteger.valueOf(b)), a, b),
                PRICE.subtract(a, b));
        }
    }

    @Test
    public void multiplyMatchesBigDecimal() {
        final SplittableRandom random = new SplittableRandom(51);
        for (final DecimalRounding mode : DecimalRounding.values()) {
            final DecimalContext context = new DecimalContext(mode);
            for (int i = 0; i < 50_000; i++) {
                final FixedDecimal<?> aScale = randomScale(random);
                final FixedDecimal<?> bScale = randomScale(random);
                final FixedDecimal<?> scale = randomScale(random);
                final long a = randomValue(random);
                final long b = randomValue(random);
                final long expected = expected(() -> aScale.toBigDecimal(a).multiply(bScale.toBigDecimal(b))
                    .setScale(scale.decimals(), mode.toRoundingMode()), a, b);
                assertEquals(expected, scale.multiply(a, aScale, b, bScale, context), () -> mode + " " +
                    aScale.toString(a) + " * " + bScale.toString(b) + " with " + scale.decimals() + " decimals");
            }
        }
    }

    @Test
    public void divideMatchesBigDecimal() {
        final SplittableRandom random = new SplittableRandom(52);
        for (final DecimalRounding mode : DecimalRounding.values()) {
            final DecimalContext context = new DecimalContext(mode);
            for (int i = 0; i < 50_000; i++) {
                final FixedDecimal<?> aScale = randomScale(random);
                final FixedDecimal<?> bScale = randomScale(random);
                final FixedDecimal<?> scale = randomScale(random);
                final long a = randomValue(random);
                final long b = randomValue(random);
                final long expected = b == 0 ? FixedDecimal.NAN : expected(() -> aScale.toBigDecimal(a)
                    .divide(bScale.toBigDecimal(b), scale.decimals(), mode.toRoundingMode()), a, b);
                assertEquals(expected, scale.divide(a, aScale, b, bScale, context), () -> mode + " " +
                    aScale.toString(a) + " / " + bScale.toString(b) + " with " + scale.decimals() + " decimals");
            }
        }
    }

    @Test
    public void convertMatchesBigDecimal() {
        final SplittableRandom random = new SplittableRandom(53);
        for (final DecimalRounding mode : DecimalRounding.values()) {
            final DecimalContext context = new DecimalContext(mode);
            for (int i = 0; i < 50_000; i++) {
                final FixedDecimal<?> from = randomScale(random);
                final FixedDecimal<?> scale = randomScale(random);
                final long a = randomValue(random);
                final long expected = expected(() -> from.toBigDecimal(a).setScale(scale.decimals(),
                    mode.toRoundingMode()), a, 0);
                assertEquals(expected, scale.convert(a, from, context), () -> mode + " " + from.toString(a) +
                    " to " + scale.decimals() + " decimals");
                if (a != FixedDecimal.NAN) {
                    assertEquals(expected, scale.valueOf(from.toBigDecimal(a), context));
                }
            }
        }
    }

    @Test
    public void sameScaleOperations() {
        final SplittableRandom random = new SplittableRandom(54);
        for (int i = 0; i < 50_000; i++) {
            final FixedDecimal<?> scale = randomScale(random);
            final long a = randomValue(random);
            final long b = randomValue(random);
            assertEquals(scale.multiply(a, scale, b, scale, HALF_UP), scale.multiply(a, b, HALF_UP));
            assertEquals(scale.divide(a, scale, b, scale, HALF_UP), scale.divide(a, b, HALF_UP));
        }
    }

    @Test
    public void strings() {
        assertEquals("0.00000001", PRICE.toString(1));
        assertEquals("-0.00000001", PRICE.toString(-1));
        assertEquals("NaN", PRICE.toString(FixedDecimal.NAN));
        assertEquals("92233720368547758.07", QTY.toString(Long.MAX_VALUE));
        assertEquals(FixedDecimal.NAN, QTY.valueOf("92233720368547758.08", HALF_UP));
        assertEquals(FixedDecimal.NAN, QTY.valueOf("abc", HALF_UP));
        assertEquals(FixedDecimal.NAN, QTY.valueOf("NaN", HALF_UP));
        assertEquals(12_346L, QTY.valueOf("123.455", HALF_UP));
        assertEquals(12_346L, QTY.valueOf(123_455L, -3, HALF_UP));
        assertEquals(0L, QTY.valueOf("1E-1000000", HALF_UP));
        assertEquals(1L, QTY.valueOf("1E-1000000", new DecimalContext(DecimalRounding.UP)));
        assertEquals(FixedDecimal.NAN, QTY.valueOf("1E+1000000", HALF_UP));
        assertEquals(0L, QTY.valueOf("0E+1000000", HALF_UP));
        assertThrows(ArithmeticException.class, () -> QTY.toBigDecimal(FixedDecimal.NAN));
        assertFalse(FixedDecimal.isNaN(0));
    }

    @Test
    public void narrowsAndWidens() {
        assertEquals(Integer.MAX_VALUE, FixedDecimal.toInt(Integer.MAX_VALUE));
        assertEquals(-Integer.MAX_VALUE, FixedDecimal.toInt(-Integer.MAX_VALUE));
        assertEquals(0, FixedDecimal.toInt(0));
        // Integer.MIN_VALUE is reserved for NaN
        assertEquals(FixedDecimal.INT_NAN, FixedDecimal.toInt(Integer.MIN_VALUE));
        assertEquals(FixedDecimal.INT_NAN, FixedDecimal.toInt(Integer.MAX_VALUE + 1L));
        assertEquals(FixedDecimal.INT_NAN, FixedDecimal.toInt(Long.MAX_VALUE));
        assertEquals(FixedDecimal.INT_NAN, FixedDecimal.toInt(FixedDecimal.NAN));
        assertEquals(FixedDecimal.NAN, FixedDecimal.fromInt(FixedDecimal.INT_NAN));
        assertTrue(FixedDecimal.isNaN(FixedDecimal.INT_NAN));
        assertFalse(FixedDecimal.isNaN(0));

        final SplittableRandom random = new SplittableRandom(55);
        for (int i = 0; i < 100_000; i++) {
            final int stored = random.nextInt(-Integer.MAX_VALUE, Integer.MAX_VALUE) + random.nextInt(2);
            assertEquals(stored, FixedDecimal.toInt(FixedDecimal.fromInt(stored)));
            assertEquals(stored, FixedDecimal.fromInt(stored));
        }

        // calculate in 64 bits, store in 32 bits
        final FixedDecimal<Price> price4 = FixedDecimal.of(4);
        final int[] prices = {FixedDecimal.toInt(price4.valueOf("101.25", HALF_UP)),
            FixedDecimal.toInt(price4.valueOf("99.5", HALF_UP))};
        final long sum = price4.add(FixedDecimal.fromInt(prices[0]), FixedDecimal.fromInt(prices[1]));
        assertEquals("200.7500", price4.toString(sum));
    }

    /**
     * Returns a random value of random magnitude and sign, and sometimes NaN or zero.
     */
    private static long randomValue(final SplittableRandom random) {
        final int kind = random.nextInt(100);
        if (kind == 0) {
            return FixedDecimal.NAN;
        }
        if (kind == 1) {
            return 0;
        }
        final long magnitude = random.nextLong() >>> random.nextInt(1, Long.SIZE);
        return random.nextBoolean() ? magnitude : -magnitude;
    }

    private static FixedDecimal<?> randomScale(final SplittableRandom random) {
        return FixedDecimal.of(random.nextInt(FixedDecimal.DECIMALS_MAX + 1));
    }

    /**
     * Returns the expected raw value from an exact result, or NaN when an operand is NaN, the
     * result is inexact with UNNECESSARY or out of range.
     */
    private static long expected(final Supplier<?> result, final long a, final long b) {
        if (a == FixedDecimal.NAN || b == FixedDecimal.NAN) {
            return FixedDecimal.NAN;
        }
        final Object value;
        try {
            value = result.get();
        } catch (final ArithmeticException e) {
            return FixedDecimal.NAN;
        }
        final BigInteger units = value instanceof BigDecimal decimal ? decimal.unscaledValue() : (BigInteger) value;
        return units.compareTo(MIN) < 0 || units.compareTo(MAX) > 0 ? FixedDecimal.NAN : units.longValueExact();
    }
}
