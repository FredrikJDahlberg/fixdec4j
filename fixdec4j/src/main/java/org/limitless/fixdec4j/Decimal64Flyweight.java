package org.limitless.fixdec4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * This class implements fixed decimal arithmetic using a 61-bit (two's
 * complement) mantissa and 3-bit unsigned value for decimals: 1mmm mmmm 2mmm
 * mmmm 3mmm mmmm 4mmm mmmm 5mmm mmmm 6mmm mmmm 7mmm mmmm 8mmm mddd.
 * <p>
 * The mantissa ranges from -2^60+2 to 2^60-1 and the decimals from 0 to 7, and
 * Not a Number is represented by the mantissa -2^60+1 (MANTISSA_ERROR).
 * <p>
 * The multiplication and division methods will round the result properly to the
 * largest precision of its operands. Note that only the number of decimals is
 * stored, e.g. 100e0 and 1e2 are normalized to 100e0.
 * <p>
 * The supported rounding modes match java.math.RoundingMode, see DecimalRounding.
 * <p>
 * The arithmetic operations will not overflow unless the result cannot be
 * represented within the limits above (intermediate values use 128-bit
 * arithmetic when necessary).
 * @author fredrikdahlberg
 */
public final class Decimal64Flyweight {
    private static final long DECIMAL_BITS = 3;
    private static final long DECIMAL_MASK = (1L << DECIMAL_BITS) - 1;

    // limits
    private static final long MANTISSA_BITS = Long.SIZE - DECIMAL_BITS;
    /** Largest number of decimals of a value. */
    public static final int DECIMALS_MAX = (1 << DECIMAL_BITS) - 1;
    /** Smallest exponent accepted by valueOf(long, int), -DECIMALS_MAX. */
    public static final int EXPONENT_MIN = -DECIMALS_MAX;
    /** Largest exponent accepted by valueOf(long, int), which scales the mantissa. */
    public static final int EXPONENT_MAX = 18;

    /** Largest mantissa, 2^60 - 1. */
    public static final long MANTISSA_MAX = (Long.MAX_VALUE >>> DECIMAL_BITS);
    /** Smallest mantissa, -2^60 + 2. */
    public static final long MANTISSA_MIN = -MANTISSA_MAX + 1;
    /** Mantissa of NaN, -2^60 + 1. */
    public static final long MANTISSA_ERROR = -MANTISSA_MAX;
    // out of the mantissa range with either sign
    private static final long OVERFLOW = Long.MAX_VALUE;
    private static final long INT_MASK = 0xffffffffL;

    // constants
    /** Not a number, the result of an overflow, an invalid string or a division by zero. */
    public static final long NAN = MANTISSA_ERROR << DECIMAL_BITS;
    /** Largest value, MANTISSA_MAX with no decimals. */
    public static final long MAX_VALUE = valueOf(MANTISSA_MAX, 0);
    /** Smallest value, MANTISSA_MIN with no decimals. */
    public static final long MIN_VALUE = valueOf(MANTISSA_MIN, 0);
    /** Largest number of bytes written by toBytes: a sign, 19 digits and the point. */
    public static final int STRING_LENGTH_MAX = FixedFlyweight.STRING_LENGTH_MAX;

    private static final int PARSE_STATE_MANTISSA = 1;
    private static final int PARSE_STATE_EXPONENT = 2;
    private static final int PARSE_POINT = '.' - '0';

    private Decimal64Flyweight() {
    }

    /**
     * Returns a decimal flyweight from a scaled mantissa and an exponent, e.g. 12345 and -2 for
     * 123.45. A positive exponent scales the mantissa, e.g. 123 and 2 is 12300 with no decimals.
     * @param scaledMantissa scaled mantissa
     * @param valueExponent  exponent, EXPONENT_MIN to EXPONENT_MAX
     * @return decimal flyweight, or NAN for an exponent out of range or overflow
     */
    public static long valueOf(final long scaledMantissa, final int valueExponent) {
        boolean valid = valueExponent >= -DECIMALS_MAX && valueExponent <= EXPONENT_MAX;
        long mantissa = Math.abs(scaledMantissa);
        int exponent = valueExponent;
        if (valid) {
            if (valueExponent >= 1) {
                final long scale = Powers10[valueExponent];
                final int bitCount = Unsigned64Flyweight.numberOfBits(Math.abs(mantissa)) +
                    Unsigned64Flyweight.numberOfBits(scale);
                valid = bitCount <= MANTISSA_BITS;
                mantissa *= scale;
                exponent = 0;
            }
        }
        valid &= mantissa <= MANTISSA_MAX;
        if (scaledMantissa < 0) {
            mantissa = -mantissa;
        }
        return valid ? encode(mantissa, exponent) : NAN;
    }

    /**
     * Returns a decimal flyweight parsed from a plain decimal string, e.g. "-123.45". The number of
     * decimals is kept, e.g. "1.50" has two. A sign other than a leading '-', a leading or trailing
     * point, exponent notation and more than DECIMALS_MAX decimals are not
     * supported, see FixedDecimal for a lenient parser that rounds.
     * @param string decimal string
     * @return decimal flyweight, or NAN for null, an invalid string, too many decimals or overflow
     */
    public static long valueOf(final String string) {
        if (string == null) {
            return NAN;
        }

        final long length = string.length();
        if (length == 0) {
            return NAN;
        }

        final boolean positive = string.charAt(0) != '-';
        int position = positive ? 0 : 1;
        if (position == length) {
            return NAN;
        }

        int digit = string.charAt(position++) - '0';
        if (digit < 0 || digit >= 10) {
            return NAN;
        }

        final long minimum = Long.MIN_VALUE / 10;
        long mantissa = -digit;
        int decimals = 0;
        int state = PARSE_STATE_MANTISSA;
        while (position < length) {
            digit = string.charAt(position++) - '0';
            if ((digit < 0 && digit != PARSE_POINT) || digit >= 10 || mantissa < minimum) {
                return NAN;
            }
            if (digit == PARSE_POINT) {
                if (state != PARSE_STATE_MANTISSA) {
                    return NAN;
                }
                state = PARSE_STATE_EXPONENT;
            } else {
                if (state == PARSE_STATE_EXPONENT) {
                    ++decimals;
                }
                mantissa *= 10;
                mantissa -= digit;
            }
        }
        if (digit == PARSE_POINT || mantissa < -MANTISSA_MAX || decimals > DECIMALS_MAX) {
            return NAN;
        }
        if (positive) {
            mantissa = -mantissa;
        }
        return encode(mantissa, -decimals);
    }

    /**
     * Returns a decimal flyweight from a double, rounded half away from zero to a number of
     * decimals, e.g. valueOf(-2.5, 0) is -3. The double is rounded as stored, so 1.005, which
     * is slightly below 1.005 as a double, rounds to 1.00 with two decimals.
     * @param value    double
     * @param decimals number of decimals, 0 to DECIMALS_MAX
     * @return decimal flyweight, or NAN for a NaN or infinite double, decimals out of range or overflow
     */
    public static long valueOf(final double value, final int decimals) {
        if (Double.isNaN(value) || decimals < 0 || decimals > DECIMALS_MAX) {
            return NAN;
        }
        final long mantissa = Math.round(Math.abs(value) * Powers10[decimals]);
        return encode(value < 0 ? -mantissa : mantissa, -decimals);
    }

    /**
     * Returns a decimal flyweight from a BigDecimal. The number of decimals is kept, but a value with
     * more than DECIMALS_MAX decimals is rounded to that many according to the
     * rounding mode.
     * @param value   BigDecimal
     * @param context rounding mode
     * @return decimal flyweight, or NAN for overflow or an inexact result with UNNECESSARY
     */
    public static long valueOf(final BigDecimal value, final DecimalContext context) {
        BigDecimal decimal = value;
        if (decimal.scale() > DECIMALS_MAX) {
            if (decimal.precision() - decimal.scale() < -DECIMALS_MAX) {
                // below 10^-8 in magnitude, which rounds like 1E-9 with the same sign in every mode,
                // but avoids scaling a huge scale such as 1E-1000000
                decimal = BigDecimal.valueOf(decimal.signum(), DECIMALS_MAX + 2);
            }
            if (context.mode == DecimalRounding.UNNECESSARY &&
                decimal.stripTrailingZeros().scale() > DECIMALS_MAX) {
                return NAN;
            }
            decimal = decimal.setScale(DECIMALS_MAX, context.mode.toRoundingMode());
        } else if (decimal.scale() < 0) {
            // at least 20 integer digits is at least 10^19, above MANTISSA_MAX, which also avoids
            // scaling a huge exponent such as 1E+1000000
            if (decimal.signum() != 0 && decimal.precision() - decimal.scale() > 19) {
                return NAN;
            }
            decimal = decimal.setScale(0);
        }
        final BigInteger mantissa = decimal.unscaledValue();
        if (mantissa.bitLength() >= Long.SIZE) {
            return NAN;
        }
        return encode(mantissa.longValue(), -decimal.scale());
    }

    /**
     * Returns a hash code for a decimal flyweight, the same for numerically equal values such as
     * 1.0 and 1.00, consistent with equals.
     * @param decimal decimal flyweight
     * @return hash code
     */
    public static int hashCode(final long decimal) {
        return Long.hashCode(stripTrailingZeros(decimal));
    }

    /**
     * Returns whether two decimal flyweights are numerically equal, e.g. 1.0 and 1.00, consistent
     * with compareTo returning 0. NaN equals NaN. Compare with == to also require the same number
     * of decimals.
     * @param decimal1 decimal flyweight value
     * @param decimal2 decimal flyweight value
     * @return true when numerically equal
     */
    public static boolean equals(final long decimal1, final long decimal2) {
        // every value has a single representation without trailing zeros
        return decimal1 == decimal2 || stripTrailingZeros(decimal1) == stripTrailingZeros(decimal2);
    }

    /**
     * Returns a decimal flyweight with the fewest decimals that represent the same value, e.g. 1.5
     * for 1.500 and 0 for 0.00.
     * @param value decimal flyweight value
     * @return value without trailing zeros, NAN for NAN
     */
    public static long stripTrailingZeros(final long value) {
        if (isNaN(value)) {
            return NAN;
        }
        long mantissa = mantissa(value);
        int decimals = -exponent(value);
        while (decimals > 0 && mantissa % 10 == 0) {
            mantissa /= 10;
            --decimals;
        }
        return encode(mantissa, -decimals);
    }

    /**
     * Compares two flyweight values for order.
     * @param decimal1 decimal flyweight value
     * @param decimal2 decimal flyweight value
     * @return less than (-1), equals (0) or greater than (1) the other object.
     */
    public static int compareTo(final long decimal1, final long decimal2) {
        if (decimal1 == decimal2) {
            return 0;
        }

        final long mantissa1 = mantissa(decimal1);
        final long mantissa2 = mantissa(decimal2);
        final int decimals1 = -exponent(decimal1);
        final int decimals2 = -exponent(decimal2);
        if (decimals1 == decimals2) {
            return Long.compare(mantissa1, mantissa2);
        }
        if ((mantissa1 < 0) != (mantissa2 < 0)) {
            return mantissa1 < 0 ? -1 : 1;
        }
        // Scale the mantissa with fewer decimals, if that overflows its magnitude is larger than the other
        if (decimals1 < decimals2) {
            final int scale = decimals2 - decimals1;
            if (Math.abs(mantissa1) > SCALE_LIMITS[scale]) {
                return mantissa1 < 0 ? -1 : 1;
            }
            return Long.compare(mantissa1 * Powers10[scale], mantissa2);
        } else {
            final int scale = decimals1 - decimals2;
            if (Math.abs(mantissa2) > SCALE_LIMITS[scale]) {
                return mantissa2 < 0 ? 1 : -1;
            }
            return Long.compare(mantissa1, mantissa2 * Powers10[scale]);
        }
    }

    /**
     * Returns the unscaled mantissa of its argument.
     * @param decimal decimal flyweight value
     * @return unscaled mantissa, MANTISSA_ERROR for NAN
     */
    public static long mantissa(final long decimal) {
        return decimal >> DECIMAL_BITS; // NAN is MANTISSA_ERROR << DECIMAL_BITS
    }

    /**
     * Returns the normalized exponent of this instance. Positive exponents are
     * stored as a scaled mantissa with a zero exponent.
     * @param decimal decimal flyweight value
     * @return negative exponent or 0
     */
    public static int exponent(final long decimal) {
        return (int) -(decimal & DECIMAL_MASK);
    }

    /**
     * Returns true when the instance is equal to NAN.
     * @param decimal decimal flyweight value
     * @return true when equal to NAN
     */
    public static boolean isNaN(final long decimal) {
        return decimal == NAN;
    }

    /**
     * Returns true when the mantissa of the value is zero (disregards the
     * exponent)
     * @param decimal decimal flyweight value
     * @return true for zero values
     */
    public static boolean isZero(final long decimal) {
        return mantissa(decimal) == 0;
    }

    /**
     * Returns the sum of two decimal flyweight values.
     * @param value decimal flyweight value
     * @param term    decimal flyweight value
     * @return sum or NAN indicating overflow.
     */
    public static long add(final long value, final long term) {
        if (isNaN(value) || isNaN(term)) {
            return NAN;
        }
        return add(mantissa(value), -exponent(value), mantissa(term), -exponent(term));
    }

    /**
     * Returns the sum of its arguments
     * @param inValueMantissa value mantissa
     * @param inValueDecimals number of value decimals
     * @param inTermMantissa  term mantissa
     * @param inTermDecimals  number of term decimals
     * @return sum or NAN indicating overflow
     */
    private static long add(final long inValueMantissa,
                            final int inValueDecimals,
                            final long inTermMantissa,
                            final int inTermDecimals) {
        long valueMantissa = inValueMantissa;
        long termMantissa = inTermMantissa;
        // A mantissa scaled beyond 2^62 cannot be cancelled by the other (at most 2^60), so the sum
        // overflows. Otherwise the sum is below 2^63 and encode checks the range.
        if (inValueDecimals < inTermDecimals) {
            final int scale = inTermDecimals - inValueDecimals;
            if (Math.abs(valueMantissa) > ADD_LIMITS[scale]) {
                return NAN;
            }
            valueMantissa *= Powers10[scale];
        } else if (inValueDecimals > inTermDecimals) {
            final int scale = inValueDecimals - inTermDecimals;
            if (Math.abs(termMantissa) > ADD_LIMITS[scale]) {
                return NAN;
            }
            termMantissa *= Powers10[scale];
        }
        return encode(valueMantissa + termMantissa, -Math.max(inValueDecimals, inTermDecimals));
    }

    /**
     * Returns the difference of two decimal flyweight values.
     * @param value decimal flyweight value
     * @param term    decimal flyweight value
     * @return difference or NAN indicating overflow.
     */
    public static long subtract(final long value, final long term) {
        if (isNaN(value) || isNaN(term)) {
            return NAN;
        }
        return add(mantissa(value), -exponent(value), -mantissa(term), -exponent(term));
    }

    /**
     * Returns the negated value of its argument.
     * @param value decimal flyweight value
     * @return a negated decimal flyweight value.
     */
    public static long minus(final long value) {
        if (isNaN(value)) {
            return NAN;
        }

        final long mantissa = -mantissa(value);
        final int exponent = exponent(value);
        return encode(mantissa, exponent);
    }

    /**
     * Returns the product of two decimal flyweight values rounded according to
     * the rounding mode.
     * @param value   decimal flyweight value
     * @param factor  decimal flyweight value
     * @param context rounding mode
     * @return product of the values or NAN indicating overflow
     */
    public static long multiply(final long value, final long factor, DecimalContext context) {
        if (isNaN(value) || isNaN(factor)) {
            return NAN;
        }

        // The product of a / 10^da and b / 10^db with max(da, db) decimals has the mantissa
        // a * b * 10^max(da, db) / 10^(da + db) = a * b / 10^min(da, db)
        final long valueMantissa = mantissa(value);
        final long factorMantissa = mantissa(factor);
        final int valueDecimals = -exponent(value);
        final int factorDecimals = -exponent(factor);
        final boolean negative = (valueMantissa < 0) != (factorMantissa < 0);
        final long product = roundedMultiply(Math.abs(valueMantissa), Math.abs(factorMantissa),
            Math.min(valueDecimals, factorDecimals), negative, context.mode);
        return encode(negative ? -product : product,
            -Math.max(valueDecimals, factorDecimals));
    }

    /**
     * Divides 64-bit dividend with 64-bit divisor (using 128-bit arithmetic)
     * @param dividend 64-bit fixed decimal flyweight
     * @param divisor  64-bit fixed decimal flyweight
     * @param context  rounding mode
     * @return rounded quotient 64-bit fixed decimal flyweight
     */
    public static long divide(final long dividend, final long divisor, final DecimalContext context) {
        if (isNaN(dividend) || isNaN(divisor) || isZero(divisor)) {
            return NAN;
        }

        // The quotient of a / 10^da and b / 10^db with max(da, db) decimals has the mantissa
        // a * 10^(db + max(da, db) - da) / b, i.e. only the dividend is scaled (by at most 10^14)
        final long dividendMantissa = mantissa(dividend);
        final long divisorMantissa = mantissa(divisor);
        final int dividendDecimals = -exponent(dividend);
        final int divisorDecimals = -exponent(divisor);
        final int decimals = Math.max(dividendDecimals, divisorDecimals);
        final boolean negative = (dividendMantissa < 0) != (divisorMantissa < 0);
        final long quotient = roundedDivide(Math.abs(dividendMantissa), divisorDecimals + decimals - dividendDecimals,
            Math.abs(divisorMantissa), negative, context);
        return encode(negative ? -quotient : quotient, -decimals);
    }

    /**
     * Returns a decimal flyweight rounded according to the decimal rounding
     * mode.
     * @param value    decimal flyweight value
     * @param decimals number of decimals, 0 to DECIMALS_MAX
     * @param context  rounding mode
     * @return decimal flyweight according to the rounding mode or NAN
     * indicating overflow
     */
    public static long round(final long value, final int decimals, final DecimalContext context) {
        if (isNaN(value) || decimals < 0 || decimals > DECIMALS_MAX) {
            return NAN;
        }

        final int decimalCount = -exponent(value);
        if (decimalCount == decimals) {
            return value;
        }

        final long mantissa = mantissa(value);
        if (decimalCount < decimals) {
            final int scale = decimals - decimalCount;
            if (Math.abs(mantissa) > SCALE_LIMITS[scale]) {
                return NAN;
            }
            return encode(mantissa * Powers10[scale], -decimals);
        }

        final int scale = decimalCount - decimals;
        final long magnitude = Math.abs(mantissa);
        final long quotient = PowersOf10.divide(magnitude, scale);
        final long remainder = magnitude - quotient * Powers10[scale];
        final long rounded = roundQuotient(quotient, remainder, Powers10[scale], mantissa < 0, context.mode);
        return encode(mantissa < 0 ? -rounded : rounded, -decimals);
    }

    /**
     * Returns the absolute value of the decimal flyweight.
     * @param value decimal flyweight value
     * @return the absolute value its argument
     */
    public static long abs(final long value) {
        if (isNaN(value) || value >= 0) {
            return value;
        }
        return encode(-mantissa(value), exponent(value));
    }

    /**
     * Returns a decimal flyweight times an integer, exact, with the decimals of the value.
     * @param value   decimal flyweight value
     * @param integer integer factor
     * @return product or NAN indicating overflow
     */
    public static long multiplyByInteger(final long value, final long integer) {
        if (isNaN(value)) {
            return NAN;
        }
        final long mantissa = mantissa(value);
        final long high = Math.multiplyHigh(mantissa, integer);
        final long low = mantissa * integer;
        // encode checks the mantissa range of a product that fits in 64 bits
        return high != (low >> (Long.SIZE - 1)) ? NAN : encode(low, exponent(value));
    }

    /**
     * Returns a decimal flyweight divided by an integer, rounded to the decimals of the value.
     * @param value   decimal flyweight value
     * @param integer integer divisor
     * @param context rounding mode
     * @return quotient or NAN indicating division by zero (or by Long.MIN_VALUE)
     */
    public static long divideByInteger(final long value, final long integer, final DecimalContext context) {
        if (isNaN(value) || integer == 0 || integer == Long.MIN_VALUE) {
            return NAN;
        }
        final long mantissa = mantissa(value);
        final long magnitude = Math.abs(mantissa);
        final long divisor = Math.abs(integer);
        final boolean negative = (mantissa < 0) != (integer < 0);
        final long quotient = magnitude / divisor;
        final long rounded = roundQuotient(quotient, magnitude - quotient * divisor, divisor, negative, context.mode);
        return encode(negative ? -rounded : rounded, exponent(value));
    }

    /**
     * Returns a decimal flyweight rounded to a multiple of a positive increment, e.g. a price
     * rounded to a tick size of 0.05, with the largest number of decimals of the two.
     * @param value     decimal flyweight value
     * @param increment positive decimal flyweight increment
     * @param context   rounding mode
     * @return rounded value or NAN indicating overflow or an increment that is not positive
     */
    public static long roundToIncrement(final long value, final long increment, final DecimalContext context) {
        if (isNaN(value) || isNaN(increment) || mantissa(increment) <= 0) {
            return NAN;
        }
        final long mantissa = mantissa(value);
        final int valueDecimals = -exponent(value);
        final int incrementDecimals = -exponent(increment);
        final int decimals = Math.max(valueDecimals, incrementDecimals);
        final long magnitude = Math.abs(mantissa);
        if (magnitude > SCALE_LIMITS[decimals - valueDecimals]) {
            // at least 2^63 units, while the increment is at most 2^60: any multiple near it overflows
            return NAN;
        }
        final long x = magnitude * Powers10[decimals - valueDecimals];
        // An increment of at least 2^63 units exceeds twice the value, which then rounds like
        // Long.MAX_VALUE units: to 0, or to a multiple that overflows in any case
        final long y = mantissa(increment) > SCALE_LIMITS[decimals - incrementDecimals] ? Long.MAX_VALUE :
            mantissa(increment) * Powers10[decimals - incrementDecimals];
        final long quotient = x / y;
        final long multiple = roundQuotient(quotient, x - quotient * y, y, mantissa < 0, context.mode);
        final long high = Math.multiplyHigh(multiple, y);
        final long rounded = multiple * y;
        if (multiple == OVERFLOW || high != 0 || rounded < 0) {
            return NAN;
        }
        return encode(mantissa < 0 ? -rounded : rounded, -decimals);
    }

    /**
     * Returns the largest integer not above a decimal flyweight, with no decimals.
     * @param value decimal flyweight value
     * @return integer value
     */
    public static long floor(final long value) {
        return round(value, 0, DecimalContext.FLOOR);
    }

    /**
     * Returns the smallest integer not below a decimal flyweight, with no decimals.
     * @param value decimal flyweight value
     * @return integer value
     */
    public static long ceil(final long value) {
        return round(value, 0, DecimalContext.CEILING);
    }

    /**
     * Returns the remainder of dividing two decimal flyweights, exact, with the sign of the
     * dividend like BigDecimal.remainder and the largest number of decimals of the two.
     * @param dividend decimal flyweight value
     * @param divisor  decimal flyweight value
     * @return remainder or NAN indicating division by zero
     */
    public static long remainder(final long dividend, final long divisor) {
        if (isNaN(dividend) || isNaN(divisor) || isZero(divisor)) {
            return NAN;
        }
        final long dividendMantissa = mantissa(dividend);
        final int dividendDecimals = -exponent(dividend);
        final int divisorDecimals = -exponent(divisor);
        final long x = Math.abs(dividendMantissa);
        final long y = Math.abs(mantissa(divisor));
        final long remainder;
        if (dividendDecimals >= divisorDecimals) {
            // only the divisor is scaled, and a divisor of at least 2^63 units exceeds the dividend
            final int scale = dividendDecimals - divisorDecimals;
            remainder = y > SCALE_LIMITS[scale] ? x : x % (y * Powers10[scale]);
        } else {
            // the dividend times 10^scale in 128 bits, reduced modulo the divisor
            final long scale = Powers10[divisorDecimals - dividendDecimals];
            final long high = Math.multiplyHigh(x, scale);
            final long low = x * scale;
            if (high == 0 && low >= 0) {
                remainder = low % y;
            } else {
                final long quotient = MutableUnsigned128.divide(high % y, low, y);
                remainder = low - quotient * y; // below 2^64, so the low 64 bits are exact
            }
        }
        return encode(dividendMantissa < 0 ? -remainder : remainder, -Math.max(dividendDecimals, divisorDecimals));
    }

    /**
     * Writes a decimal flyweight as ASCII like toString, e.g. a field of a FIX message, without
     * allocating.
     * @param value  decimal flyweight value
     * @param bytes  buffer
     * @param offset index of the first byte
     * @return number of bytes written, at most STRING_LENGTH_MAX
     * @throws IndexOutOfBoundsException if the string does not fit in the buffer
     */
    public static int toBytes(final long value, final byte[] bytes, final int offset) {
        // the mantissa is never Long.MIN_VALUE, which FixedFlyweight writes as NaN
        return isNaN(value) ? FixedFlyweight.toBytes(FixedFlyweight.NAN, 0, bytes, offset) :
            FixedFlyweight.toBytes(mantissa(value), -exponent(value), bytes, offset);
    }

    /**
     * Returns the value of the specified number as a byte, which may involve
     * rounding or truncation.
     * @param value decimal flyweight value
     * @param context rounding mode, e.g. DOWN to truncate
     * @return byte value
     * @throws ArithmeticException if the value is NaN
     */
    public static byte byteValue(final long value, final DecimalContext context) {
        return (byte) longValue(value, context);
    }

    /**
     * Returns the value of the specified number as a short, which may involve
     * rounding or truncation.
     * @param value decimal flyweight value
     * @param context rounding mode, e.g. DOWN to truncate
     * @return short value
     * @throws ArithmeticException if the value is NaN
     */
    public static short shortValue(final long value, final DecimalContext context) {
        return (short) longValue(value, context);
    }

    /**
     * Returns the value of the specified number as an integer, which may
     * involve rounding or truncation.
     * @param value decimal flyweight value
     * @param context rounding mode, e.g. DOWN to truncate
     * @return integer value
     * @throws ArithmeticException if the value is NaN
     */
    public static int intValue(final long value, final DecimalContext context) {
        return (int) longValue(value, context);
    }

    /**
     * Returns the value of the specified number as a long, which may
     * involve rounding or truncation.
     * @param value decimal flyweight value
     * @param context rounding mode, e.g. DOWN to truncate
     * @return long value
     * @throws ArithmeticException if the value is NaN
     */
    public static long longValue(final long value, final DecimalContext context) {
        if (isNaN(value)) {
            throw new ArithmeticException("NaN cannot be converted to an integer");
        }
        return mantissa(round(value, 0, context));
    }

    /**
     * Returns the value of the specified number as a float, which may involve
     * rounding.
     * @param value decimal flyweight value
     * @return float value or NAN indicating overflow
     */
    public static float floatValue(long value) {
        if (isNaN(value)) {
            return Float.NaN;
        }

        final long mantissa = mantissa(value);
        final int exponent = exponent(value);
        return mantissa / (float) Powers10[-exponent];
    }

    /**
     * Returns the value of the specified number as a double, which may involve
     * rounding.
     * @param value decimal flyweight value
     * @return double value or NAN indicating overflow
     */
    public static double doubleValue(long value) {
        if (isNaN(value)) {
            return Double.NaN;
        }

        final long mantissa = mantissa(value);
        final int exponent = exponent(value);
        return mantissa / (double) Powers10[-exponent];
    }

    /**
     * Returns the exact value as a BigDecimal with the same number of decimals.
     * @param value decimal flyweight value
     * @return BigDecimal value
     * @throws ArithmeticException if the value is NaN, which BigDecimal cannot represent
     */
    public static BigDecimal toBigDecimal(final long value) {
        if (isNaN(value)) {
            throw new ArithmeticException("NaN cannot be converted to BigDecimal");
        }
        return BigDecimal.valueOf(mantissa(value), -exponent(value));
    }

    /**
     * Returns a string representation of the object.
     * @param value decimal flyweight value
     * @return string or "NAN" indicating overflow
     */
    public static String toString(final long value) {
        if (isNaN(value)) {
            return "NaN";
        }
        return toString(mantissa(value), -exponent(value));
    }

    /**
     * Returns the plain string of mantissa / 10^decimals with exactly that many decimals, e.g.
     * "-0.0120" for -120 and 4 decimals.
     * @param mantissa mantissa, not Long.MIN_VALUE
     * @param decimals number of decimals, at most 18
     * @return string
     */
    static String toString(final long mantissa, final int decimals) {
        // The digits of m / 10^d are the digits of m with a point inserted before the last d digits.
        // 24 zero padded digits are written at 0..23, eight at a time, then the last d digits are
        // moved one position right to make room for the point.
        final long magnitude = Math.abs(mantissa);
        final byte[] buffer = new byte[25];
        final long upper = magnitude / 100_000_000L;
        final long top = magnitude / 10_000_000_000_000_000L; // below 1000
        Swar.store(buffer, 0, Swar.formatDigits((int) top));
        Swar.store(buffer, 8, Swar.formatDigits((int) (upper - top * 100_000_000L)));
        Swar.store(buffer, 16, Swar.formatDigits((int) (magnitude - upper * 100_000_000L)));
        int start = 24 - Math.max(digitsBase10(magnitude), decimals + 1);
        int end = 24;
        if (decimals > 0) {
            System.arraycopy(buffer, 24 - decimals, buffer, 25 - decimals, decimals);
            buffer[24 - decimals] = '.';
            end = 25;
        }
        if (mantissa < 0) {
            buffer[--start] = '-';
        }
        return new String(buffer, start, end - start, StandardCharsets.ISO_8859_1);
    }

    /**
     * This method is adapted from an algorithm invented by Terje Mathisen.
     * @param value value
     * @param offset buffer start index
     * @param buffer output
     */
    public static void longToString(final long value, final int offset, final byte[] buffer) {
        final long f1_10_000_000 = (1L << 60) / 1_000_000_000L;
        final long low = value % 10_000_000_000L;
        final long high = value / 10_000_000_000L;
        // 2^60 / 10^9 = f1_10_000_000 + 0.393..., the corrections keep the fixed point value above the
        // exact value by less than one unit of the last digit (up to 10^10 - 1)
        long loValue = low * (f1_10_000_000 + 1) - (low / 4) - (low / 8);
        long hiValue = high * (f1_10_000_000 + 1) - (high / 4) - (high / 8);
        long mask = Long.MAX_VALUE >>> 3;
        long shift = 60;
        for(int i = 0; i < 10; ++i) {
            buffer[i + offset] = (byte) ('0' + (hiValue >>> shift));
            buffer[i + offset + 10] = (byte) ('0' + (loValue >>> shift));
            hiValue = (hiValue & mask) * 5;
            loValue = (loValue & mask) * 5;
            mask >>>= 1;
            --shift;
        }
    }

    /**
     * Returns the product of two non-negative mantissas divided by a power of ten, rounded
     * according to the rounding mode.
     * @param value  non-negative mantissa
     * @param factor non-negative mantissa
     * @param exponent power of ten exponent of the divisor, at most DECIMALS_MAX (10^7 is below 2^24)
     * @param negative whether the signed product is negative
     * @param mode   rounding mode
     * @return non-negative rounded quotient or OVERFLOW
     */
    private static long roundedMultiply(final long value,
                                        final long factor,
                                        final int exponent,
                                        final boolean negative,
                                        final DecimalRounding mode) {
        final long scale = Powers10[exponent];
        final long quotient;
        final long remainder;
        if (Unsigned64Flyweight.numberOfBits(value) + Unsigned64Flyweight.numberOfBits(factor) < Long.SIZE - 1) {
            // product below 2^62
            final long product = value * factor;
            quotient = PowersOf10.divide(product, exponent);
            remainder = product - quotient * scale;
        } else {
            // 128-bit product divided in 32-bit digits, which cannot overflow since the scale is below 2^24
            // non-negative factors, so the signed high 64 bits are the unsigned ones
            final long high = Math.multiplyHigh(value, factor);
            if (high >= scale) {
                return OVERFLOW; // quotient of at least 2^64
            }
            final long low = value * factor;
            final long upper = (high << Integer.SIZE) | (low >>> Integer.SIZE);
            final long upperQuotient = PowersOf10.divide(upper, exponent);
            final long lower = ((upper - upperQuotient * scale) << Integer.SIZE) | (low & INT_MASK);
            final long lowerQuotient = PowersOf10.divide(lower, exponent);
            remainder = lower - lowerQuotient * scale;
            quotient = (upperQuotient << Integer.SIZE) | lowerQuotient;
            if (quotient < 0 || quotient > MANTISSA_MAX) {
                return OVERFLOW;
            }
        }
        return roundQuotient(quotient, remainder, scale, negative, mode);
    }

    /**
     * Returns the quotient of a non-negative dividend scaled by a power of ten and a positive
     * divisor, rounded according to the rounding mode.
     * @param dividend non-negative mantissa
     * @param scale    power of ten exponent of the dividend scaling
     * @param divisor  positive mantissa
     * @param negative whether the signed quotient is negative
     * @param context  rounding mode
     * @return non-negative rounded quotient or OVERFLOW
     */
    private static long roundedDivide(final long dividend,
                                      final int scale,
                                      final long divisor,
                                      final boolean negative,
                                      final DecimalContext context) {
        final long quotient;
        final long remainder;
        if (dividend <= SCALE_LIMITS[scale]) {
            final long scaled = dividend * Powers10[scale];
            quotient = scaled / divisor;
            remainder = scaled - quotient * divisor;
        } else {
            final long high = Math.multiplyHigh(dividend, Powers10[scale]);
            final long low = dividend * Powers10[scale];
            // A valid quotient is at most MANTISSA_MAX = 2^60 - 1, i.e. scaled / 2^60 < divisor
            final int bits = (int) MANTISSA_BITS - 1;
            if (((high << (Long.SIZE - bits)) | (low >>> bits)) >= divisor) {
                return OVERFLOW;
            }
            quotient = MutableUnsigned128.divide(high, low, divisor);
            remainder = low - quotient * divisor; // below 2^64, so the low 64 bits are exact
        }
        return roundQuotient(quotient, remainder, divisor, negative, context.mode);
    }

    /**
     * Returns a truncated non-negative quotient rounded according to the rounding mode, using
     * the remainder of the division to decide whether to increment its magnitude.
     * @param quotient  non-negative truncated quotient
     * @param remainder remainder, 0 &lt;= remainder &lt; divisor
     * @param divisor   positive divisor
     * @param negative  whether the signed result is negative
     * @param mode      rounding mode
     * @return non-negative rounded quotient, or OVERFLOW for an inexact result and UNNECESSARY
     */
    private static long roundQuotient(final long quotient,
                                      final long remainder,
                                      final long divisor,
                                      final boolean negative,
                                      final DecimalRounding mode) {
        // Compare remainder / divisor with 0.5 without overflowing 2 * remainder
        final long half = divisor - remainder;
        if (mode == DecimalRounding.HALF_UP) {
            // common case kept small enough to inline into the arithmetic
            return remainder >= half ? quotient + 1 : quotient;
        }
        if (remainder == 0) {
            return quotient;
        }
        return roundInexact(quotient, remainder, half, negative, mode);
    }

    /**
     * Returns an inexact truncated non-negative quotient rounded according to any mode but HALF_UP.
     * @param quotient  non-negative truncated quotient
     * @param remainder remainder, 0 &lt; remainder &lt; divisor
     * @param half      divisor - remainder
     * @param negative  whether the signed result is negative
     * @param mode      rounding mode
     * @return non-negative rounded quotient, or OVERFLOW for UNNECESSARY
     */
    private static long roundInexact(final long quotient,
                                     final long remainder,
                                     final long half,
                                     final boolean negative,
                                     final DecimalRounding mode) {
        if (mode == DecimalRounding.UNNECESSARY) {
            return OVERFLOW;
        }
        final boolean increment = switch (mode) {
            case UP -> true;
            case DOWN, UNNECESSARY -> false;
            case CEILING -> !negative;
            case FLOOR -> negative;
            case HALF_UP -> remainder >= half;
            case HALF_DOWN -> remainder > half;
            case HALF_EVEN -> remainder > half || (remainder == half && (quotient & 1) != 0);
        };
        return increment ? quotient + 1 : quotient;
    }

    /**
     * Returns an encoded decimal flyweight, but preserves error values.
     * @param mantissa scaled mantissa
     * @param exponent       exponent
     * @return decimal flyweight value or NAN indicating overflow.
     */
    private static long encode(final long mantissa, final int exponent) {
        if (mantissa >= MANTISSA_MIN && mantissa <= MANTISSA_MAX) {
            return (mantissa << DECIMAL_BITS) | (-exponent & DECIMAL_MASK);
        } else {
            return NAN;
        }
    }

    /**
     * Returns the number of decimal digits of a non-negative value, e.g. 1 for 0 and 4 for 4711.
     * The number of bits times log10(2) (1233 / 4096) is either the digit count or one less.
     * @param value non-negative value
     * @return number of decimal digits
     */
    public static int digitsBase10(int value) {
        final int bits = Integer.SIZE - Integer.numberOfLeadingZeros(value | 1);
        final int digits = (bits * 1233) >>> 12;
        return (value | 1) >= Powers10[digits] ? digits + 1 : digits;
    }

    /**
     * Returns the number of decimal digits of a non-negative value, e.g. 1 for 0 and 4 for 4711.
     * The number of bits times log10(2) (1233 / 4096) is either the digit count or one less.
     * @param value non-negative value
     * @return number of decimal digits
     */
    public static int digitsBase10(long value) {
        final int bits = Long.SIZE - Long.numberOfLeadingZeros(value | 1);
        final int digits = (bits * 1233) >>> 12;
        return (value | 1) >= Powers10[digits] ? digits + 1 : digits;
    }

    private static final long[] Powers10 = {
        1L, // 10^0
        10L, // 10^1
        100L, // 10^2
        1000L, // 10^3
        10000L, // 10^4
        100000L, // 10^5
        1000000L, // 10^6
        10000000L, // 10^7
        100000000L, // 10^8
        1000000000L, // 10^9
        10000000000L, // 10^10
        100000000000L, // 10^11
        1000000000000L, // 10^12
        10000000000000L, // 10^13
        100000000000000L, // 10^14
        1000000000000000L, // 10^15
        10000000000000000L, // 10^16
        100000000000000000L, // 10^17
        1000000000000000000L, // 10^18
        // 9223372036854775807L
    };

    // SCALE_LIMITS[k] is the largest value that can be multiplied by 10^k without overflow
    private static final long[] SCALE_LIMITS = new long[Powers10.length];

    // ADD_LIMITS[k] is the largest value that can be multiplied by 10^k without exceeding 2^62
    private static final long[] ADD_LIMITS = new long[Powers10.length];

    static {
        for (int i = 0; i < Powers10.length; ++i) {
            SCALE_LIMITS[i] = Long.MAX_VALUE / Powers10[i];
            ADD_LIMITS[i] = (1L << 62) / Powers10[i];
        }
    }
}
