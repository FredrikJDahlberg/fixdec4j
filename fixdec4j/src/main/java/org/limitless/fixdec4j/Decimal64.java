package org.limitless.fixdec4j;

import java.math.BigDecimal;

/**
 * An immutable decimal of 64 bits, where each value stores its number of decimals (0 - 7), see
 * {@link Decimal64Flyweight} for the representation and limits. Every operation returns a new
 * instance; a result that cannot be represented, an invalid string or a division by zero is NAN,
 * which propagates through further operations:
 * <pre>
 * Decimal64 price = Decimal64.valueOf("101.25");
 * Decimal64 total = price.multiply(Decimal64.valueOf("3"), DecimalContext.HALF_UP); // 303.75
 * Decimal64 third = Decimal64.valueOf("1").divide(Decimal64.valueOf("3"), DecimalContext.HALF_UP);    // 0
 * Decimal64 exact = Decimal64.valueOf("1.00").divide(Decimal64.valueOf("3"), DecimalContext.HALF_UP); // 0.33
 * if (total.isNaN()) { ... }
 * </pre>
 * Multiply and divide round to the largest number of decimals of the operands, so an integer
 * divided by an integer is an integer.
 * @author fredrikdahlberg
 */
public final class Decimal64 implements Comparable<Decimal64> {
    /** Largest mantissa, 2^60 - 1. */
    public static final long MANTISSA_MAX = Decimal64Flyweight.MANTISSA_MAX;
    /** Smallest mantissa, -2^60 + 2. */
    public static final long MANTISSA_MIN = Decimal64Flyweight.MANTISSA_MIN;
    /** Mantissa of NaN, -2^60 + 1. */
    public static final long MANTISSA_ERROR = Decimal64Flyweight.MANTISSA_ERROR;

    /** Largest value, MANTISSA_MAX with no decimals. */
    public static final Decimal64 MAX_VALUE = new Decimal64(Decimal64Flyweight.MAX_VALUE);
    /** Smallest value, MANTISSA_MIN with no decimals. */
    public static final Decimal64 MIN_VALUE = new Decimal64(Decimal64Flyweight.MIN_VALUE);
    /** Zero with no decimals. */
    public static final Decimal64 ZERO = new Decimal64(0);
    /** Not a number, the result of an overflow, an invalid string or a division by zero. */
    public static final Decimal64 NAN = new Decimal64(Decimal64Flyweight.NAN);

    private final long fixedDecimal;

    private Decimal64(final long value) {
        fixedDecimal = value;
    }

    /**
     * Constructs an immutable decimal from another decimal.
     * @param value decimal instance
     */
    public Decimal64(final Decimal64 value) {
        fixedDecimal = value.fixedDecimal;
    }

    /**
     * Constructs an immutable decimal from a scaled mantissa and an exponent.
     * @param scaledMantissa scaled mantissa
     * @param exponent       exponent
     */
    public Decimal64(final long scaledMantissa, final int exponent) {
        fixedDecimal = Decimal64Flyweight.valueOf(scaledMantissa, exponent);
    }

    /**
     * Returns an immutable decimal from a scaled mantissa and an exponent, e.g. 12345 and -2 for
     * 123.45. A positive exponent scales the mantissa, e.g. 123 and 2 is 12300 with no decimals.
     * @param scaledMantissa scaled mantissa
     * @param exponent       exponent, -Decimal64Flyweight.DECIMALS_MAX to Decimal64Flyweight.EXPONENT_MAX
     * @return new decimal, or NAN for an exponent out of range or overflow
     */
    public static Decimal64 valueOf(final long scaledMantissa, final int exponent) {
        return new Decimal64(scaledMantissa, exponent);
    }

    /**
     * Returns an immutable decimal parsed from a plain decimal string, e.g. "-123.45". The number of
     * decimals is kept, e.g. "1.50" has two. A sign other than a leading '-', a leading or trailing
     * point, exponent notation and more than Decimal64Flyweight.DECIMALS_MAX decimals are not
     * supported, see FixedDecimal for a lenient parser that rounds.
     * @param value decimal string
     * @return new decimal, or NAN for null, an invalid string, too many decimals or overflow
     */
    public static Decimal64 valueOf(final String value) {
        return new Decimal64(Decimal64Flyweight.valueOf(value));
    }

    /**
     * Returns an immutable decimal from a double, rounded half away from zero to a number of
     * decimals, e.g. valueOf(-2.5, 0) is -3. The double is rounded as stored, so 1.005, which
     * is slightly below 1.005 as a double, rounds to 1.00 with two decimals.
     * @param value    double
     * @param decimals number of decimals, 0 to Decimal64Flyweight.DECIMALS_MAX
     * @return new decimal, or NAN for a NaN or infinite double, decimals out of range or overflow
     */
    public static Decimal64 valueOf(final double value, final int decimals) {
        return new Decimal64(Decimal64Flyweight.valueOf(value, decimals));
    }

    /**
     * Returns an immutable decimal from a BigDecimal. The number of decimals is kept, but a value with
     * more than Decimal64Flyweight.DECIMALS_MAX decimals is rounded to that many according to the
     * rounding mode.
     * @param value   BigDecimal
     * @param context rounding mode
     * @return new decimal, or NAN for overflow or an inexact result with UNNECESSARY
     */
    public static Decimal64 valueOf(final BigDecimal value, final DecimalContext context) {
        return new Decimal64(Decimal64Flyweight.valueOf(value, context));
    }

    /**
     * Returns a string representation of the object.
     * @return string representation
     */
    @Override
    public String toString() {
        return Decimal64Flyweight.toString(fixedDecimal);
    }

    /**
     * Indicates whether another object is a Decimal64 of the same numeric value, e.g. 1.0 equals
     * 1.00, consistent with compareTo and hashCode. NaN equals NaN.
     * @param object other object
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof Decimal64 other && Decimal64Flyweight.equals(fixedDecimal, other.fixedDecimal);
    }

    /**
     * Returns a hash code value for the object
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Decimal64Flyweight.hashCode(fixedDecimal);
    }

    /**
     * Compares this object with the specified object for order.
     * @param value other object
     * @return less than (-1), equals (0) or greater than (1) the other object
     * @throws NullPointerException if the other object is null
     */
    @Override
    public int compareTo(final Decimal64 value) {
        return Decimal64Flyweight.compareTo(fixedDecimal, value.fixedDecimal);
    }

    /**
     * Returns the unscaled mantissa of this instance
     * @return unscaled mantissa
     */
    public long mantissa() {
        return Decimal64Flyweight.mantissa(fixedDecimal);
    }

    /**
     * Returns the normalized exponent of this instance. Positive exponents are stored as a
     * scaled mantissa with a zero exponent.
     * @return negative exponent or 0
     */
    public int exponent() {
        return Decimal64Flyweight.exponent(fixedDecimal);
    }

    /**
     * Returns true when the instance is equal to NAN.
     * @return true when equal to NAN
     */
    public boolean isNaN() {
        return Decimal64Flyweight.isNaN(fixedDecimal);
    }

    /**
     * Returns true when the mantissa of the value is zero (disregards the exponent)
     * @return true for zero values
     */
    public boolean isZero() {
        return Decimal64Flyweight.isZero(fixedDecimal);
    }

    /**
     * Returns the sum of this instance and its argument.
     * @param value term
     * @return a new instance with the sum or NAN indicating overflow.
     */
    public Decimal64 add(final Decimal64 value) {
        return new Decimal64(Decimal64Flyweight.add(fixedDecimal, value.fixedDecimal));
    }

    /**
     * Returns the difference of this instance and its argument.
     * @param value term
     * @return a new instance with the difference or NAN indicating overflow.
     */
    public Decimal64 subtract(final Decimal64 value) {
        return new Decimal64(Decimal64Flyweight.subtract(fixedDecimal, value.fixedDecimal));
    }

    /**
     * Returns an negated immutable decimal.
     * @return a negated new instance.
     */
    public Decimal64 minus() {
        return new Decimal64(Decimal64Flyweight.minus(fixedDecimal));
    }

    /**
     * Returns the product of this instance and its argument, rounded according to the rounding mode of the context.
     * @param value factor
     * @param context rounding mode
     * @return a new instance with the product or NAN indicating overflow.
     */
    public Decimal64 multiply(final Decimal64 value, final DecimalContext context) {
        return new Decimal64(Decimal64Flyweight.multiply(fixedDecimal, value.fixedDecimal, context));
    }

    /**
     * Returns the quotient of this instance.
     * @param value divisor
     * @param context rounding mode
     * @return a new instance with the quotient or NAN indicating overflow.
     */
    public Decimal64 divide(final Decimal64 value, final DecimalContext context) {
        return new Decimal64(Decimal64Flyweight.divide(fixedDecimal, value.fixedDecimal, context));
    }

    /**
     * Round the value to the specified number of decimals according to the rounding mode.
     * @param decimals number of decimals
     * @param context rounding mode
     * @return rounded value
     */
    public Decimal64 round(int decimals, DecimalContext context) {
        return new Decimal64(Decimal64Flyweight.round(fixedDecimal, decimals, context));
    }

    /**
     * Returns the absolute value of the decimal.
     * @return new absolute value instance
     */
    public Decimal64 abs() {
        return new Decimal64(Decimal64Flyweight.abs(fixedDecimal));
    }

    /**
     * Returns this value times an integer, exact, with the decimals of this value.
     * @param integer integer factor
     * @return product, NaN on overflow
     */
    public Decimal64 multiplyByInteger(final long integer) {
        return new Decimal64(Decimal64Flyweight.multiplyByInteger(fixedDecimal, integer));
    }

    /**
     * Returns this value divided by an integer, rounded to the decimals of this value.
     * @param integer integer divisor
     * @param context rounding mode
     * @return quotient, NaN on division by zero
     */
    public Decimal64 divideByInteger(final long integer, final DecimalContext context) {
        return new Decimal64(Decimal64Flyweight.divideByInteger(fixedDecimal, integer, context));
    }

    /**
     * Returns this value rounded to a multiple of a positive increment, e.g. a tick size, with the
     * largest number of decimals of the two.
     * @param increment positive increment
     * @param context   rounding mode
     * @return rounded value, NaN on overflow or an increment that is not positive
     */
    public Decimal64 roundToIncrement(final Decimal64 increment, final DecimalContext context) {
        return new Decimal64(Decimal64Flyweight.roundToIncrement(fixedDecimal, increment.fixedDecimal, context));
    }

    /**
     * Returns the largest integer not above this value, with no decimals.
     * @return integer value
     */
    public Decimal64 floor() {
        return new Decimal64(Decimal64Flyweight.floor(fixedDecimal));
    }

    /**
     * Returns the smallest integer not below this value, with no decimals.
     * @return integer value
     */
    public Decimal64 ceil() {
        return new Decimal64(Decimal64Flyweight.ceil(fixedDecimal));
    }

    /**
     * Returns the remainder of dividing this value by a divisor, exact, with the sign of this value.
     * @param divisor divisor
     * @return remainder, NaN on division by zero
     */
    public Decimal64 remainder(final Decimal64 divisor) {
        return new Decimal64(Decimal64Flyweight.remainder(fixedDecimal, divisor.fixedDecimal));
    }

    /**
     * Writes this value as ASCII like toString, without allocating.
     * @param bytes  buffer
     * @param offset index of the first byte
     * @return number of bytes written, at most Decimal64Flyweight.STRING_LENGTH_MAX
     * @throws IndexOutOfBoundsException if the string does not fit in the buffer
     */
    public int toBytes(final byte[] bytes, final int offset) {
        return Decimal64Flyweight.toBytes(fixedDecimal, bytes, offset);
    }

    /**
     * Returns the value of the specified number as a byte, which may involve rounding or truncation.
     * @param context rounding mode
     * @return byte value
     * @throws ArithmeticException if this instance is NaN
     */
    public byte byteValue(final DecimalContext context) {
        return Decimal64Flyweight.byteValue(fixedDecimal, context);
    }

    /**
     * Returns the value of the specified number as a short, which may involve rounding or truncation.
     * @param context rounding mode
     * @return short value
     * @throws ArithmeticException if this instance is NaN
     */
    public short shortValue(DecimalContext context) {
        return Decimal64Flyweight.shortValue(fixedDecimal, context);
    }

    /**
     * Returns the value of the specified number as an integer, which may involve rounding or truncation.
     * @param context rounding mode
     * @return integer value
     * @throws ArithmeticException if this instance is NaN
     */
    public int intValue(DecimalContext context) {
        return Decimal64Flyweight.intValue(fixedDecimal, context);
    }

    /**
     * Returns the value of the specified number as a long, which may involve rounding or truncation.
     * @param context rounding mode
     * @return long value
     * @throws ArithmeticException if this instance is NaN
     */
    public long longValue(DecimalContext context) {
        return Decimal64Flyweight.longValue(fixedDecimal, context);
    }

    /**
     * Returns the value of the specified number as a float, which may involve rounding.
     * @return float value
     */
    public float floatValue() {
        return Decimal64Flyweight.floatValue(fixedDecimal);
    }

    /**
     * Returns the value of the specified number as a double, which may involve rounding.
     * @return double value
     */
    public double doubleValue() {
        return Decimal64Flyweight.doubleValue(fixedDecimal);
    }

    /**
     * Returns the exact value as a BigDecimal with the same number of decimals.
     * @return BigDecimal value
     * @throws ArithmeticException if the value is NaN, which BigDecimal cannot represent
     */
    public BigDecimal toBigDecimal() {
        return Decimal64Flyweight.toBigDecimal(fixedDecimal);
    }

    /**
     * Returns the decimal fly-weight representation of this instance.
     * @return decimal fly-weight value
     */
    public long toLongBits() {
        return fixedDecimal;
    }

    /**
     * Returns a decimal instance constructed from a decimal fly-weight representation.
     * @param inFixedValue decimal fly-weight value
     * @return decimal instance
     */
    public static Decimal64 fromLongBits(final long inFixedValue) {
        return new Decimal64(inFixedValue);
    }
}
