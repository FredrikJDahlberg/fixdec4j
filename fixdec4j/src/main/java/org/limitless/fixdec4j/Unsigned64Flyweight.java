package org.limitless.fixdec4j;

/**
 * This class implements unsigned 64-bit arithmetic. It is an internal building block for the
 * decimal types and not part of the public API; use Long.divideUnsigned, Long.remainderUnsigned
 * and Long.compareUnsigned for unsigned arithmetic.
 * @author fredrikdahlberg
 */
final class Unsigned64Flyweight {
    /**
     * Returns the number of bits in the number, e.g. ln(value)/ln(2).
     * @param unsignedValue unsigned long
     * @return the number of bits in the number
     */
    public static int numberOfBits(final long unsignedValue) {
        return Long.SIZE - Long.numberOfLeadingZeros(unsignedValue);
    }

    /**
     * Unsigned 64-bit addition
     * @param unsignedValue1 unsigned 64-bit value
     * @param unsignedValue2 unsigned 64-bit value
     * @return the unsigned 64-bit sum
     */
    public static long add(final long unsignedValue1, final long unsignedValue2) {
        return unsignedValue1 + unsignedValue2;
    }

    /**
     * Unsigned 64-bit subtraction
     * @param unsignedValue1 unsigned 64-bit value
     * @param unsignedValue2 unsigned 64-bit value
     * @return 64-bit unsigned difference
     */
    public static long subtract(final long unsignedValue1, final long unsignedValue2) {
        return unsignedValue1 - unsignedValue2;
    }

    /**
     * Unsigned 64-bit multiplication
     * @param unsignedValue1 unsigned 64-bit factor
     * @param unsignedValue2 unsigned 64-bit factor
     * @return 64-bit unsigned product
     */
    public static long multiply(final long unsignedValue1, final long unsignedValue2) {
        return unsignedValue1 * unsignedValue2;
    }

    /**
     * Returns the unsigned quotient of its arguments.
     * @param unsignedDividend 64-bit unsigned long
     * @param unsignedDivisor  64-bit unsigned long
     * @return 64-bit unsigned quotient
     */
    public static long divide(final long unsignedDividend, final long unsignedDivisor) {
        return Long.divideUnsigned(unsignedDividend, unsignedDivisor);
    }

    /**
     * Returns the unsigned remainder of its arguments.
     * @param unsignedDividend 64-bit unsigned long
     * @param unsignedDivisor  64-bit unsigned long
     * @return 64-bit unsigned remainder
     */
    public static long remainder(final long unsignedDividend, final long unsignedDivisor) {
        return Long.remainderUnsigned(unsignedDividend, unsignedDivisor);
    }

    /**
     * Compare two unsigned 64-bit values (interface comparable)
     * @param unsignedValue1 unsigned 64-bit long
     * @param unsignedValue2 unsigned 64-bit long
     * @return 1 = value1 > value2, 0 = value1 == value2, -1 = value1 < value2
     */
    public static int compare(final long unsignedValue1, final long unsignedValue2) {
        return Long.compareUnsigned(unsignedValue1, unsignedValue2);
    }
}
