package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class Decimal64FlyweightTest {

    Decimal64.Context context = new Decimal64.Context(DecimalRounding.UP);

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
        final Decimal64.Context down = new Decimal64.Context(DecimalRounding.DOWN);
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
}
