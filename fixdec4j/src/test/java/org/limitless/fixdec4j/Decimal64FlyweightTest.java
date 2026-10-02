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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class Decimal64FlyweightTest {

    DecimalContext context = DecimalContext.HALF_UP;

    @Test
    public void nanParts() {
        assertEquals(Decimal64Flyweight.MANTISSA_ERROR, Decimal64Flyweight.mantissa(Decimal64Flyweight.NAN));
        assertEquals(0, Decimal64Flyweight.exponent(Decimal64Flyweight.NAN));
        assertEquals(false, Decimal64Flyweight.isZero(Decimal64Flyweight.NAN));
    }

    @Test
    public void toStrings() {
        long value = Decimal64Flyweight.valueOf(1_500_000, -3);
        String string = Decimal64Flyweight.toString(value);
        assertEquals("1500.000", string);
    }

    @Test
    public void toStringDigits() {
        assertEquals("0", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(0, 0)));
        assertEquals("8", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(8, 0)));
        assertEquals("-8", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(-8, 0)));
        assertEquals("0.00998", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(998, -5)));
        assertEquals("99.82416", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(9982416, -5)));
        assertEquals("77.7340000", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(777340000L, -7)));
        assertEquals("12193263112.448712",
            Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(12193263112448712L, -6)));
        assertEquals("123123123100000000",
            Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(12312312310000L, 4)));
        assertEquals("29655948507.61", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(2965594850761L, -2)));
        assertEquals("9999999999", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(9999999999L, 0)));
        assertEquals("-1152921504606846974",
            Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(Decimal64Flyweight.MANTISSA_MIN, 0)));
    }

    @Test
    public void digitCounts() {
        assertEquals(1, Decimal64Flyweight.digitsBase10(0));
        assertEquals(1, Decimal64Flyweight.digitsBase10(1));
        assertEquals(4, Decimal64Flyweight.digitsBase10(4711));
        assertEquals(10, Decimal64Flyweight.digitsBase10(Integer.MAX_VALUE));
        assertEquals(19, Decimal64Flyweight.digitsBase10(Long.MAX_VALUE));
        assertEquals(1, Decimal64Flyweight.digitsBase10(0L));
        long power = 1;
        for (int digits = 1; digits <= 18; digits++) {
            assertEquals(digits, Decimal64Flyweight.digitsBase10(power), "10^" + (digits - 1));
            assertEquals(digits, Decimal64Flyweight.digitsBase10(power * 10 - 1), "10^" + digits + "-1");
            if (power <= Integer.MAX_VALUE / 10) {
                assertEquals(digits, Decimal64Flyweight.digitsBase10((int) power));
                assertEquals(digits, Decimal64Flyweight.digitsBase10((int) (power * 10 - 1)));
            }
            power *= 10;
        }
    }

    @Test
    public void multiply64() {
        long value = Decimal64Flyweight.valueOf(1000_00000L, -5);
        long factor = Decimal64Flyweight.valueOf(123_50, -2);
        long result = Decimal64Flyweight.multiply(value, factor, context);
        assertEquals(12_350_000_000L, Decimal64Flyweight.mantissa(result));
        assertEquals(-5, Decimal64Flyweight.exponent(result));
    }

    @Test
    public void multiply128() {
        long value = Decimal64Flyweight.valueOf(1231231231, -5);
        long factor = Decimal64Flyweight.valueOf(12334, 2);
        assertEquals(12148804802523205L, Decimal64Flyweight.multiply(value, factor, context));
    }

    @Test
    public void divide64() {
        long value = Decimal64Flyweight.valueOf(1125_00000L, -5);
        long factor = Decimal64Flyweight.valueOf(112_50, -2);
        long result = Decimal64Flyweight.divide(value, factor, context);
        assertEquals(1_000_000L, Decimal64Flyweight.mantissa(result));
        assertEquals(-5, Decimal64Flyweight.exponent(result));
    }

    @Test
    public void divideNegative() {
        long value = Decimal64Flyweight.valueOf(-1125_00000L, -5);
        long factor = Decimal64Flyweight.valueOf(112_50, -2);
        long result = Decimal64Flyweight.divide(value, factor, context);
        assertEquals(-1_000_000L, Decimal64Flyweight.mantissa(result));
        assertEquals(-5, Decimal64Flyweight.exponent(result));
    }

    @Test
    public void compareValues() {
        // equal bit lengths
        assertEquals(-1, Decimal64Flyweight.compareTo(Decimal64Flyweight.valueOf(5, 0), Decimal64Flyweight.valueOf(6, 0)));
        assertEquals(1, Decimal64Flyweight.compareTo(Decimal64Flyweight.valueOf(6, 0), Decimal64Flyweight.valueOf(5, 0)));
        assertEquals(-1, Decimal64Flyweight.compareTo(Decimal64Flyweight.valueOf(5, -1), Decimal64Flyweight.valueOf(6, -1)));
        // different decimals
        assertEquals(1, Decimal64Flyweight.compareTo(Decimal64Flyweight.valueOf(15, -1), Decimal64Flyweight.valueOf(120, -2)));
        assertEquals(-1, Decimal64Flyweight.compareTo(Decimal64Flyweight.valueOf(-15, -1), Decimal64Flyweight.valueOf(-120, -2)));
        assertEquals(0, Decimal64Flyweight.compareTo(Decimal64Flyweight.valueOf(12, -1), Decimal64Flyweight.valueOf(120, -2)));
        // scaling overflows, the magnitude decides
        assertEquals(1, Decimal64Flyweight.compareTo(
            Decimal64Flyweight.valueOf(Decimal64Flyweight.MANTISSA_MAX, 0), Decimal64Flyweight.valueOf(Decimal64Flyweight.MANTISSA_MAX, -7)));
        assertEquals(-1, Decimal64Flyweight.compareTo(
            Decimal64Flyweight.valueOf(-Decimal64Flyweight.MANTISSA_MAX + 1, 0), Decimal64Flyweight.valueOf(-1, -7)));
    }

    @Test
    public void addOverflow() {
        // 2^59 + 0.0000001 needs a mantissa of about 5.8e24
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.add(
            Decimal64Flyweight.valueOf(1L << 59, 0), Decimal64Flyweight.valueOf(1, -7)));
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.subtract(
            Decimal64Flyweight.valueOf(1, -7), Decimal64Flyweight.valueOf(1L << 59, 0)));
        final long sum = Decimal64Flyweight.add(Decimal64Flyweight.valueOf(115292150460L, 0), Decimal64Flyweight.valueOf(1, -7));
        assertEquals(1152921504600000001L, Decimal64Flyweight.mantissa(sum));
        assertEquals(-7, Decimal64Flyweight.exponent(sum));
    }

    @Test
    public void divideRoundsHalfUp() {
        // odd divisors, the remainder decides the rounding
        assertDivide(18180000, -4, 7, -4, 25971428571L, -4);   // 1818 / 0.0007 = 2597142.857142...
        assertDivide(39406, -4, 3, -5, 13135333333L, -5);      // 3.9406 / 0.00003 = 131353.333333...
        assertDivide(8, -6, -11, -6, -727273, -6);             // 0.000008 / -0.000011 = -0.727272727...
        assertDivide(2, 0, 3, 0, 1, 0);
        // ties round away from zero
        assertDivide(1, -2, 2, 0, 1, -2);                      // 0.01 / 2 = 0.005
        assertDivide(-3, -2, 2, 0, -2, -2);                    // -0.03 / 2 = -0.015
        assertDivide(1, 0, 8, 0, 0, 0);                        // 1 / 8 = 0.125
    }

    @Test
    public void divideRoundsDown() {
        final DecimalContext down = DecimalContext.DOWN;
        final long result = Decimal64Flyweight.divide(Decimal64Flyweight.valueOf(2, 0), Decimal64Flyweight.valueOf(3, 0), down);
        assertEquals(0, Decimal64Flyweight.mantissa(result));
        final long tie = Decimal64Flyweight.divide(Decimal64Flyweight.valueOf(-3, -2), Decimal64Flyweight.valueOf(2, 0), down);
        assertEquals(-1, Decimal64Flyweight.mantissa(tie));
    }

    @Test
    public void overflowIsNaN() {
        // quotients and products of 2^63 to 2^64 must not wrap to negative mantissas
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.divide(
            Decimal64Flyweight.valueOf(1041119044703L, -2), Decimal64Flyweight.valueOf(6, -5), context));
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.divide(
            Decimal64Flyweight.valueOf(236600569113721057L, -2), Decimal64Flyweight.valueOf(133233076, -6), context));
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.multiply(
            Decimal64Flyweight.valueOf(5052, 0), Decimal64Flyweight.valueOf(3580537735801959L, -1), context));
        // 922337203685478 * 92233720368.5477578, the scaled product exceeds 128 bits
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.multiply(
            Decimal64Flyweight.valueOf(922337203685478L, 0), Decimal64Flyweight.valueOf(922337203685477578L, -7), context));
    }

    private void assertDivide(long dividend, int dividendExponent, long divisor, int divisorExponent,
                              long mantissa, int exponent) {
        final long result = Decimal64Flyweight.divide(Decimal64Flyweight.valueOf(dividend, dividendExponent),
            Decimal64Flyweight.valueOf(divisor, divisorExponent), context);
        assertEquals(mantissa, Decimal64Flyweight.mantissa(result));
        assertEquals(exponent, Decimal64Flyweight.exponent(result));
    }

    @Test
    public void divideByZero() {
        long value = Decimal64Flyweight.valueOf(5, 0);
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.divide(value, Decimal64Flyweight.valueOf(0, 0), context));
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.divide(value, Decimal64Flyweight.valueOf(0, -2), context));
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.divide(value, Decimal64Flyweight.valueOf(0, -7), context));
    }

    @Test
    public void divideSignSymmetry() {
        final long[][] operands = {
            {123456789L, -4, 25L, -1},
            {123456789012L, -4, 987654321L, -6},
            {1231231231L, -5, 12334L, -2},
            {1231231231L, -5, 12334L, 2},
            {7L, -1, 3L, -7},
            {1L, 0, 3L, 0},
        };
        for (long[] operand : operands) {
            long value = Decimal64Flyweight.valueOf(operand[0], (int) operand[1]);
            long factor = Decimal64Flyweight.valueOf(operand[2], (int) operand[3]);
            long expected = Decimal64Flyweight.divide(value, factor, context);
            assertNotEquals(Decimal64Flyweight.NAN, expected);
            long negated = Decimal64Flyweight.minus(expected);
            assertEquals(negated, Decimal64Flyweight.divide(Decimal64Flyweight.minus(value), factor, context));
            assertEquals(negated, Decimal64Flyweight.divide(value, Decimal64Flyweight.minus(factor), context));
            assertEquals(expected, Decimal64Flyweight.divide(
                Decimal64Flyweight.minus(value), Decimal64Flyweight.minus(factor), context));
        }
    }

    @Test
    public void divide128() {
        long value = Decimal64Flyweight.valueOf(1231231231, -5);
        long factor = Decimal64Flyweight.valueOf(12334, 2);
        long result = Decimal64Flyweight.divide(value, factor, context);
        assertEquals(998, Decimal64Flyweight.mantissa(result));
        assertEquals(-5, Decimal64Flyweight.exponent(result));
    }

    @Test
    public void divideOverflow() {
        // 123123123100000000 / 77.7340000 = 1583903093884272.0045283, needs a 74-bit mantissa
        long value = Decimal64Flyweight.valueOf(12312312310000L, 4);
        long factor = Decimal64Flyweight.valueOf(777340000L, -7);
        assertNotEquals(Decimal64Flyweight.NAN, value);
        assertNotEquals(Decimal64Flyweight.NAN, factor);
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.divide(value, factor, context));
    }

    private static final long NAN = Decimal64Flyweight.NAN;

    private static long decimal(final String value) {
        return Decimal64Flyweight.valueOf(value);
    }

    private static String string(final long value) {
        return Decimal64Flyweight.toString(value);
    }

    @Test
    public void integerArithmetic() {
        assertEquals("1012.50", string(Decimal64Flyweight.multiplyByInteger(decimal("101.25"), 10)));
        assertEquals("33.75", string(Decimal64Flyweight.divideByInteger(decimal("101.25"), 3, context)));
        assertEquals("0.6667", string(Decimal64Flyweight.divideByInteger(decimal("2.0000"), 3, context)));
        assertEquals("1", string(Decimal64Flyweight.divideByInteger(decimal("2"), 3, context)));
        assertEquals(NAN, Decimal64Flyweight.divideByInteger(decimal("1"), 0, context));
        assertEquals(NAN, Decimal64Flyweight.divideByInteger(decimal("1"), Long.MIN_VALUE, context));
        assertEquals(NAN, Decimal64Flyweight.multiplyByInteger(Decimal64Flyweight.MAX_VALUE, 2));
        assertEquals(NAN, Decimal64Flyweight.multiplyByInteger(NAN, 0));
    }

    @Test
    public void roundsToIncrementFloorAndCeil() {
        final long price = decimal("101.256");
        assertEquals("101.250", string(Decimal64Flyweight.roundToIncrement(price, decimal("0.05"), context)));
        assertEquals("101.300", string(Decimal64Flyweight.roundToIncrement(price, decimal("0.1"), context)));
        assertEquals("-101.250", string(Decimal64Flyweight.roundToIncrement(Decimal64Flyweight.minus(price),
            decimal("0.05"), context)));
        assertEquals(NAN, Decimal64Flyweight.roundToIncrement(price, decimal("0"), context));
        assertEquals(NAN, Decimal64Flyweight.roundToIncrement(price, decimal("-0.05"), context));

        assertEquals("101", string(Decimal64Flyweight.floor(price)));
        assertEquals("102", string(Decimal64Flyweight.ceil(price)));
        assertEquals("-102", string(Decimal64Flyweight.floor(Decimal64Flyweight.minus(price))));
        assertEquals("-101", string(Decimal64Flyweight.ceil(Decimal64Flyweight.minus(price))));
        assertEquals(NAN, Decimal64Flyweight.floor(NAN));

        assertEquals("1.25", string(Decimal64Flyweight.remainder(decimal("10.25"), decimal("3"))));
        assertEquals("-1.25", string(Decimal64Flyweight.remainder(decimal("-10.25"), decimal("3"))));
        assertEquals("0.100", string(Decimal64Flyweight.remainder(decimal("10.1"), decimal("0.125"))));
        assertEquals(NAN, Decimal64Flyweight.remainder(decimal("1"), decimal("0.00")));
    }

    @Test
    public void writesBytes() {
        final byte[] message = "44=".getBytes(StandardCharsets.ISO_8859_1);
        final byte[] buffer = Arrays.copyOf(message, 32);
        final int length = Decimal64Flyweight.toBytes(decimal("-101.25"), buffer, 3);
        assertEquals("44=-101.25", new String(buffer, 0, 3 + length, StandardCharsets.ISO_8859_1));
        assertToBytes(NAN);
        assertToBytes(Decimal64Flyweight.MAX_VALUE);
        assertToBytes(Decimal64Flyweight.MIN_VALUE);
        assertThrows(IndexOutOfBoundsException.class, () -> Decimal64Flyweight.toBytes(decimal("1"), buffer, 32));
    }

    private static void assertToBytes(final long value) {
        final String string = string(value);
        final int offset = (int) (value & 7);
        final byte[] buffer = new byte[offset + string.length() + 3];
        Arrays.fill(buffer, (byte) '#');
        final int length = Decimal64Flyweight.toBytes(value, buffer, offset);
        assertEquals(string, new String(buffer, offset, length, StandardCharsets.ISO_8859_1));
        assertTrue(length <= Decimal64Flyweight.STRING_LENGTH_MAX);
        for (int i = 0; i < buffer.length; i++) {
            if (i < offset || i >= offset + length) {
                assertEquals('#', buffer[i], string + " wrote outside its range at " + i);
            }
        }
        assertThrows(IndexOutOfBoundsException.class, () -> Decimal64Flyweight.toBytes(value, new byte[length - 1], 0));
    }

    @Test
    public void newOperationsMatchBigDecimal() {
        final SplittableRandom random = new SplittableRandom(61);
        for (int i = 0; i < 100_000; i++) {
            assertToBytes(randomDecimal(random));
        }
        for (final DecimalRounding mode : DecimalRounding.values()) {
            final DecimalContext context = DecimalContext.of(mode);
            final RoundingMode roundingMode = mode.toRoundingMode();
            for (int i = 0; i < 50_000; i++) {
                final long a = randomDecimal(random);
                final long b = randomDecimal(random);
                final long integer = random.nextInt(20) == 0 ? random.nextInt(-1, 2) :
                    random.nextLong() >> random.nextInt(1, Long.SIZE);
                final String operands = mode + " " + string(a) + ", " + string(b) + ", " + integer;

                assertEquals(expected(() -> big(a).multiply(BigDecimal.valueOf(integer)), a),
                    Decimal64Flyweight.multiplyByInteger(a, integer), operands);
                assertEquals(integer == 0 || integer == Long.MIN_VALUE ? NAN : expected(() ->
                    big(a).divide(BigDecimal.valueOf(integer), big(a).scale(), roundingMode), a),
                    Decimal64Flyweight.divideByInteger(a, integer, context), operands);
                assertEquals(b != NAN && Decimal64Flyweight.mantissa(b) <= 0 ? NAN : expected(() ->
                    big(a).divide(big(b), 0, roundingMode).multiply(big(b))
                        .setScale(Math.max(big(a).scale(), big(b).scale())), a, b),
                    Decimal64Flyweight.roundToIncrement(a, b, context), operands);
                assertEquals(b != NAN && Decimal64Flyweight.isZero(b) ? NAN : expected(() ->
                    big(a).remainder(big(b)).setScale(Math.max(big(a).scale(), big(b).scale())), a, b),
                    Decimal64Flyweight.remainder(a, b), operands);
            }
        }
        for (int i = 0; i < 100_000; i++) {
            final long a = randomDecimal(random);
            assertEquals(expected(() -> big(a).setScale(0, RoundingMode.FLOOR), a), Decimal64Flyweight.floor(a),
                () -> "floor " + string(a));
            assertEquals(expected(() -> big(a).setScale(0, RoundingMode.CEILING), a), Decimal64Flyweight.ceil(a),
                () -> "ceil " + string(a));
        }
    }

    /**
     * Returns a random value of 0 to 7 decimals and random magnitude, sometimes NaN or zero.
     */
    private static long randomDecimal(final SplittableRandom random) {
        final int kind = random.nextInt(100);
        if (kind == 0) {
            return NAN;
        }
        final long mantissa = kind == 1 ? 0 : random.nextLong() >> random.nextInt(3, Long.SIZE);
        final long value = Decimal64Flyweight.valueOf(mantissa, -random.nextInt(Decimal64Flyweight.DECIMALS_MAX + 1));
        return value == NAN ? 0 : value; // the two mantissas out of range
    }

    private static BigDecimal big(final long value) {
        return Decimal64Flyweight.toBigDecimal(value);
    }

    /**
     * Returns the decimal flyweight of an exact result, or NaN when an operand is NaN, the result is
     * inexact with UNNECESSARY or out of range.
     */
    private static long expected(final Supplier<BigDecimal> result, final long... operands) {
        for (final long operand : operands) {
            if (operand == NAN) {
                return NAN;
            }
        }
        final BigDecimal value;
        try {
            value = result.get();
        } catch (final ArithmeticException e) {
            return NAN;
        }
        assertTrue(value.scale() >= 0 && value.scale() <= Decimal64Flyweight.DECIMALS_MAX, value::toString);
        final BigInteger mantissa = value.unscaledValue();
        if (mantissa.compareTo(BigInteger.valueOf(Decimal64Flyweight.MANTISSA_MIN)) < 0 ||
            mantissa.compareTo(BigInteger.valueOf(Decimal64Flyweight.MANTISSA_MAX)) > 0) {
            return NAN;
        }
        return Decimal64Flyweight.valueOf(mantissa.longValue(), -value.scale());
    }

    @Test
    public void numericEquality() {
        assertEquals(decimal("1.5"), Decimal64Flyweight.stripTrailingZeros(decimal("1.500")));
        assertEquals(decimal("0"), Decimal64Flyweight.stripTrailingZeros(decimal("0.00")));
        assertEquals(decimal("-120"), Decimal64Flyweight.stripTrailingZeros(decimal("-120.0000000")));
        assertEquals(NAN, Decimal64Flyweight.stripTrailingZeros(NAN));
        assertTrue(Decimal64Flyweight.equals(decimal("1.0"), decimal("1.00")));
        assertTrue(Decimal64Flyweight.equals(NAN, NAN));
        assertEquals(false, Decimal64Flyweight.equals(NAN, decimal("0")));
        assertEquals(false, Decimal64Flyweight.equals(decimal("1.0"), decimal("1.01")));

        // equals agrees with compareTo, and equal values have equal hash codes
        final SplittableRandom random = new SplittableRandom(62);
        for (int i = 0; i < 500_000; i++) {
            final long a = randomDecimal(random);
            // b is often a with another number of decimals
            final long b = random.nextBoolean() ? randomDecimal(random) :
                Decimal64Flyweight.round(a, random.nextInt(Decimal64Flyweight.DECIMALS_MAX + 1), context);
            final boolean equal = Decimal64Flyweight.compareTo(a, b) == 0;
            assertEquals(equal, Decimal64Flyweight.equals(a, b), () -> string(a) + " and " + string(b));
            if (equal) {
                assertEquals(Decimal64Flyweight.hashCode(a), Decimal64Flyweight.hashCode(b));
            }
            if (a != NAN && b != NAN) {
                assertEquals(big(a).compareTo(big(b)) == 0, equal, () -> string(a) + " and " + string(b));
            }
        }
    }
}
