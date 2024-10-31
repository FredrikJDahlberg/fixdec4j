package org.limitless.fixdec4j;

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
 * The supported decimal rounding modes are Up and Down, see RoundingMode.
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

        long mantissa1 = mantissa(decimal1);
        long mantissa2 = mantissa(decimal2);
        if ((mantissa1 < 0) != (mantissa2 < 0)) {
            return mantissa1 < 0 ? -1 : 1;
        }

        final int decimals1 = -exponent(decimal1);
        final int decimals2 = -exponent(decimal2);
        int mantissaBits1 = Unsigned64Flyweight.numberOfBits(Math.abs(mantissa1));
        int mantissaBits2 = Unsigned64Flyweight.numberOfBits(Math.abs(mantissa2));
        if (decimals1 != decimals2) {
            final long scale = Powers10[Math.abs(decimals1 - decimals2)];
            final long scaleBits = Unsigned64Flyweight.numberOfBits(scale);

            if (decimals1 < decimals2) {
                mantissaBits1 += (int) scaleBits;
                mantissa1 *= scale;
            } else {
                mantissaBits2 += (int) scaleBits;
                mantissa2 *= scale;
            }
        }
        if (mantissa1 == mantissa2) {
            return 0;
        } else {
            final int result = mantissaBits1 < mantissaBits2 ? -1 : 1;
            return mantissa1 < 0 && mantissa2 < 0 ? -result : result;
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
        final boolean sameSign = inValueMantissa < 0 == inTermMantissa < 0;
        int valuePower = Unsigned64Flyweight.numberOfBits(Math.abs(inValueMantissa));
        int termPower = Unsigned64Flyweight.numberOfBits(Math.abs(inTermMantissa));
        int resultPower = sameSign ? Math.max(valuePower, termPower) : Math.abs(valuePower - termPower);
        long result = MANTISSA_ERROR;
        // The maximum value of the mantissa is equal to Long.MAX_VALUE/8 so the additions below cannot overflow,
        // MANTISSA_MAX*2 = Long.MAX_VALUE/4.
        if (resultPower > MANTISSA_BITS) {
            return NAN;
        }

        long valueMantissa = inValueMantissa;
        long termMantissa = inTermMantissa;
        if (inValueDecimals != inTermDecimals) {
            final long scale = Powers10[Math.abs(inValueDecimals - inTermDecimals)];
            if (inValueDecimals < inTermDecimals) {
                valueMantissa *= scale;
                valuePower = Unsigned64Flyweight.numberOfBits(Math.abs(valueMantissa));
            } else {
                termMantissa *= scale;
                termPower = Unsigned64Flyweight.numberOfBits(Math.abs(termMantissa));
            }
            resultPower = sameSign ? Math.max(valuePower, termPower) : Math.abs(valuePower - termPower);
        }
        if (resultPower <= MANTISSA_BITS) {
            result = valueMantissa + termMantissa;
        }
        return encode(result, -Math.max(inValueDecimals, inTermDecimals));
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
    public static long multiply(final long value, final long factor, Decimal64.Context context) {
        if (isNaN(value) || isNaN(factor)) {
            return NAN;
        }

        final long valueMantissa = mantissa(value);
        final long factorMantissa = mantissa(factor);
        final int valueDecimals = -exponent(value);
        final int factorDecimals = -exponent(factor);
        final int decimals = Math.max(valueDecimals, factorDecimals);
        final long scaleDiff = Powers10[Math.abs(valueDecimals - factorDecimals)];
        final int scaleBits = Unsigned64Flyweight.numberOfBits(scaleDiff);
        final int valueBits = Unsigned64Flyweight.numberOfBits(Math.abs(valueMantissa));
        final int factorBits = Unsigned64Flyweight.numberOfBits(Math.abs(factorMantissa));
        final long result;
        if (valueBits + factorBits + scaleBits < MANTISSA_BITS) {
            result = roundedMultiply(valueMantissa, valueDecimals, factorMantissa, factorDecimals, context.mode);
        } else {
            result = roundedMultiply128(valueMantissa, valueDecimals, factorMantissa, factorDecimals, context);
        }
        return encode(result, -decimals);
    }

    /**
     * Divides 64-bit dividend with 64-bit divisor (using 128-bit arithmetic)
     * @param dividend 64-bit fixed decimal flyweight
     * @param divisor  64-bit fixed decimal flyweight
     * @return rounded quotient 64-bit fixed decimal flyweight
     */
    public static long divide(final long dividend, final long divisor, final Decimal64.Context context) {
        if (isNaN(dividend) || isNaN(divisor) || divisor == 0) {
            return NAN;
        }

        final int dividendDecimals = -exponent(dividend);
        final int divisorDecimals = -exponent(divisor);
        final long dividendMantissa = mantissa(dividend);
        final long divisorMantissa = mantissa(divisor);
        int quotientPower2 = Unsigned64Flyweight.numberOfBits(dividend);
        if (dividendDecimals != divisorDecimals) {
            if (dividendDecimals < divisorDecimals) {
                quotientPower2 += Unsigned64Flyweight.numberOfBits(dividendMantissa);
            } else {
                quotientPower2 += Unsigned64Flyweight.numberOfBits(divisorMantissa);
            }
        }

        final int decimals = Math.max(dividendDecimals, divisorDecimals);
        final long scaling = Powers10[decimals + Math.abs(dividendDecimals - divisorDecimals)];
        quotientPower2 += Unsigned64Flyweight.numberOfBits(scaling);
        final long quotient;
        if (quotientPower2 < Long.SIZE) {
            quotient = roundedDivide(dividendMantissa, dividendDecimals, divisorMantissa, divisorDecimals, context);
        } else {
            quotient = roundedDivide128(dividendMantissa, dividendDecimals, divisorMantissa, divisorDecimals, context);
        }
        return encode(quotient, -decimals);
    }

    /**
     * Returns a decimal flyweight rounded according to the decimal rounding
     * mode.
     * @param value decimal flyweight value
     * @param context   helper
     * @return decimal flyweight according to the rounding mode or NAN
     * indicating overflow
     */
    public static long round(final long value, final int decimals, final Decimal64.Context context) {
        if (isNaN(value) || decimals < 0 || decimals > DECIMALS_MAX) {
            return NAN;
        }

        final int decimalCount = -exponent(value);
        if (decimalCount == decimals) {
            return value;
        }

        final long fixedScale = valueOf(Powers10[Math.abs(decimalCount - decimals)], 0);
        final long rounded;
        if (decimalCount > decimals) {
            rounded = divide(value, fixedScale, context);
        } else {
            rounded = multiply(value, fixedScale, context);
        }
        return encode(mantissa(rounded), -decimals);
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
    public static byte byteValue(final long value, final Decimal64.Context context) {
        return (byte) longValue(value, context);
    }

    /**
     * Returns the value of the specified number as a short, which may involve
     * rounding or truncation.
     * @param value decimal flyweight value
     * @return short value
     */
    public static short shortValue(final long value, final Decimal64.Context context) {
        return (short) longValue(value, context);
    }

    /**
     * Returns the value of the specified number as an integer, which may
     * involve rounding or truncation.
     * @param value decimal flyweight value
     * @return integer value
     */
    public static int intValue(final long value, final Decimal64.Context context) {
        return (int) longValue(value, context);
    }

    /**
     * Returns the value of the specified number as an integer, which may
     * involve rounding or truncation.
     * @param value decimal flyweight value
     * @return long value or NAN indicating overflow
     */
    public static long longValue(final long value, Decimal64.Context context) {
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
     * Returns a string representation of the object.
     * @param value decimal flyweight value
     * @return string or "NAN" indicating overflow
     */
    public static String toString(final long value) {
        if (isNaN(value)) {
            return "NaN";
        }

        final int exponent = exponent(value);
        final int decimalCount = exponent < 0 ? -exponent : 0;
        long mantissa = mantissa(value);
        byte sign = 0;
        if (mantissa < 0) {
            sign = '-';
            mantissa = -mantissa;
        }

        final byte[] string = new byte[64];
        int length = 0;
        long integerValue = mantissa;
        if (exponent < 0) {
            integerValue = mantissa / Powers10[decimalCount];
            long decimalValue = mantissa % Powers10[decimalCount];
            longToString(decimalValue, decimalCount + 1, string);
            length += decimalCount + 1;
            string[20] = '.';
        }
        longToString(integerValue, 0, string);

        int integerDigits = digitsBase10(integerValue);
        length += integerDigits;

        int offset = 20 - integerDigits;
        if (sign != 0) {
            --offset;
            string[offset] = sign;
            ++length;
        }
        return new String(string, offset, length);
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
        long loValue = low * (f1_10_000_000 + 1) - (low / 4);
        long hiValue = high * (f1_10_000_000 + 1) - (high / 4);
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
     * Returns the product of two decimal flyweight values rounded according to the rounding mode.
     * @param valueMantissa  scaled mantissa of the value
     * @param valueDecimals  unsigned decimal count of the value
     * @param factorMantissa scaled mantissa of the factor
     * @param factorDecimals unsigned decimal count of the factor
     * @return the rounded product
     */
    private static long roundedMultiply(final long valueMantissa,
                                        final int valueDecimals,
                                        final long factorMantissa,
                                        final int factorDecimals,
                                        final DecimalRounding mode) {
        final int decimals = Math.max(valueDecimals, factorDecimals);
        long product = Math.abs(valueMantissa * factorMantissa);
        if (valueDecimals != factorDecimals) {
            product *= Powers10[Math.abs(valueDecimals - factorDecimals)];
        }

        final long scale = Powers10[decimals];
        if (mode == DecimalRounding.UP) {
            product += (scale >>> 1);
        }
        product /= scale;

        if ((valueMantissa < 0) != (factorMantissa < 0)) {
            product = -product;
        }
        return product;
    }

    /**
     * Returns the product of two decimal flyweight values rounded according to the rounding mode.
     * @param valueMantissa  scaled mantissa of the value
     * @param valueDecimals  unsigned decimal count of the value
     * @param factorMantissa scaled mantissa of the factor
     * @param factorDecimals unsigned decimal count of the factor
     * @param context rounding mode
     * @return product
     */
    private static long roundedMultiply128(final long valueMantissa,
                                           final int valueDecimals,
                                           final long factorMantissa,
                                           final int factorDecimals,
                                           final Decimal64.Context context) {
        final MutableUnsigned128 product = context.product.set(Math.abs(valueMantissa));
        if (valueDecimals != factorDecimals) {
            final long scale = Powers10[Math.abs(valueDecimals - factorDecimals)];
            product.multiply(context.scale.set(scale));
        }

        final MutableUnsigned128 factor = context.factor.set(Math.abs(factorMantissa));
        product.multiply(factor);

        final long scale = Powers10[Math.max(valueDecimals, factorDecimals)];
        if (scale == 1) {
            product.divide(scale, context);
        } else {
            final MutableUnsigned128 rounding = context.rounding.set(scale).shiftRight(1);
            final MutableUnsigned128 remainder = context.remainder1;
            context.q1.set(product).divide(rounding, remainder, context);
            if (remainder.isZero()) {
                product.divide(context.scale.set(scale), remainder, context);
                if (remainder.compareTo(rounding) == 0 && context.mode == DecimalRounding.UP) {
                    product.increment();
                }
            } else {
                if (context.mode == DecimalRounding.UP) {
                    product.add(rounding);
                }
                product.divide(scale, context);
            }
        }
        return mantissaValue(product, valueMantissa < 0 != factorMantissa < 0);
    }

    /**
     * Returns the quotient of its arguments.
     * @param dividendMantissa scaled dividend mantissa
     * @param dividendDecimals number of dividend decimals
     * @param inDivisorMantissa  scaled divisor mantissa
     * @param divisorDecimals  number of divisor decimals
     * @param context         helper
     * @return signed 64-bit quotient
     */
    private static long roundedDivide(final long dividendMantissa,
                                      final int dividendDecimals,
                                      final long inDivisorMantissa,
                                      final int divisorDecimals,
                                      final Decimal64.Context context) {
        long quotientMantissa = Math.abs(dividendMantissa);
        long divisorMantissa = Math.abs(inDivisorMantissa);
        int decimals = Math.max(divisorDecimals, dividendDecimals);
        if (dividendDecimals != divisorDecimals) {
            final long scale = Powers10[Math.abs(dividendDecimals - divisorDecimals)];
            if (dividendDecimals < divisorDecimals) {
                quotientMantissa *= scale;
            } else {
                divisorMantissa *= scale;
            }
        }
        quotientMantissa *= Powers10[decimals];

        final long rounding = divisorMantissa >>> 1;
        if (rounding == 0) {
            quotientMantissa /= divisorMantissa;
        } else {
            if (quotientMantissa % rounding == 0) {
                final long remainder = quotientMantissa % divisorMantissa;
                quotientMantissa /= divisorMantissa;
                if (remainder == rounding && context.mode == DecimalRounding.UP) {
                    ++quotientMantissa;
                }
            } else {
                if (context.mode == DecimalRounding.UP) {
                    quotientMantissa += rounding;
                }
                quotientMantissa /= divisorMantissa;
            }
        }
        if ((inDivisorMantissa < 0) != (dividendMantissa < 0)) {
            quotientMantissa = -quotientMantissa;
        }
        return quotientMantissa;
    }

    /**
     * Returns the quotient of its arguments.
     * @param dividendMantissa scaled dividend mantissa
     * @param dividendDecimals number of dividend decimals
     * @param divisorMantissa  scaled divisor mantissa
     * @param divisorDecimals  number of divisor decimals
     * @param context          helper
     * @return signed 64-bit quotient
     */
    private static long roundedDivide128(final long dividendMantissa,
                                         final int dividendDecimals,
                                         final long divisorMantissa,
                                         final int divisorDecimals,
                                         final Decimal64.Context context) {
        final MutableUnsigned128 quotient = new MutableUnsigned128(Math.abs(dividendMantissa));
        final MutableUnsigned128 divisor = new MutableUnsigned128(Math.abs(divisorMantissa));

        if (dividendDecimals != divisorDecimals) {
            final long scale = Powers10[Math.abs(dividendDecimals - divisorDecimals)];
            if (dividendDecimals < divisorDecimals) {
                quotient.multiply(scale, context);
            } else {
                divisor.multiply(scale, context);
            }
        }
        quotient.multiply(Powers10[Math.max(dividendDecimals, divisorDecimals)], context);

        final MutableUnsigned128 rounding = new MutableUnsigned128(divisor).shiftRight(1);
        if (rounding.isZero()) {
            quotient.divide(divisor, context);
        } else {
            context.q1.set(quotient).divide(rounding, context.remainder2, context);
            if (context.remainder2.isZero()) {
                quotient.divide(divisor, context.remainder2, context);
                if (context.remainder2.compareTo(rounding) == 0 && context.mode == DecimalRounding.UP) {
                    quotient.increment();
                }
            } else {
                if (context.mode == DecimalRounding.UP) {
                    quotient.add(rounding);
                }
                quotient.divide(divisor, context);
            }
        }
        return mantissaValue(quotient, dividendMantissa < 0 != divisorMantissa < 0);
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
     * Returns a mantissa value checked for overflow
     * @param value  128-bit unsigned value
     * @param signed sign indicator
     * @return verified mantissa or MANTISSA_ERROR indicating overflow
     */
    private static long mantissaValue(final MutableUnsigned128 value, final boolean signed) {
        long mantissa = MANTISSA_ERROR;
        if (value.fitsLong()) {
            mantissa = value.longValue();
            if (mantissa >= MANTISSA_MIN && mantissa <= MANTISSA_MAX) {
                if (signed) {
                    mantissa = -mantissa;
                }
            } else {
                mantissa = MANTISSA_ERROR;
            }
        }
        return mantissa;
    }

    // limit encoded digit count
    private static final long[] digitsBase2 = {
        4294967296L,  8589934582L,  8589934582L,
        8589934582L,  12884901788L, 12884901788L,
        12884901788L, 17179868184L, 17179868184L,
        17179868184L, 21474826480L, 21474826480L,
        21474826480L, 21474826480L, 25769703776L,
        25769703776L, 25769703776L, 30063771072L,
        30063771072L, 30063771072L, 34349738368L,
        34349738368L, 34349738368L, 34349738368L,
        38554705664L, 38554705664L, 38554705664L,
        41949672960L, 41949672960L, 41949672960L,
        42949672960L, 42949672960L,
    };

    private static final int INT_MAX_DIGITS = digitsBase10(Integer.MAX_VALUE);

    public static int digitsBase10(int value) {
        int count = Integer.SIZE - Integer.numberOfLeadingZeros(value);
        return (int) ((value + digitsBase2[count]) >>> 32);
    }

    public static int digitsBase10(long value) {
        final int digits;
        if (value <= Integer.MAX_VALUE) {
            digits = digitsBase10((int) value);
        } else {
            digits = digitsBase10(value >>> Integer.SIZE) + INT_MAX_DIGITS - 1;
        }
        return digits;
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
}
