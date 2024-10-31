package org.limitless.fixdec4j;

/**
 * This class implements a mutable fixed decimal number, see the DecimalFlyweight class for details.
 * @author fredrikdahlberg
 */
public final class MutableDecimal64 implements Comparable<MutableDecimal64> {
    // mantissa limits
    public static final long MANTISSA_MAX = Decimal64Flyweight.MANTISSA_MAX;
    public static final long MANTISSA_MIN = Decimal64Flyweight.MANTISSA_MIN;
    public static final long MANTISSA_ERROR = Decimal64Flyweight.MANTISSA_ERROR;

    // constants
    public static final MutableDecimal64 MAX_VALUE = new MutableDecimal64(Decimal64Flyweight.MAX_VALUE);
    public static final MutableDecimal64 MIN_VALUE = new MutableDecimal64(Decimal64Flyweight.MIN_VALUE);
    public static final MutableDecimal64 ZERO = new MutableDecimal64(0);
    public static final MutableDecimal64 NAN = new MutableDecimal64(Decimal64Flyweight.NAN);

    private long fixedDecimal;

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
     * Constructs a mutable decimal from a mantissa and an exponent.
     * @param mantissa scaled mantissa
     * @param exponent       exponent
     * @return mutable decimal instance
     */
    public static MutableDecimal64 valueOf(final long mantissa, final int exponent) {
        return new MutableDecimal64(mantissa, exponent);
    }

    /**
     * Constructs a mutable decimal value from a string.
     * @param string string
     * @return new mutable decimal instance
     */
    public static MutableDecimal64 valueOf(final String string) {
        final MutableDecimal64 fixed = new MutableDecimal64();
        fixed.fixedDecimal = Decimal64Flyweight.valueOf(string);
        return fixed;
    }

    /**
     * Constructs a mutable decimal value from a double.
     * @param value    double
     * @param decimals number of decimals
     * @return new mutable decimal instance
     */
    public static MutableDecimal64 valueOf(final double value, final int decimals) {
        final MutableDecimal64 fixed = new MutableDecimal64();
        fixed.fixedDecimal = Decimal64Flyweight.valueOf(value, decimals);
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

        return fixedDecimal == ((MutableDecimal64) object).fixedDecimal;
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
    public int compareTo(final MutableDecimal64 value) {
        if (value == null) {
            return 1;
        }

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
     * Returns the product of this instance and its argument rounding away from zero.
     * @param value factor
     * @param context helper
     * @return this instance with the product or NAN indicating overflow.
     */
    public MutableDecimal64 multiply(final MutableDecimal64 value, final MutableDecimal64.Context context) {
        fixedDecimal = Decimal64Flyweight.multiply(fixedDecimal, value.fixedDecimal, context);
        return this;
    }

    /**
     * Returns the product of this instance and its argument rounding away from zero.
     * @param value factor
     * @param context helper
     * @return this instance with the product or NAN indicating overflow.
     */
    public MutableDecimal64 multiply(final MutableDecimal64 value, final Decimal64.Context context) {
        fixedDecimal = Decimal64Flyweight.multiply(fixedDecimal, value.fixedDecimal, context);
        return this;
    }

    /**
     * Returns the quotient of this instance and its argument rounding away from zero.
     * @param value divisor
     * @param context helper
     * @return this instance with the quotient or NAN indicating overflow.
     */
    public MutableDecimal64 divide(final MutableDecimal64 value, final MutableDecimal64.Context context) {
        fixedDecimal = Decimal64Flyweight.divide(fixedDecimal, value.fixedDecimal, context);
        return this;
    }

    /**
     * Returns this instance rounded according to the decimal rounding mode.
     * @param decimals mutable decimal value
     * @param context helper
     * @return this instance rounded according to the rounding mode
     */
    public MutableDecimal64 round(final int decimals, final MutableDecimal64.Context context) {
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
     * Returns the value of the specified number as a byte, which may involve rounding or truncation.
     * @param context helper
     * @return byte value
     */
    public byte byteValue(MutableDecimal64.Context context) {
        return Decimal64Flyweight.byteValue(fixedDecimal, context);
    }

    /**
     * Returns the value of the specified number as a short, which may involve rounding or truncation.
     * @param context helper
     * @return short value
     */
    public short shortValue(final MutableDecimal64.Context context) {
        return Decimal64Flyweight.shortValue(fixedDecimal, context);
    }

    /**
     * Returns the value of the specified number as an integer, which may involve rounding or truncation.
     * @param context helper
     * @return integer value
     */
    public int intValue(final MutableDecimal64.Context context) {
        return Decimal64Flyweight.intValue(fixedDecimal, context);
    }

    /**
     * Returns the value of the specified number as an integer, which may involve rounding or truncation.
     * @param context helper
     * @return long value
     */
    public long longValue(final MutableDecimal64.Context context) {
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
     * Returns a mutable decimal instance constructed from a decimal fly-weight representation.
     * @param value decimal fly-weight value
     * @return decimal instance
     */
    public MutableDecimal64 fromLongBits(final long value) {
        fixedDecimal = value;
        return this;
    }

    public static final class Context extends Decimal64.Context {

       public Context(DecimalRounding mode) {
           super(mode);
       }

    }
}
