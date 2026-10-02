package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PowersOf10Test {

    @Test
    public void dividesLikeDivision() {
        final SplittableRandom random = new SplittableRandom(80);
        long divisor = 1;
        for (int k = 0; k <= PowersOf10.EXPONENT_MAX; k++, divisor *= 10) {
            final long[] edges = {0, 1, 9, 10, 11, divisor - 1, divisor, divisor + 1, Long.MAX_VALUE,
                Long.MAX_VALUE - 1, Long.MAX_VALUE / divisor * divisor, Long.MAX_VALUE / divisor * divisor - 1};
            for (final long value : edges) {
                if (value >= 0) {
                    assertDivides(value, k, divisor);
                }
            }
            for (int i = 0; i < 2_000_000; i++) {
                // every magnitude, and multiples of the divisor and their neighbours
                final long value = random.nextLong() >>> random.nextInt(1, Long.SIZE);
                assertDivides(value, k, divisor);
                final long multiple = value / divisor * divisor;
                assertDivides(multiple, k, divisor);
                if (multiple > 0) {
                    assertDivides(multiple - 1, k, divisor);
                }
            }
        }
    }

    private static void assertDivides(final long value, final int exponent, final long divisor) {
        if (PowersOf10.divide(value, exponent) != value / divisor) {
            assertEquals(value / divisor, PowersOf10.divide(value, exponent), value + " / 10^" + exponent);
        }
    }
}
