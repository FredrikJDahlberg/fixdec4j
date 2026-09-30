package org.limitless.fixdec4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * This class implements fixed decimal arithmetic using a 61-bit (two's
 * complement) mantissa and 3-bit unsigned value for decimals: 1mmm mmmm 2mmm
 * mmmm 3mmm mmmm 4mmm mmmm 5mmm mmmm 6mmm mmmm 7mmm mmmm 8mmm mddd.
 * <p>
 * The largest mantissa ranges from -2^60-2 to 2^60-1, decimals 0 - 7 and Not a
 * Number is represented as 2^60-1.
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
    public static final int DECIMALS_MAX = (1 << DECIMAL_BITS) - 1;
    public static final int EXPONENT_MIN = -DECIMALS_MAX;
    public static final int EXPONENT_MAX = 18;

    public static final long MANTISSA_MAX = (Long.MAX_VALUE >>> DECIMAL_BITS);
    public static final long MANTISSA_MIN = -MANTISSA_MAX + 1;
    public static final long MANTISSA_ERROR = -MANTISSA_MAX;
    // out of the mantissa range with either sign
    private static final long OVERFLOW = Long.MAX_VALUE;
    private static final long INT_MASK = 0xffffffffL;

    // constants
    public static final long NAN = MANTISSA_ERROR << DECIMAL_BITS;
    public static final long MAX_VALUE = valueOf(MANTISSA_MAX, 0);
    public static final long MIN_VALUE = valueOf(MANTISSA_MIN, EXPONENT_MIN);

    private static final int PARSE_STATE_MANTISSA = 1;
    private static final int PARSE_STATE_EXPONENT = 2;
    private static final int PARSE_POINT = '.' - '0';

    /**
     * Constructs a decimal flyweight from a scaled mantissa and an exponent. A
     * positive exponent is stored as zero decimals, with the mantissa scaled
     * accordingly.
     * @param scaledMantissa scaled mantissa, e.g. 200.50 is 20050e-2
     * @param valueExponent       exponent
     * @return decimal flyweight or NAN indicating overflow
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
     * Constructs a decimal flyweight value from a string ("-123.45678").
     * Exponent notation is not supported.
     * @param string string
     * @return 64-bit encoded fixed decimal or NaN
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
     * Constructs a decimal flyweight from a double.
     * @param value    double value
     * @param decimals number of decimals
     * @return decimal flyweight or NAN indicating overflow
     */
    public static long valueOf(final double value, final int decimals) {
        if (Double.isNaN(value) || decimals < 0 || decimals > DECIMALS_MAX) {
            return NAN;
        }
        final long mantissa = Math.round(Math.abs(value) * Powers10[decimals]);
        return encode(value < 0 ? -mantissa : mantissa, -decimals);
    }

    /**
     * Constructs a decimal flyweight from a BigDecimal. The number of decimals is kept, but a value
     * with more than DECIMALS_MAX decimals is rounded to DECIMALS_MAX according to the rounding mode.
     * @param value   BigDecimal value
     * @param context rounding mode
     * @return decimal flyweight, or NAN indicating overflow or an inexact result with UNNECESSARY
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
     * Returns a hash code for a decimal flyweight
     * @param decimal decimal flyweight
     * @return hash code
     */
    public static int hashCode(long decimal) {
        return Long.hashCode(decimal);
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
     * @return unscaled mantissa
     */
    public static long mantissa(final long decimal) {
        return decimal == NAN ? MANTISSA_ERROR : (decimal >> DECIMAL_BITS);
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
     * @param value decimal flyweight value
     * @param factor      decimal flyweight value
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
     * @param value decimal flyweight value
     * @param context   helper
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
        final long quotient = divideByPowerOf10(magnitude, scale);
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
     * Returns the value of the specified number as a byte, which may involve
     * rounding or truncation.
     * @param value decimal flyweight value
     * @return byte value
     */
    public static byte byteValue(final long value, final DecimalContext context) {
        return (byte) longValue(value, context);
    }

    /**
     * Returns the value of the specified number as a short, which may involve
     * rounding or truncation.
     * @param value decimal flyweight value
     * @return short value
     */
    public static short shortValue(final long value, final DecimalContext context) {
        return (short) longValue(value, context);
    }

    /**
     * Returns the value of the specified number as an integer, which may
     * involve rounding or truncation.
     * @param value decimal flyweight value
     * @return integer value
     */
    public static int intValue(final long value, final DecimalContext context) {
        return (int) longValue(value, context);
    }

    /**
     * Returns the value of the specified number as an integer, which may
     * involve rounding or truncation.
     * @param value decimal flyweight value
     * @return long value or NAN indicating overflow
     */
    public static long longValue(final long value, DecimalContext context) {
        if (isNaN(value)) {
            return NAN;
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
            // product below 2^62, plain division measured faster here than divideByPowerOf10 (Apple M-series)
            final long product = value * factor;
            quotient = product / scale;
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
            final long upperQuotient = upper / scale;
            final long lower = ((upper - upperQuotient * scale) << Integer.SIZE) | (low & INT_MASK);
            final long lowerQuotient = lower / scale;
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
     * @param context  helper
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
            final MutableUnsigned128 result = context.remainder;
            quotient = MutableUnsigned128.divide(high, low, divisor, result);
            remainder = result.lowBits();
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
     * Returns a non-negative value divided by 10^exponent. The divisors are constants so that the
     * JIT replaces the division with a multiplication by the reciprocal.
     * @param value    non-negative value
     * @param exponent power of ten exponent, at most DECIMALS_MAX
     * @return quotient
     */
    private static long divideByPowerOf10(final long value, final int exponent) {
        switch (exponent) {
            case 0:
                return value;
            case 1:
                return value / 10L;
            case 2:
                return value / 100L;
            case 3:
                return value / 1_000L;
            case 4:
                return value / 10_000L;
            case 5:
                return value / 100_000L;
            case 6:
                return value / 1_000_000L;
            case 7:
                return value / 10_000_000L;
            default:
                return value / Powers10[exponent];
        }
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
