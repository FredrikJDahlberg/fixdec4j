package org.limitless.fixdec4j;

import java.math.BigDecimal;

/**
 * A mutable decimal of 64 bits, where each value stores its number of decimals (0 - 7), see
 * {@link Decimal64Flyweight} for the representation and limits. The operations update this
 * instance and return it, so they can be chained without allocating:
 * <pre>
 * MutableDecimal64 value = MutableDecimal64.valueOf("101.25");
 * value.multiply(MutableDecimal64.valueOf("3"), DecimalContext.HALF_UP).round(1, DecimalContext.HALF_UP); // 303.8
 * </pre>
 * A result that cannot be represented, an invalid string or a division by zero is NAN, which
 * propagates through further operations. An instance is not thread-safe.
 * @author fredrikdahlberg
 */
public final class MutableDecimal64 implements Comparable<MutableDecimal64> {
    // mantissa limits
    /** Largest mantissa, 2^60 - 1. */
    public static final long MANTISSA_MAX = Decimal64Flyweight.MANTISSA_MAX;
    /** Smallest mantissa, -2^60 + 2. */
    public static final long MANTISSA_MIN = Decimal64Flyweight.MANTISSA_MIN;
    /** Mantissa of NaN, -2^60 + 1. */
    public static final long MANTISSA_ERROR = Decimal64Flyweight.MANTISSA_ERROR;

    private long fixedDecimal;

    // Factories rather than constants: a shared mutable instance could be changed by any caller.

    /**
     * Returns a new instance of the largest value.
     * @return new instance
     */
    public static MutableDecimal64 maxValue() {
        return new MutableDecimal64(Decimal64Flyweight.MAX_VALUE);
    }

    /**
     * Returns a new instance of the smallest value.
     * @return new instance
     */
    public static MutableDecimal64 minValue() {
        return new MutableDecimal64(Decimal64Flyweight.MIN_VALUE);
    }

    /**
     * Returns a new instance of zero.
     * @return new instance
     */
    public static MutableDecimal64 zero() {
        return new MutableDecimal64(0);
    }

    /**
     * Returns a new instance of NaN.
     * @return new instance
     */
    public static MutableDecimal64 nan() {
        return new MutableDecimal64(Decimal64Flyweight.NAN);
    }

    /**
     * Constructs an empty mutable decimal.
     */
    public MutableDecimal64() {
        fixedDecimal = 0;
    }

    /**
     * Constructs a mutable decimal from a decimal fly-weight value
     * @param value decimal fly-weight value
     */
    private MutableDecimal64(final long value) {
        fixedDecimal = value;
    }

    /**
     * Constructs a mutable decimal from another instance.
     * @param value fixed decimal
     */
    public MutableDecimal64(final MutableDecimal64 value) {
        fixedDecimal = value.fixedDecimal;
    }

    /**
     * Constructs a mutable decimal from a scaled mantissa and an exponent.
     * @param mantissa scaled mantissa
     * @param exponent       exponent
     */
    public MutableDecimal64(final long mantissa, final int exponent) {
        fixedDecimal = Decimal64Flyweight.valueOf(mantissa, exponent);
    }

    /**
     * Returns a mutable decimal from a scaled mantissa and an exponent, e.g. 12345 and -2 for
     * 123.45. A positive exponent scales the mantissa, e.g. 123 and 2 is 12300 with no decimals.
     * @param mantissa scaled mantissa
     * @param exponent       exponent, -Decimal64Flyweight.DECIMALS_MAX to Decimal64Flyweight.EXPONENT_MAX
     * @return new decimal, or NAN for an exponent out of range or overflow
     */
    public static MutableDecimal64 valueOf(final long mantissa, final int exponent) {
        return new MutableDecimal64(mantissa, exponent);
    }

    /**
     * Returns a mutable decimal parsed from a plain decimal string, e.g. "-123.45". The number of
     * decimals is kept, e.g. "1.50" has two. A sign other than a leading '-', a leading or trailing
     * point, exponent notation and more than Decimal64Flyweight.DECIMALS_MAX decimals are not
     * supported, see FixedDecimal for a lenient parser that rounds.
     * @param string decimal string
     * @return new decimal, or NAN for null, an invalid string, too many decimals or overflow
     */
    public static MutableDecimal64 valueOf(final String string) {
        final MutableDecimal64 fixed = new MutableDecimal64();
        fixed.fixedDecimal = Decimal64Flyweight.valueOf(string);
        return fixed;
    }

    /**
     * Returns a mutable decimal from a double, rounded half away from zero to a number of
     * decimals, e.g. valueOf(-2.5, 0) is -3. The double is rounded as stored, so 1.005, which
     * is slightly below 1.005 as a double, rounds to 1.00 with two decimals.
     * @param value    double
     * @param decimals number of decimals, 0 to Decimal64Flyweight.DECIMALS_MAX
     * @return new decimal, or NAN for a NaN or infinite double, decimals out of range or overflow
     */
    public static MutableDecimal64 valueOf(final double value, final int decimals) {
        final MutableDecimal64 fixed = new MutableDecimal64();
        fixed.fixedDecimal = Decimal64Flyweight.valueOf(value, decimals);
        return fixed;
    }

    /**
     * Returns a mutable decimal from a BigDecimal. The number of decimals is kept, but a value with
     * more than Decimal64Flyweight.DECIMALS_MAX decimals is rounded to that many according to the
     * rounding mode.
     * @param value   BigDecimal
     * @param context rounding mode
     * @return new decimal, or NAN for overflow or an inexact result with UNNECESSARY
     */
    public static MutableDecimal64 valueOf(final BigDecimal value, final DecimalContext context) {
        final MutableDecimal64 fixed = new MutableDecimal64();
        fixed.fixedDecimal = Decimal64Flyweight.valueOf(value, context);
        return fixed;
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
     * Indicates whether another object is a MutableDecimal64 of the same numeric value, e.g. 1.0
     * equals 1.00, consistent with compareTo and hashCode. NaN equals NaN.
     * @param object other object
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof MutableDecimal64 other && Decimal64Flyweight.equals(fixedDecimal, other.fixedDecimal);
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
    public int compareTo(final MutableDecimal64 value) {
        return Decimal64Flyweight.compareTo(fixedDecimal, value.fixedDecimal);
    }

    /**
     * Returns the unscaled mantissa of this instance.
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
     * @return this instance with the sum or NAN indicating overflow
     */
    public MutableDecimal64 add(final MutableDecimal64 value) {
        fixedDecimal = Decimal64Flyweight.add(fixedDecimal, value.fixedDecimal);
        return this;
    }

    /**
     * Returns the difference of this instance and its argument.
     * @param value term
     * @return this instance with the difference or NAN indicating overflow.
     */
    public MutableDecimal64 subtract(final MutableDecimal64 value) {
        fixedDecimal = Decimal64Flyweight.subtract(fixedDecimal, value.fixedDecimal);
        return this;
    }

    /**
     * Negates the value of this instance.
     * @return this instance with a negated value.
     */
    public MutableDecimal64 minus() {
        fixedDecimal = Decimal64Flyweight.minus(fixedDecimal);
        return this;
    }

    /**
     * Returns the product of this instance and its argument, rounded according to the rounding mode of the context.
     * @param value factor
     * @param context rounding mode
     * @return this instance with the product or NAN indicating overflow.
     */
    public MutableDecimal64 multiply(final MutableDecimal64 value, final DecimalContext context) {
        fixedDecimal = Decimal64Flyweight.multiply(fixedDecimal, value.fixedDecimal, context);
        return this;
    }

    /**
     * Returns the quotient of this instance and its argument, rounded according to the rounding mode of the context.
     * @param value divisor
     * @param context rounding mode
     * @return this instance with the quotient or NAN indicating overflow.
     */
    public MutableDecimal64 divide(final MutableDecimal64 value, final DecimalContext context) {
        fixedDecimal = Decimal64Flyweight.divide(fixedDecimal, value.fixedDecimal, context);
        return this;
    }

    /**
     * Returns this instance rounded according to the decimal rounding mode.
     * @param decimals mutable decimal value
     * @param context rounding mode
     * @return this instance rounded according to the rounding mode
     */
    public MutableDecimal64 round(final int decimals, final DecimalContext context) {
        fixedDecimal = Decimal64Flyweight.round(fixedDecimal, decimals, context);
        return this;
    }

    /**
     * Returns the absolute value of this instance.
     * @return this instance with absolute value applied.
     */
    public MutableDecimal64 abs() {
        fixedDecimal = Decimal64Flyweight.abs(fixedDecimal);
        return this;
    }

    /**
     * Multiplies this instance by an integer, exactly, keeping its decimals.
     * @param integer integer factor
     * @return this instance, NaN on overflow
     */
    public MutableDecimal64 multiplyByInteger(final long integer) {
        fixedDecimal = Decimal64Flyweight.multiplyByInteger(fixedDecimal, integer);
        return this;
    }

    /**
     * Divides this instance by an integer, rounded to its decimals.
     * @param integer integer divisor
     * @param context rounding mode
     * @return this instance, NaN on division by zero
     */
    public MutableDecimal64 divideByInteger(final long integer, final DecimalContext context) {
        fixedDecimal = Decimal64Flyweight.divideByInteger(fixedDecimal, integer, context);
        return this;
    }

    /**
     * Rounds this instance to a multiple of a positive increment, e.g. a tick size, with the
     * largest number of decimals of the two.
     * @param increment positive increment
     * @param context   rounding mode
     * @return this instance, NaN on overflow or an increment that is not positive
     */
    public MutableDecimal64 roundToIncrement(final MutableDecimal64 increment, final DecimalContext context) {
        fixedDecimal = Decimal64Flyweight.roundToIncrement(fixedDecimal, increment.fixedDecimal, context);
        return this;
    }

    /**
     * Sets this instance to the largest integer not above it, with no decimals.
     * @return this instance
     */
    public MutableDecimal64 floor() {
        fixedDecimal = Decimal64Flyweight.floor(fixedDecimal);
        return this;
    }

    /**
     * Sets this instance to the smallest integer not below it, with no decimals.
     * @return this instance
     */
    public MutableDecimal64 ceil() {
        fixedDecimal = Decimal64Flyweight.ceil(fixedDecimal);
        return this;
    }

    /**
     * Sets this instance to the remainder of dividing it by a divisor, exact, with its sign.
     * @param divisor divisor
     * @return this instance, NaN on division by zero
     */
    public MutableDecimal64 remainder(final MutableDecimal64 divisor) {
        fixedDecimal = Decimal64Flyweight.remainder(fixedDecimal, divisor.fixedDecimal);
        return this;
    }

    /**
     * Writes this instance as ASCII like toString, without allocating.
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
    public byte byteValue(DecimalContext context) {
        return Decimal64Flyweight.byteValue(fixedDecimal, context);
    }

    /**
     * Returns the value of the specified number as a short, which may involve rounding or truncation.
     * @param context rounding mode
     * @return short value
     * @throws ArithmeticException if this instance is NaN
     */
    public short shortValue(final DecimalContext context) {
        return Decimal64Flyweight.shortValue(fixedDecimal, context);
    }

    /**
     * Returns the value of the specified number as an integer, which may involve rounding or truncation.
     * @param context rounding mode
     * @return integer value
     * @throws ArithmeticException if this instance is NaN
     */
    public int intValue(final DecimalContext context) {
        return Decimal64Flyweight.intValue(fixedDecimal, context);
    }

    /**
     * Returns the value of the specified number as an integer, which may involve rounding or truncation.
     * @param context rounding mode
     * @return long value
     * @throws ArithmeticException if this instance is NaN
     */
    public long longValue(final DecimalContext context) {
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
     * Returns a new instance from a decimal fly-weight representation, like Decimal64.fromLongBits.
     * @param value decimal fly-weight value
     * @return new instance
     */
    public static MutableDecimal64 fromLongBits(final long value) {
        return new MutableDecimal64(value);
    }

    /**
     * Sets this instance to a decimal fly-weight representation.
     * @param value decimal fly-weight value
     * @return this instance
     */
    public MutableDecimal64 setLongBits(final long value) {
        fixedDecimal = value;
        return this;
    }
}
