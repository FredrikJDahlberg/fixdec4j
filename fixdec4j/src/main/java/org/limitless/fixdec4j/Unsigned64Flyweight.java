package org.limitless.fixdec4j;

/**
 * This class implements unsigned 64-bit arithmetic.
 * @author Fredah
 */
public final class Unsigned64Flyweight {
    /**
     * Returns the number of bits in the number, e.g. ln(value)/ln(2).
     * @param inUnsignedValue unsigned long
     * @return the number of bits in the number
     */
    public static int numberOfBits(final long inUnsignedValue) {
        return Long.SIZE - Long.numberOfLeadingZeros(inUnsignedValue);
    }

    /**
     * Unsigned 64-bit addition
     * @param inUnsignedValue1 unsigned 64-bit value
     * @param inUnsignedValue2 unsigned 64-bit value
     * @return the unsigned 64-bit sum
     */
    public static long add(final long inUnsignedValue1, final long inUnsignedValue2) {
        return inUnsignedValue1 + inUnsignedValue2;
    }

    /**
     * Unsigned 64-bit subtraction
     * @param inUnsignedValue1 unsigned 64-bit value
     * @param inUnsignedValue2 unsigned 64-bit value
     * @return 64-bit unsigned difference
     */
    public static long subtract(final long inUnsignedValue1, final long inUnsignedValue2) {
        return inUnsignedValue1 - inUnsignedValue2;
    }

    /**
     * Unsigned 64-bit multiplication
     * @param inUnsignedValue1 unsigned 64-bit factor
     * @param inUnsignedValue2 unsigned 64-bit factor
     * @return 64-bit unsigned product
     */
    public static long multiply(final long inUnsignedValue1, final long inUnsignedValue2) {
        return inUnsignedValue1 * inUnsignedValue2;
    }

    /**
     * Returns the unigned quotient of its arguments.
     * (The Long.divideUnsigned is too slow, beacuase it uses BigInteger)
     * @param inUnsignedDividend 64-bit unsigned long
     * @param inUnsignedDivisor  64-bit unsigned long
     * @return 64-bit unsigned quotient
     */
    public static long divide(final long inUnsignedDividend, final long inUnsignedDivisor) {
        if (inUnsignedDivisor < 0) {
            if (compare(inUnsignedDividend, inUnsignedDivisor) < 0) {
                return 0;
            } else {
                return 1;
            }
        }
        if (inUnsignedDividend >= 0) {
            return inUnsignedDividend / inUnsignedDivisor;
        }

        final long quotient = ((inUnsignedDividend >>> 1) / inUnsignedDivisor) << 1;
        final long remainder = inUnsignedDividend - quotient * inUnsignedDivisor;

        return quotient + (compare(remainder, inUnsignedDivisor) >= 0 ? 1 : 0);
    }

    /**
     * Returns the unsigned remainder of its arguments.
     * (The Long.remainderUnsigned is too slow, becuase it uses BigInteger)
     * @param inUnsignedDividend 64-bit unsigned long
     * @param inUnsignedDivisor  64-bit unsigned long
     * @return 64-bit unsigned remainder
     */
    public static long remainder(long inUnsignedDividend, long inUnsignedDivisor) {
        if (inUnsignedDivisor < 0) {
            if (compare(inUnsignedDividend, inUnsignedDivisor) < 0) {
                return inUnsignedDividend;
            } else {
                return inUnsignedDividend - inUnsignedDivisor;
            }
        }
        if (inUnsignedDividend >= 0) {
            return inUnsignedDividend % inUnsignedDivisor;
        }

        final long quotient = ((inUnsignedDividend >>> 1) / inUnsignedDivisor) << 1;
        final long remainder = inUnsignedDividend - quotient * inUnsignedDivisor;

        return remainder - (compare(remainder, inUnsignedDivisor) >= 0 ? inUnsignedDivisor : 0);
    }

    /**
     * Compare two unsigned 64-bit values (interface comparable)
     * @param inUnsignedValue1 unsigned 64-bit long
     * @param inUnsignedValue2 unsigned 64-bit long
     * @return 1 = value1 > value2, 0 = value1 == value2, -1 = value1 < value2
     */
    public static int compare(final long inUnsignedValue1, final long inUnsignedValue2) {
        final long flipped1 = inUnsignedValue1 ^ Long.MIN_VALUE;
        final long flipped2 = inUnsignedValue2 ^ Long.MIN_VALUE;

        return Long.compare(flipped1, flipped2);
    }
}
