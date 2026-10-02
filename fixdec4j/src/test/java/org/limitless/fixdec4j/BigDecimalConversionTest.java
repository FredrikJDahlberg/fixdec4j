package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class BigDecimalConversionTest {

    private static final DecimalContext HALF_UP = DecimalContext.HALF_UP;
    private static final DecimalContext UNNECESSARY = DecimalContext.UNNECESSARY;

    @Test
    public void toBigDecimalKeepsDecimals() {
        assertEquals(new BigDecimal("1.50"), Decimal64Flyweight.toBigDecimal(Decimal64Flyweight.valueOf("1.50")));
        assertEquals(new BigDecimal("-0.0000001"), Decimal64Flyweight.toBigDecimal(Decimal64Flyweight.valueOf(-1, -7)));
        assertEquals(new BigDecimal("1000"), Decimal64Flyweight.toBigDecimal(Decimal64Flyweight.valueOf(1, 3)));
        assertEquals(BigDecimal.valueOf(Decimal64Flyweight.MANTISSA_MAX),
            Decimal64Flyweight.toBigDecimal(Decimal64Flyweight.MAX_VALUE));
    }

    @Test
    public void toBigDecimalOfNaNThrows() {
        assertThrows(ArithmeticException.class, () -> Decimal64Flyweight.toBigDecimal(Decimal64Flyweight.NAN));
        assertThrows(ArithmeticException.class, Decimal64.NAN::toBigDecimal);
        assertThrows(ArithmeticException.class, () -> new MutableDecimal64(MutableDecimal64.nan()).toBigDecimal());
    }

    @Test
    public void roundTrip() {
        final SplittableRandom random = new SplittableRandom(45);
        for (int i = 0; i < 100_000; i++) {
            final long mantissa = random.nextLong(Decimal64Flyweight.MANTISSA_MIN, Decimal64Flyweight.MANTISSA_MAX + 1) >>
                random.nextInt(61);
            final long value = Decimal64Flyweight.valueOf(mantissa, -random.nextInt(Decimal64Flyweight.DECIMALS_MAX + 1));
            assertEquals(value, Decimal64Flyweight.valueOf(Decimal64Flyweight.toBigDecimal(value), UNNECESSARY),
                Decimal64Flyweight.toString(value));
        }
    }

    @Test
    public void valueOfMatchesSetScale() {
        final SplittableRandom random = new SplittableRandom(46);
        for (final DecimalRounding mode : DecimalRounding.values()) {
            final DecimalContext context = DecimalContext.of(mode);
            for (int i = 0; i < 20_000; i++) {
                final BigInteger unscaled = new BigInteger(random.nextInt(1, 72), new java.util.Random(random.nextLong()));
                final BigDecimal value = new BigDecimal(random.nextBoolean() ? unscaled : unscaled.negate(),
                    random.nextInt(-5, 21));
                assertEquals(expected(value, mode), Decimal64Flyweight.valueOf(value, context),
                    mode + " " + value.toPlainString());
            }
        }
    }

    @Test
    public void valueOfLimits() {
        assertEquals(Decimal64Flyweight.MAX_VALUE,
            Decimal64Flyweight.valueOf(BigDecimal.valueOf(Decimal64Flyweight.MANTISSA_MAX), HALF_UP));
        assertEquals(Decimal64Flyweight.NAN,
            Decimal64Flyweight.valueOf(BigDecimal.valueOf(Decimal64Flyweight.MANTISSA_MAX + 1), HALF_UP));
        assertEquals(Decimal64Flyweight.valueOf(Decimal64Flyweight.MANTISSA_MIN, 0),
            Decimal64Flyweight.valueOf(BigDecimal.valueOf(Decimal64Flyweight.MANTISSA_MIN), HALF_UP));
        assertEquals(Decimal64Flyweight.NAN,
            Decimal64Flyweight.valueOf(BigDecimal.valueOf(Decimal64Flyweight.MANTISSA_MIN - 1), HALF_UP));
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf(new BigDecimal("1E+19"), HALF_UP));
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf(new BigDecimal("1E+1000000"), HALF_UP));
        assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf(new BigDecimal("1E-1000000"), UNNECESSARY));
        assertEquals(Decimal64Flyweight.valueOf(0, -7), Decimal64Flyweight.valueOf(new BigDecimal("1E-1000000"), HALF_UP));
        assertEquals(Decimal64Flyweight.valueOf(1000, 0), Decimal64Flyweight.valueOf(new BigDecimal("1E+3"), HALF_UP));
        assertEquals(Decimal64Flyweight.valueOf(0, 0), Decimal64Flyweight.valueOf(new BigDecimal("0E+50"), HALF_UP));
        assertEquals(Decimal64Flyweight.valueOf(0, -7), Decimal64Flyweight.valueOf(new BigDecimal("0E-50"), UNNECESSARY));
        // trailing zeros beyond DECIMALS_MAX are exact, the result keeps DECIMALS_MAX decimals
        assertEquals(Decimal64Flyweight.valueOf(15_000_000, -7),
            Decimal64Flyweight.valueOf(new BigDecimal("1.500000000"), UNNECESSARY));
    }

    @Test
    public void objects() {
        final BigDecimal value = new BigDecimal("12345.678");
        assertEquals(value, Decimal64.valueOf(value, HALF_UP).toBigDecimal());
        assertEquals(value, MutableDecimal64.valueOf(value, HALF_UP).toBigDecimal());
        // more than DECIMALS_MAX decimals rounds with the mode of the context
        final BigDecimal tie = new BigDecimal("0.00000025");
        assertEquals(new BigDecimal("0.0000002"),
            Decimal64.valueOf(tie, DecimalContext.HALF_EVEN).toBigDecimal());
        assertEquals(new BigDecimal("0.0000003"), MutableDecimal64.valueOf(tie, HALF_UP).toBigDecimal());
    }

    @Test
    public void tinyValuesRoundLikeBigDecimal() {
        for (final DecimalRounding mode : DecimalRounding.values()) {
            for (final String tiny : new String[] {"1E-8", "-1E-8", "9E-9", "-9E-9", "5E-8", "-5E-8", "0E-20",
                "1E-1000000", "-1E-1000000"}) {
                final BigDecimal value = new BigDecimal(tiny);
                assertEquals(value.scale() > 100 ? expected(new BigDecimal(tiny.replace("1000000", "9")), mode) :
                    expected(value, mode), Decimal64Flyweight.valueOf(value, DecimalContext.of(mode)), mode + " " + tiny);
            }
        }
    }

    /**
     * Returns the expected decimal flyweight using BigDecimal: rounded to at most DECIMALS_MAX
     * decimals, at least zero decimals, and NaN when out of range or inexact with UNNECESSARY.
     */
    private static long expected(final BigDecimal value, final DecimalRounding mode) {
        final BigDecimal scaled;
        try {
            scaled = value.setScale(Math.max(0, Math.min(value.scale(), Decimal64Flyweight.DECIMALS_MAX)),
                mode.toRoundingMode());
        } catch (final ArithmeticException e) {
            return Decimal64Flyweight.NAN;
        }
        final BigInteger unscaled = scaled.unscaledValue();
        if (unscaled.compareTo(BigInteger.valueOf(Decimal64Flyweight.MANTISSA_MIN)) < 0 ||
            unscaled.compareTo(BigInteger.valueOf(Decimal64Flyweight.MANTISSA_MAX)) > 0) {
            return Decimal64Flyweight.NAN;
        }
        return Decimal64Flyweight.valueOf(unscaled.longValueExact(), -scaled.scale());
    }
}
