package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.SplittableRandom;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
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

    private static final DecimalContext HALF_UP = DecimalContext.HALF_UP;
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
        assertTrue(notional.minus().compareTo(new MutableFixed64<>(NOTIONAL)) < 0);
    }

    @Test
    public void rejectsSameKindOfOtherScale() {
        // same type parameter, different number of decimals: caught at runtime, assertions or not
        final MutableFixed64<Price> price = MutableFixed64.valueOf(PRICE, "1.5", HALF_UP);
        final MutableFixed64<Price> coarse = MutableFixed64.valueOf(FixedDecimal.<Price>of(2), "1.5", HALF_UP);
        assertThrows(IllegalArgumentException.class, () -> price.add(coarse));
        assertThrows(IllegalArgumentException.class, () -> price.subtract(coarse));
        assertThrows(IllegalArgumentException.class, () -> price.set(coarse));
        assertThrows(IllegalArgumentException.class, () -> price.compareTo(coarse));
        assertEquals("1.50000000", price.toString());
    }

    @Test
    public void sharesScales() {
        assertSame(FixedDecimal.of(8), PRICE);
        assertEquals(new FixedDecimal<Price>(8), PRICE);
        final MutableFixed64<Price> price = MutableFixed64.valueOf(PRICE, "1.5", HALF_UP);
        assertSame(PRICE, price.scale());
        assertEquals(MutableFixed64.fromRaw(PRICE, 150_000_000L), price);
        assertNotEquals(MutableFixed64.fromRaw(FixedDecimal.of(7), 150_000_000L), price);
        assertEquals("1.50000000", price.toString());
        assertEquals(new BigDecimal("1.50000000"), price.toBigDecimal());
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
        assertEquals(FixedDecimal.NAN, PRICE.minus(FixedDecimal.NAN));
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
            final DecimalContext context = DecimalContext.of(mode);
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
            final DecimalContext context = DecimalContext.of(mode);
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
            final DecimalContext context = DecimalContext.of(mode);
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
        assertEquals(1L, QTY.valueOf("1E-1000000", DecimalContext.UP));
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
            final DecimalContext context = DecimalContext.of(mode);
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
            assertParse(randomScale(random), builder.toString(), DecimalContext.of(mode));
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
        assertToBytes(scale, value, string);
    }

    /**
     * Asserts that toBytes writes the string at an offset and leaves the bytes around it unchanged.
     */
    private static void assertToBytes(final FixedDecimal<?> scale, final long value, final String string) {
        final int offset = (int) (value & 7);
        final byte[] buffer = new byte[offset + string.length() + 3];
        Arrays.fill(buffer, (byte) '#');
        final int length = scale.toBytes(value, buffer, offset);
        assertEquals(string, new String(buffer, offset, length, StandardCharsets.ISO_8859_1));
        assertTrue(length <= FixedDecimal.STRING_LENGTH_MAX);
        for (int i = 0; i < buffer.length; i++) {
            if (i < offset || i >= offset + length) {
                assertEquals('#', buffer[i], string + " wrote outside its range at " + i);
            }
        }
        // exactly the room needed, and one byte too few
        assertEquals(length, scale.toBytes(value, new byte[length], 0));
        assertThrows(IndexOutOfBoundsException.class, () -> scale.toBytes(value, new byte[length - 1], 0));
    }

    @Test
    public void writesBytes() {
        final byte[] message = new byte[32];
        message[0] = '4';
        message[1] = '4';
        message[2] = '=';
        final int length = PRICE.toBytes(PRICE.valueOf("-101.25", HALF_UP), message, 3);
        message[3 + length] = 1;
        assertEquals("44=-101.25000000\u0001", new String(message, 0, 4 + length, StandardCharsets.ISO_8859_1));
        assertToBytes(QTY, FixedDecimal.NAN, "NaN");
        assertToBytes(FixedDecimal.of(9), -Long.MAX_VALUE, "-9223372036.854775807");
        assertToBytes(FixedDecimal.of(0), -Long.MAX_VALUE, "-9223372036854775807");
        assertThrows(IndexOutOfBoundsException.class, () -> QTY.toBytes(1, message, -1));

        final MutableFixed64<Price> price = MutableFixed64.valueOf(PRICE, "0.5", HALF_UP);
        assertEquals(10, price.toBytes(message, 0));
        assertEquals("0.50000000", new String(message, 0, 10, StandardCharsets.ISO_8859_1));
    }

    @Test
    public void integerArithmetic() {
        final long price = PRICE.valueOf("101.25", HALF_UP);
        assertEquals("1012.50000000", PRICE.toString(PRICE.multiplyByInteger(price, 10)));
        assertEquals("33.75000000", PRICE.toString(PRICE.divideByInteger(price, 3, HALF_UP)));
        assertEquals("0.67", QTY.toString(QTY.divideByInteger(QTY.valueOf("2", HALF_UP), 3, HALF_UP)));
        assertEquals(FixedDecimal.NAN, PRICE.divideByInteger(price, 0, HALF_UP));
        assertEquals(FixedDecimal.NAN, PRICE.multiplyByInteger(Long.MAX_VALUE, 2));
        assertEquals(FixedDecimal.NAN, PRICE.multiplyByInteger(FixedDecimal.NAN, 0));
        assertEquals(FixedDecimal.NAN, PRICE.multiplyByInteger(FixedDecimal.NAN, 1));
        assertEquals(-Long.MAX_VALUE, PRICE.multiplyByInteger(Long.MAX_VALUE, -1));

        final MutableFixed64<Notional> notional = MutableFixed64.valueOf(NOTIONAL, "100", HALF_UP);
        assertEquals("300.00", notional.multiplyByInteger(3).toString());
        assertEquals("42.86", notional.divideByInteger(7, HALF_UP).toString());

        final SplittableRandom random = new SplittableRandom(58);
        for (final DecimalRounding mode : DecimalRounding.values()) {
            final DecimalContext context = DecimalContext.of(mode);
            for (int i = 0; i < 50_000; i++) {
                final FixedDecimal<?> scale = randomScale(random);
                final long a = randomValue(random);
                final long integer = random.nextInt(20) == 0 ? random.nextInt(-1, 2) :
                    random.nextLong() >> random.nextInt(1, Long.SIZE);
                final long product = expected(() -> BigInteger.valueOf(a).multiply(BigInteger.valueOf(integer)), a, 0);
                assertEquals(product, scale.multiplyByInteger(a, integer), () -> a + " * " + integer);
                final long quotient = integer == 0 ? FixedDecimal.NAN : expected(() -> scale.toBigDecimal(a)
                    .divide(BigDecimal.valueOf(integer), scale.decimals(), mode.toRoundingMode()), a, 0);
                assertEquals(quotient, scale.divideByInteger(a, integer, context), () -> mode + " " +
                    scale.toString(a) + " / " + integer);
            }
        }
    }

    @Test
    public void floorCeilAndRemainder() {
        final long price = PRICE.valueOf("101.25", HALF_UP);
        assertEquals("101.00000000", PRICE.toString(PRICE.floor(price)));
        assertEquals("102.00000000", PRICE.toString(PRICE.ceil(price)));
        assertEquals("-102.00000000", PRICE.toString(PRICE.floor(-price)));
        assertEquals("-101.00000000", PRICE.toString(PRICE.ceil(-price)));
        assertEquals(PRICE.valueOf("101", HALF_UP), PRICE.floor(PRICE.valueOf("101", HALF_UP)));
        assertEquals(FixedDecimal.NAN, PRICE.floor(FixedDecimal.NAN));
        assertEquals(FixedDecimal.NAN, QTY.ceil(Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, FixedDecimal.of(0).ceil(Long.MAX_VALUE));

        assertEquals("1.25", QTY.toString(QTY.remainder(QTY.valueOf("10.25", HALF_UP), QTY.valueOf("3", HALF_UP))));
        assertEquals("-1.25", QTY.toString(QTY.remainder(QTY.valueOf("-10.25", HALF_UP), QTY.valueOf("3", HALF_UP))));
        assertEquals("1.25", QTY.toString(QTY.remainder(QTY.valueOf("10.25", HALF_UP), QTY.valueOf("-3", HALF_UP))));
        assertEquals(FixedDecimal.NAN, QTY.remainder(1, 0));
        assertEquals(FixedDecimal.NAN, QTY.remainder(FixedDecimal.NAN, 1));
        assertEquals(FixedDecimal.NAN, QTY.remainder(1, FixedDecimal.NAN));

        final MutableFixed64<Price> typed = MutableFixed64.fromRaw(PRICE, price);
        assertEquals("101.00000000", typed.floor().toString());
        assertEquals("101.00000000", typed.ceil().toString());
        assertEquals("1.00000000", typed.remainder(MutableFixed64.valueOf(PRICE, "2", HALF_UP)).toString());
        assertThrows(IllegalArgumentException.class, () -> typed.remainder(
            MutableFixed64.valueOf(FixedDecimal.<Price>of(2), "2", HALF_UP)));

        final SplittableRandom random = new SplittableRandom(60);
        for (int i = 0; i < 200_000; i++) {
            final FixedDecimal<?> scale = randomScale(random);
            final long a = randomValue(random);
            final long b = random.nextBoolean() ? randomValue(random) : random.nextLong(-1000, 1000);
            final int decimals = scale.decimals();
            assertEquals(expected(() -> scale.toBigDecimal(a).setScale(0, RoundingMode.FLOOR).setScale(decimals), a, 0),
                scale.floor(a), () -> "floor " + scale.toString(a));
            assertEquals(expected(() -> scale.toBigDecimal(a).setScale(0, RoundingMode.CEILING).setScale(decimals), a, 0),
                scale.ceil(a), () -> "ceil " + scale.toString(a));
            final long remainder = b == 0 ? FixedDecimal.NAN :
                expected(() -> scale.toBigDecimal(a).remainder(scale.toBigDecimal(b)), a, b);
            assertEquals(remainder, scale.remainder(a, b), () -> scale.toString(a) + " % " + scale.toString(b));
        }
    }

    @Test
    public void rounds() {
        final long price = PRICE.valueOf("101.256", HALF_UP);
        assertEquals("101.26000000", PRICE.toString(PRICE.round(price, 2, HALF_UP)));
        assertEquals("100.00000000", PRICE.toString(PRICE.round(price, -1, HALF_UP)));
        assertEquals(price, PRICE.round(price, 8, HALF_UP));
        assertEquals(price, PRICE.round(price, 12, HALF_UP));
        assertEquals(FixedDecimal.NAN, PRICE.round(price, 8 - 19, HALF_UP));
        assertEquals(9_223_372_036_854_775_800L, QTY.round(Long.MAX_VALUE, 0, HALF_UP));
        assertEquals(FixedDecimal.NAN, QTY.round(Long.MAX_VALUE, 1, HALF_UP)); // ...758.10 overflows
        assertEquals(FixedDecimal.NAN, PRICE.round(price, 2, DecimalContext.UNNECESSARY));

        final long tick = PRICE.valueOf("0.05", HALF_UP);
        assertEquals("101.25000000", PRICE.toString(PRICE.roundToIncrement(price, tick, HALF_UP)));
        assertEquals("101.30000000", PRICE.toString(PRICE.roundToIncrement(price, tick,
            DecimalContext.CEILING)));
        assertEquals("-101.25000000", PRICE.toString(PRICE.roundToIncrement(-price, tick, HALF_UP)));
        assertEquals(FixedDecimal.NAN, PRICE.roundToIncrement(price, 0, HALF_UP));
        assertEquals(FixedDecimal.NAN, PRICE.roundToIncrement(price, -tick, HALF_UP));
        assertEquals(FixedDecimal.NAN, PRICE.roundToIncrement(Long.MAX_VALUE, 1L << 62, HALF_UP)); // 2^63

        final MutableFixed64<Price> typed = MutableFixed64.fromRaw(PRICE, price);
        assertEquals("101.26000000", typed.round(2, HALF_UP).toString());
        assertEquals("101.25000000", typed.roundToIncrement(MutableFixed64.fromRaw(PRICE, tick),
            DecimalContext.FLOOR).toString());
        assertThrows(IllegalArgumentException.class, () -> typed.roundToIncrement(
            MutableFixed64.valueOf(FixedDecimal.<Price>of(2), "0.05", HALF_UP), HALF_UP));

        final SplittableRandom random = new SplittableRandom(59);
        for (final DecimalRounding mode : DecimalRounding.values()) {
            final DecimalContext context = DecimalContext.of(mode);
            for (int i = 0; i < 50_000; i++) {
                final FixedDecimal<?> scale = randomScale(random);
                final long a = randomValue(random);
                final int places = random.nextInt(scale.decimals() - 20, scale.decimals() + 2);
                final long rounded = places >= scale.decimals() ? a : places < scale.decimals() - 18 ?
                    FixedDecimal.NAN : expected(() -> scale.toBigDecimal(a).setScale(places, mode.toRoundingMode())
                    .setScale(scale.decimals()), a, 0);
                assertEquals(rounded, scale.round(a, places, context), () -> mode + " " + scale.toString(a) +
                    " to " + places + " places");

                final long increment = random.nextInt(20) == 0 ? random.nextLong(-1, 1) :
                    random.nextLong(1, 1L << random.nextInt(1, Long.SIZE - 1));
                final long multiple = increment <= 0 ? FixedDecimal.NAN : expected(() -> new BigDecimal(a)
                    .divide(BigDecimal.valueOf(increment), 0, mode.toRoundingMode()).toBigIntegerExact()
                    .multiply(BigInteger.valueOf(increment)), a, 0);
                assertEquals(multiple, scale.roundToIncrement(a, increment, context), () -> mode + " " + a +
                    " to a multiple of " + increment);
            }
        }
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
