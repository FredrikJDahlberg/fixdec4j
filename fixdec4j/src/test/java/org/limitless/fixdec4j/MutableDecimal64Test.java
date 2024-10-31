package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MutableDecimal64Test {

    @Test
    public void basics() {
        MutableDecimal64 value = MutableDecimal64.valueOf(40_000, -3);
        MutableDecimal64 term = MutableDecimal64.valueOf(50_000, -3);
        value.add(term);
        assertEquals(720003, value.toLongBits());

        MutableDecimal64 decimal =  MutableDecimal64.valueOf(16, -1);
        decimal.subtract(MutableDecimal64.valueOf(1, -1));
        assertEquals(15, decimal.mantissa());
        assertEquals(-1, decimal.exponent());
    }

    @Test
    public void toStrings() {
        MutableDecimal64 value = MutableDecimal64.valueOf(1_500_000, -3);
        assertEquals("1500.000", value.toString());
    }

    @Test
    public void multiply64() {
        MutableDecimal64 value = MutableDecimal64.valueOf(1000_00000L, -5);
        MutableDecimal64 factor = MutableDecimal64.valueOf(123_50, -2);
        value.multiply(factor, new MutableDecimal64.Context(DecimalRounding.UP));
        assertEquals(12_350_000_000L, value.mantissa());
        assertEquals(-5, value.exponent());
    }

    @Test
    public void multiply128() {
        MutableDecimal64 value = MutableDecimal64.valueOf(12_310_00000, -5);
        MutableDecimal64 factor = MutableDecimal64.valueOf(15, 7);
        value.multiply(factor, new MutableDecimal64.Context(DecimalRounding.UP));
        assertEquals(184650000000000000L, value.mantissa());
        assertEquals(-5, value.exponent());
    }

    @Test
    public void divide64() {
        MutableDecimal64 value = MutableDecimal64.valueOf(1125_00000L, -5);
        MutableDecimal64 factor = MutableDecimal64.valueOf(112_50, -2);
        value.divide(factor, new MutableDecimal64.Context(DecimalRounding.UP));
        assertEquals(1_000_000L, value.mantissa());
        assertEquals(-5, value.exponent());
    }

    @Test
    public void divide128() {
        MutableDecimal64 value = MutableDecimal64.valueOf(1231231231, -5);
        MutableDecimal64 factor = MutableDecimal64.valueOf(12334, 2);
        value.divide(factor, new MutableDecimal64.Context(DecimalRounding.UP));
        assertEquals(998, value.mantissa());
        assertEquals(-5, value.exponent());
    }
}
