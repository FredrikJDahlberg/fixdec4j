package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
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
    public void parseMatchesBigDecimal() {
        final String[] strings = {"", "-", "+", ".", "-.", "+.", "..", "1..", "1.2.3", " 1", "1 ", "--1", "+-1", "1-",
            "NaN", "nan", "Infinity", "0x10", "1,5", "0", "-0", "+0", "0.", ".0", "-.5", "+.5", "5.", "007.50",
            "00000000000000000000000000001", "9223372036854775807", "9223372036854775808", "-9223372036854775807",
            "-9223372036854775808", "92233720368547758.07", "92233720368547758.075", "-92233720368547758.075",
            "0.005", "0.015", "0.025", "-0.005", "0.0050000000000000000001", "0.0049999999999999999999",
            "1e2", "1E-2", "1.5e+3", "-1.25E1", "1e", "e1", "1e2.5", "١٢", "1٢"};
        for (final DecimalRounding mode : DecimalRounding.values()) {
            final DecimalContext context = new DecimalContext(mode);
            for (int decimals = 0; decimals <= FixedDecimal.DECIMALS_MAX; decimals++) {
                final FixedDecimal<?> scale = FixedDecimal.of(decimals);
                for (final String string : strings) {
                    assertParse(scale, string, context);
                }
            }
        }

        final SplittableRandom random = new SplittableRandom(56);
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 500_000; i++) {
            builder.setLength(0);
            final int sign = random.nextInt(4);
            if (sign == 0) {
                builder.append('-');
            } else if (sign == 1) {
                builder.append('+');
            }
            final int integerDigits = random.nextInt(22);
            final int fractionDigits = random.nextInt(-1, 14); // -1 for no point
            for (int d = 0; d < integerDigits; d++) {
                builder.append(randomDigit(random));
            }
            if (fractionDigits >= 0) {
                builder.append('.');
                for (int d = 0; d < fractionDigits; d++) {
                    builder.append(randomDigit(random));
                }
            }
            final DecimalRounding mode = DecimalRounding.values()[random.nextInt(DecimalRounding.values().length)];
            assertParse(randomScale(random), builder.toString(), new DecimalContext(mode));
        }
    }

    @Test
    public void formatMatchesBigDecimal() {
        final SplittableRandom random = new SplittableRandom(57);
        final long[] edges = {0, 1, -1, 9, 10, 99, 100, Long.MAX_VALUE, -Long.MAX_VALUE, 999_999_999, 1_000_000_000,
            9_999_999_999L, 10_000_000_000L, -10_000_000_000L};
        for (int decimals = 0; decimals <= FixedDecimal.DECIMALS_MAX; decimals++) {
            final FixedDecimal<?> scale = FixedDecimal.of(decimals);
            for (final long value : edges) {
                assertFormat(scale, value);
            }
        }
        for (int i = 0; i < 500_000; i++) {
            final long value = randomValue(random);
            if (value != FixedDecimal.NAN) {
                assertFormat(randomScale(random), value);
            }
        }
    }

    private static void assertFormat(final FixedDecimal<?> scale, final long value) {
        final String string = scale.toString(value);
        assertEquals(BigDecimal.valueOf(value, scale.decimals()).toPlainString(), string);
        assertEquals(value, scale.valueOf(string, HALF_UP));
    }

    /**
     * Asserts that a string parses like BigDecimal rounded to the scale, and invalid strings as NaN.
     */
    private static void assertParse(final FixedDecimal<?> scale, final String string, final DecimalContext context) {
        long expected;
        try {
            expected = expected(() -> new BigDecimal(string).setScale(scale.decimals(),
                context.roundingMode().toRoundingMode()), 0, 0);
        } catch (final NumberFormatException e) {
            expected = FixedDecimal.NAN;
        }
        assertEquals(expected, scale.valueOf(string, context), () -> context.roundingMode() + " \"" + string +
            "\" with " + scale.decimals() + " decimals");

        // the same string as bytes in a buffer, between digits that are not part of it
        final byte[] field = string.getBytes(StandardCharsets.ISO_8859_1);
        final int offset = string.length() % 5;
        final byte[] buffer = new byte[offset + field.length + string.length() % 11];
        Arrays.fill(buffer, (byte) '7');
        System.arraycopy(field, 0, buffer, offset, field.length);
        final boolean ascii = string.chars().allMatch(c -> c < 0x80);
        assertEquals(ascii ? expected : FixedDecimal.NAN, scale.valueOf(buffer, offset, field.length, context),
            () -> context.roundingMode() + " bytes \"" + string + "\" with " + scale.decimals() + " decimals");
    }

    @Test
    public void parsesBytes() {
        final byte[] message = "44=101.25\u000138=300\u0001".getBytes(StandardCharsets.ISO_8859_1);
        assertEquals(10_125_000_000L, PRICE.valueOf(message, 3, 6, HALF_UP));
        assertEquals(30_000L, QTY.valueOf(message, 13, 3, HALF_UP));
        assertEquals(10_100L, QTY.valueOf(message, 3, 4, HALF_UP)); // "101." of "101.25"
        assertEquals(FixedDecimal.NAN, QTY.valueOf(message, 0, 0, HALF_UP));
        assertThrows(IndexOutOfBoundsException.class, () -> QTY.valueOf(message, 16, 3, HALF_UP));
        final String longString = "0".repeat(100) + "1.5";
        assertEquals(150L, QTY.valueOf(longString, HALF_UP));
        assertEquals(150L, QTY.valueOf(new StringBuilder(longString).substring(90), HALF_UP));
    }

    private static char randomDigit(final SplittableRandom random) {
        // bias towards 0, 5 and 9 to hit ties, trailing zeros and carries
        final int kind = random.nextInt(4);
        return kind == 0 ? '0' : kind == 1 ? '5' : kind == 2 ? '9' : (char) ('0' + random.nextInt(10));
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
