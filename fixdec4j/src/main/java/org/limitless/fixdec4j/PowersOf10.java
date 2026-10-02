package org.limitless.fixdec4j;

import java.math.BigInteger;

/**
 * Division by powers of ten as a multiplication by a precomputed reciprocal (Granlund and
 * Montgomery, "Division by invariant integers using multiplication"), without a hardware division
 * or a branch on the exponent. It is an internal building block for the decimal types and not part
 * of the public API.
 * @author fredrikdahlberg
 */
final class PowersOf10 {
    static final int EXPONENT_MAX = 18;

    // n / 10^k = unsignedMultiplyHigh(2n, MULTIPLIERS[k]) >>> shift(k) for 0 <= n < 2^63
    private static final long[] MULTIPLIERS = new long[EXPONENT_MAX + 1];

    static {
        // For N-bit n and l = ceil(log2 d), m = floor(2^(N + l) / d) + 1 gives n / d = n * m >>> (N + l)
        // exactly, and m fits in N + 1 bits. With N = 63, m fits in a long (unsigned) and the
        // multiplication of 2n by m is shifted right by 64 + l. Doubling n makes 10^0 the multiplier
        // 2^63 with no shift, so that every exponent takes the same path.
        MULTIPLIERS[0] = Long.MIN_VALUE; // 2^63
        for (int k = 1; k <= EXPONENT_MAX; ++k) {
            final BigInteger divisor = BigInteger.TEN.pow(k);
            final int l = divisor.subtract(BigInteger.ONE).bitLength();
            MULTIPLIERS[k] = BigInteger.ONE.shiftLeft(63 + l).divide(divisor).add(BigInteger.ONE).longValue();
            if (shift(k) != l) {
                throw new AssertionError("shift of 10^" + k);
            }
        }
    }

    private PowersOf10() {
    }

    /**
     * Returns a non-negative value divided by 10^exponent.
     * @param value    value, 0 to Long.MAX_VALUE
     * @param exponent exponent, 0 to EXPONENT_MAX
     * @return truncated quotient
     */
    static long divide(final long value, final int exponent) {
        return Math.unsignedMultiplyHigh(value << 1, MULTIPLIERS[exponent]) >>> shift(exponent);
    }

    /**
     * Returns ceil(log2 10^k) for k up to EXPONENT_MAX, computed rather than loaded from a table
     * (1701 / 512 is log2 10 rounded up), 0 for 10^0.
     */
    private static int shift(final int exponent) {
        return (exponent * 1701 + 511) >>> 9;
    }
}
