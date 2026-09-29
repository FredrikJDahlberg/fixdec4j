package org.limitless.fixdec4j;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Decimal operands shared by all decimal benchmarks so that results are comparable across
 * implementations. Each value is a scaled mantissa and a number of decimals, e.g. 12345.6789 is
 * 123456789 with 4 decimals.
 */
public enum Operands {
    /** Multiply and divide fit the 64-bit fast path: 12345.6789 and 2.5 */
    SMALL(123456789L, 4, 25L, 1),
    /** Multiply and divide need 128-bit intermediates: 12345678.9012 and 987.654321 */
    LARGE(123456789012L, 4, 987654321L, 6);

    final long mantissa1;
    final int decimals1;
    final long mantissa2;
    final int decimals2;

    Operands(final long mantissa1, final int decimals1, final long mantissa2, final int decimals2) {
        this.mantissa1 = mantissa1;
        this.decimals1 = decimals1;
        this.mantissa2 = mantissa2;
        this.decimals2 = decimals2;
    }

    /**
     * Returns the number of decimals of a fixed decimal result, i.e. the largest of the operands.
     * @return number of decimals
     */
    int decimals() {
        return Math.max(decimals1, decimals2);
    }

    /**
     * Returns the first operand as a decimal flyweight.
     * @return decimal flyweight value
     */
    long value1() {
        return Decimal64Flyweight.valueOf(mantissa1, -decimals1);
    }

    /**
     * Returns the second operand as a decimal flyweight.
     * @return decimal flyweight value
     */
    long value2() {
        return Decimal64Flyweight.valueOf(mantissa2, -decimals2);
    }

    BigDecimal bigValue1() {
        return BigDecimal.valueOf(mantissa1, decimals1);
    }

    BigDecimal bigValue2() {
        return BigDecimal.valueOf(mantissa2, decimals2);
    }

    /**
     * Fails fast unless the fixed decimal results equal BigDecimal rounded half up to the
     * largest number of decimals of the operands. A wrong result (e.g. NaN) would otherwise be
     * benchmarked as if it were correct.
     * @param add      decimal flyweight sum
     * @param subtract decimal flyweight difference
     * @param multiply decimal flyweight product
     * @param divide   decimal flyweight quotient
     */
    void verify(final long add, final long subtract, final long multiply, final long divide) {
        final BigDecimal value1 = bigValue1();
        final BigDecimal value2 = bigValue2();
        verify("add", value1.add(value2), add);
        verify("subtract", value1.subtract(value2), subtract);
        verify("multiply", value1.multiply(value2).setScale(decimals(), RoundingMode.HALF_UP), multiply);
        verify("divide", value1.divide(value2, decimals(), RoundingMode.HALF_UP), divide);
    }

    private void verify(final String operation, final BigDecimal expected, final long result) {
        final BigDecimal actual = Decimal64Flyweight.isNaN(result) ? null :
            BigDecimal.valueOf(Decimal64Flyweight.mantissa(result), -Decimal64Flyweight.exponent(result));
        if (!expected.equals(actual)) {
            throw new IllegalStateException(this + " " + operation + ": expected " + expected.toPlainString() +
                " but was " + (actual == null ? "NaN" : actual.toPlainString()));
        }
    }
}
