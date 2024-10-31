package org.limitless.fixdec4j;

/**
 * This class implements an immutable fixed decimal number, see the DecimalFlyweight for details.
 * @author fredrikdahlberg
 */
public final class Decimal64 implements Comparable<Decimal64> {
    public static final long MANTISSA_MAX = Decimal64Flyweight.MANTISSA_MAX;
    public static final long MANTISSA_MIN = Decimal64Flyweight.MANTISSA_MIN;
    public static final long MANTISSA_ERROR = Decimal64Flyweight.MANTISSA_ERROR;

    public static final Decimal64 MAX_VALUE = new Decimal64(Decimal64Flyweight.MAX_VALUE);
    public static final Decimal64 MIN_VALUE = new Decimal64(Decimal64Flyweight.MIN_VALUE);
    public static final Decimal64 ZERO = new Decimal64(0);
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
     * Constructs an immutable decimal from a scaled mantissa and an exponent.
     * @param scaledMantissa scaled mantissa
     * @param exponent       exponent
     * @return decimal instance
     */
    public static Decimal64 valueOf(final long scaledMantissa, final int exponent) {
        return new Decimal64(scaledMantissa, exponent);
    }

    /**
     * Constructs an immutable decimal value from a string.
     * @param value string
     * @return new mutable decimal instance
     */
    public static Decimal64 valueOf(final String value) {
        return new Decimal64(Decimal64Flyweight.valueOf(value));
    }

    /**
     * Constructs an immutable decimal value from a double.
     * @param value    double
     * @param decimals number of decimals
     * @return new mutable decimal instance
     */
    public static Decimal64 valueOf(final double value, final int decimals) {
        return new Decimal64(Decimal64Flyweight.valueOf(value, decimals));
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
     * Indicates whether some other object is equal to this one.
     * @param object other object
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        return fixedDecimal == ((Decimal64) object).fixedDecimal;
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
     */
    @Override
    public int compareTo(final Decimal64 value) {
        if (value == null) {
            return 1;
        }
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
     * Returns the product of this instance and its argument rounding away from zero.
     * @param value factor
     * @param context rounding mode
     * @return a new instance with the product or NAN indicating overflow.
     */
    public Decimal64 multiply(final Decimal64 value, final Decimal64.Context context) {
        return new Decimal64(Decimal64Flyweight.multiply(fixedDecimal, value.fixedDecimal, context));
    }

    /**
     * Returns the quotient of this instance.
     * @param value divisor
     * @param context rounding mode
     * @return a new instance with the quotient or NAN indicating overflow.
     */
    public Decimal64 divide(final Decimal64 value, final Decimal64.Context context) {
        return new Decimal64(Decimal64Flyweight.divide(fixedDecimal, value.fixedDecimal, context));
    }

    /**
     * Round the value to the specified number of decimals according to the rounding mode.
     * @param decimals number of decimals
     * @param context rounding mode
     * @return rounded value
     */
    public Decimal64 round(int decimals, Decimal64.Context context) {
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
     * Returns the value of the specified number as a byte, which may involve rounding or truncation.
     * @param context rounding mode
     * @return byte value
     */
    public byte byteValue(final Decimal64.Context context) {
        return Decimal64Flyweight.byteValue(fixedDecimal, context);
    }

    /**
     * Returns the value of the specified number as a short, which may involve rounding or truncation.
     * @param context rounding mode
     * @return short value
     */
    public short shortValue(Decimal64.Context context) {
        return Decimal64Flyweight.shortValue(fixedDecimal, context);
    }

    /**
     * Returns the value of the specified number as an integer, which may involve rounding or truncation.
     * @param context rounding mode
     * @return integer value
     */
    public int intValue(Decimal64.Context context) {
        return Decimal64Flyweight.intValue(fixedDecimal, context);
    }

    /**
     * Returns the value of the specified number as an integer, which may involve rounding or truncation.
     * @return long value
     */
    public long longValue(Decimal64.Context context) {
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

    public static class Context extends MutableUnsigned128.Context {

        public Context(DecimalRounding mode) {
            super(mode);
        }
    }
}
