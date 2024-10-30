package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Decimal64FlyweightTest {

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
}

