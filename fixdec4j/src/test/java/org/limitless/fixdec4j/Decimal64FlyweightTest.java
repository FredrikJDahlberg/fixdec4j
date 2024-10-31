package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Decimal64FlyweightTest {

    Decimal64.Context context = new Decimal64.Context(DecimalRounding.UP);

    @Test
    public void toStrings() {
        long value = Decimal64Flyweight.valueOf(1_500_000, -3);
        String string = Decimal64Flyweight.toString(value);
        assertEquals("1500.000", string);
    }

    @Test
    public void digitCounts() {
        assertEquals(1, Decimal64Flyweight.digitsBase10(0));
        assertEquals(1, Decimal64Flyweight.digitsBase10(1));
        assertEquals(4, Decimal64Flyweight.digitsBase10(4711));
        assertEquals(10, Decimal64Flyweight.digitsBase10(Integer.MAX_VALUE));
        assertEquals(19, Decimal64Flyweight.digitsBase10(Long.MAX_VALUE));
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
    public void divide128() {
        long value = Decimal64Flyweight.valueOf(1231231231, -5);
        long factor = Decimal64Flyweight.valueOf(12334, 2);
        assertEquals(7989, Decimal64Flyweight.divide(value, factor, context));
    }
}
