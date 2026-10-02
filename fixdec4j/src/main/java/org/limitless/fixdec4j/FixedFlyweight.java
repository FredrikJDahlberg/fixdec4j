package org.limitless.fixdec4j;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Fixed-scale decimal arithmetic on a long holding the value times 10^decimals, e.g. 123.45 with
 * 2 decimals is 12345. The number of decimals is not stored in the value but passed to every
 * operation, normally through a FixedDecimal. Long.MIN_VALUE is NaN, so a value ranges from
 * -Long.MAX_VALUE to Long.MAX_VALUE units of 10^-decimals.
 * <p>
 * Divisions by powers of ten multiply by a precomputed reciprocal, see PowersOf10, so they cost
 * the same whether or not the number of decimals is a constant.
 * Products and scaled dividends use 128-bit intermediates, so multiply and divide only overflow
 * when the result does not fit.
 * @author fredrikdahlberg
 */
final class FixedFlyweight {
    static final long NAN = Long.MIN_VALUE;
    static final int INT_NAN = Integer.MIN_VALUE;
    static final int DECIMALS_MAX = 9;
    // a sign, 19 digits and the point
    static final int STRING_LENGTH_MAX = 21;

    // negative magnitude signalling overflow, or an inexact result with UNNECESSARY
    private static final long OVERFLOW = -1;
    private static final long INT_MASK = 0xffffffffL;
    // any digit can be appended to a smaller value without overflow, and at most 7 to this one
    private static final long APPEND_LIMIT = Long.MAX_VALUE / 10;

    private static final long[] POWERS10 = {
        1L, 10L, 100L, 1_000L, 10_000L, 100_000L, 1_000_000L, 10_000_000L, 100_000_000L,
        1_000_000_000L, 10_000_000_000L, 100_000_000_000L, 1_000_000_000_000L,
        10_000_000_000_000L, 100_000_000_000_000L, 1_000_000_000_000_000L,
        10_000_000_000_000_000L, 100_000_000_000_000_000L, 1_000_000_000_000_000_000L
    };

    private FixedFlyweight() {
    }

    static long add(final long value, final long term) {
        final long sum = value + term;
        // overflow when the sum has the opposite sign of both operands
        if (value == NAN || term == NAN || sum == NAN || ((value ^ sum) & (term ^ sum)) < 0) {
            return NAN;
        }
        return sum;
    }

    static long subtract(final long value, final long term) {
        final long difference = value - term;
        // overflow when the operands differ in sign and the difference has the sign of the term
        if (value == NAN || term == NAN || difference == NAN || ((value ^ term) & (value ^ difference)) < 0) {
            return NAN;
        }
        return difference;
    }

    /**
     * Returns the product of two values with any number of decimals, rounded to decimals.
     */
    static long multiply(final long value, final int valueDecimals, final long factor, final int factorDecimals,
                         final int decimals, final DecimalContext context) {
        if (value == NAN || factor == NAN) {
            return NAN;
        }
        final long x = Math.abs(value);
        final long y = Math.abs(factor);
        final boolean negative = (value ^ factor) < 0;
        // the product has valueDecimals + factorDecimals decimals
        final int exponent = valueDecimals + factorDecimals - decimals;
        final long magnitude;
        if (exponent >= 0) {
            if (numberOfBits(x) + numberOfBits(y) < Long.SIZE) {
                // product below 2^63
                magnitude = roundedDivideByPowerOf10(x * y, exponent, negative, context.mode);
            } else {
                magnitude = roundedDivideByPowerOf10(Math.multiplyHigh(x, y), x * y, exponent, negative, context);
            }
        } else {
            magnitude = scaleUp(Math.multiplyHigh(x, y), x * y, -exponent);
        }
        return magnitude < 0 ? NAN : negative ? -magnitude : magnitude;
    }

    /**
     * Returns the quotient of two values with any number of decimals, rounded to decimals.
     */
    static long divide(final long dividend, final int dividendDecimals, final long divisor, final int divisorDecimals,
                       final int decimals, final DecimalContext context) {
        if (dividend == NAN || divisor == NAN || divisor == 0) {
            return NAN;
        }
        final long x = Math.abs(dividend);
        final long y = Math.abs(divisor);
        final boolean negative = (dividend ^ divisor) < 0;
        // x / 10^dx / (y / 10^dy) with d decimals has the magnitude x * 10^(dy + d - dx) / y
        final int exponent = divisorDecimals + decimals - dividendDecimals;
        final long magnitude;
        if (exponent >= 0) {
            final long scale = POWERS10[exponent];
            magnitude = roundedDivide(Math.multiplyHigh(x, scale), x * scale, y, negative, context);
        } else {
            magnitude = roundedDivideByScaledDivisor(x, y, -exponent, negative, context.mode);
        }
        return magnitude < 0 ? NAN : negative ? -magnitude : magnitude;
    }

    /**
     * Returns a value times an integer, exact, or NaN on overflow.
     */
    static long multiplyByInteger(final long value, final long integer) {
        final long high = Math.multiplyHigh(value, integer);
        final long low = value * integer;
        // NaN is checked explicitly since NaN times 0 is 0
        return value == NAN || high != (low >> (Long.SIZE - 1)) || low == NAN ? NAN : low;
    }

    /**
     * Returns a value with decimals rounded to places decimals, still with decimals decimals. Fewer
     * than zero places round to tens, hundreds and so on.
     */
    static long round(final long value, final int decimals, final int places, final DecimalContext context) {
        if (value == NAN || places >= decimals) {
            return value;
        }
        final int exponent = decimals - places;
        if (exponent > PowersOf10.EXPONENT_MAX) {
            return NAN;
        }
        final long magnitude = roundedDivideByPowerOf10(Math.abs(value), exponent, value < 0, context.mode);
        // also OVERFLOW for a negative magnitude, an inexact result with UNNECESSARY
        final long rounded = scaleUp(0, magnitude, exponent);
        return rounded < 0 ? NAN : value < 0 ? -rounded : rounded;
    }

    /**
     * Returns the largest integer not above a value, with decimals decimals.
     */
    static long floor(final long value, final int decimals) {
        return round(value, decimals, 0, DecimalContext.FLOOR);
    }

    /**
     * Returns the smallest integer not below a value, with decimals decimals, or NaN on overflow.
     */
    static long ceil(final long value, final int decimals) {
        return round(value, decimals, 0, DecimalContext.CEILING);
    }

    /**
     * Returns the remainder of two values in the same scale, exact, with the sign of the dividend.
     */
    static long remainder(final long dividend, final long divisor) {
        return dividend == NAN || divisor == NAN || divisor == 0 ? NAN : dividend % divisor;
    }

    /**
     * Returns a value rounded to a multiple of a positive increment in the same scale, e.g. a tick size.
     */
    static long roundToIncrement(final long value, final long increment, final DecimalContext context) {
        if (value == NAN || increment <= 0) {
            return NAN;
        }
        final long magnitude = Math.abs(value);
        final long quotient = magnitude / increment;
        final long multiple = round(quotient, magnitude - quotient * increment, increment, value < 0, context.mode);
        final long high = Math.multiplyHigh(multiple, increment);
        final long rounded = multiple * increment;
        return multiple < 0 || high != 0 || rounded < 0 ? NAN : value < 0 ? -rounded : rounded;
    }

    /**
     * Returns a value with fromDecimals decimals converted to decimals, rounded if decimals are removed.
     */
    static long rescale(final long value, final int fromDecimals, final int decimals, final DecimalContext context) {
        if (value == NAN || fromDecimals == decimals) {
            return value;
        }
        if (decimals > fromDecimals) {
            final long scale = POWERS10[decimals - fromDecimals];
            final long high = Math.multiplyHigh(value, scale);
            final long low = value * scale;
            return high != (low >> (Long.SIZE - 1)) || low == NAN ? NAN : low;
        }
        final long magnitude = roundedDivideByPowerOf10(Math.abs(value), fromDecimals - decimals, value < 0,
            context.mode);
        return magnitude < 0 ? NAN : value < 0 ? -magnitude : magnitude;
    }

    static long valueOf(final BigDecimal value, final int decimals, final DecimalContext context) {
        if (value.signum() == 0) {
            return 0;
        }
        BigDecimal decimal = value;
        final int integerDigits = decimal.precision() - decimal.scale();
        if (integerDigits > 19) {
            // at least 10^19, above Long.MAX_VALUE, which also avoids scaling a huge exponent
            return NAN;
        }
        if (integerDigits <= -decimals - 1) {
            // below a tenth of a unit, which rounds like a hundredth of a unit with the same sign
            // in every mode, but avoids scaling a huge scale such as 1E-1000000
            decimal = BigDecimal.valueOf(decimal.signum(), decimals + 2);
        }
        final BigDecimal scaled;
        try {
            scaled = decimal.setScale(decimals, context.mode.toRoundingMode());
        } catch (final ArithmeticException e) {
            return NAN; // rounding necessary with UNNECESSARY
        }
        final BigInteger units = scaled.unscaledValue();
        // Long.MIN_VALUE fits but is NaN, which is also out of range
        return units.bitLength() < Long.SIZE ? units.longValue() : NAN;
    }

    /**
     * Parses [+-]digits[.digits] without allocating, rounding the digits beyond the scale. Strings
     * with an exponent or non-ASCII characters are parsed by BigDecimal, which accepts the same
     * plain strings, so the result equals the BigDecimal value rounded to decimals, or NaN. The
     * characters are read one at a time: copying them to bytes for SWAR costs more than it saves.
     */
    static long valueOf(final CharSequence value, final int decimals, final DecimalContext context) {
        final int length = value.length();
        int position = 0;
        boolean negative = false;
        if (length > 0 && (value.charAt(0) == '-' || value.charAt(0) == '+')) {
            negative = value.charAt(0) == '-';
            position = 1;
        }
        final Digits digits = new Digits(decimals);
        while (position < length) {
            final char c = value.charAt(position++);
            if (!digits.accept(c)) {
                return c == 'e' || c == 'E' || c >= 0x80 ? parseBigDecimal(value.toString(), decimals, context) : NAN;
            }
        }
        return digits.value(negative, context);
    }

    /**
     * Parses ASCII [+-]digits[.digits] like valueOf(CharSequence...), taking at most eight integer
     * digits and the decimals up to the scale eight at a time.
     */
    static long valueOf(final byte[] bytes, final int offset, final int length, final int decimals,
                        final DecimalContext context) {
        final int end = offset + length;
        int position = offset;
        boolean negative = false;
        if (length > 0 && (bytes[offset] == '-' || bytes[offset] == '+')) {
            negative = bytes[offset] == '-';
            ++position;
        }
        final long plain = parsePlain(bytes, position, end, decimals);
        if (plain != NAN) {
            return negative ? -plain : plain;
        }
        final Digits digits = new Digits(decimals);
        while (position < end) {
            final int c = bytes[position++] & 0xFF;
            if (!digits.accept(c)) {
                return c == 'e' || c == 'E' ?
                    parseBigDecimal(new String(bytes, offset, length, StandardCharsets.ISO_8859_1), decimals, context) :
                    NAN;
            }
        }
        return digits.value(negative, context);
    }

    /**
     * Parses at most eight integer digits, optionally followed by a point and at most the scale of
     * decimals, with two independent eight byte loads.
     * @return non-negative value, or NAN to use the general parser
     */
    private static long parsePlain(final byte[] bytes, final int position, final int end, final int decimals) {
        final long integer = Swar.loadPadded(bytes, position);
        final int integerDigits = Math.min(Swar.digitCount(integer), end - position);
        final int point = position + integerDigits;
        if (point == end) {
            return integerDigits == 0 ? NAN : Swar.parseDigits(integer, integerDigits) * POWERS10[decimals];
        }
        if (bytes[point] != '.') {
            return NAN;
        }
        final int fraction = point + 1;
        final long word = Swar.loadPadded(bytes, fraction);
        final int fractionDigits = Math.min(Swar.digitCount(word), end - fraction);
        if (fraction + fractionDigits != end || fractionDigits > decimals || integerDigits + fractionDigits == 0) {
            return NAN;
        }
        // below 10^17, the two products are independent
        return Swar.parseDigits(integer, integerDigits) * POWERS10[decimals] +
            Swar.parseDigits(word, fractionDigits) * POWERS10[decimals - fractionDigits];
    }

    private static long parseBigDecimal(final String value, final int decimals, final DecimalContext context) {
        try {
            return valueOf(new BigDecimal(value), decimals, context);
        } catch (final NumberFormatException e) {
            return NAN;
        }
    }

    /**
     * Accumulates the digits and point of a plain decimal, one character at a time. Allocated per
     * parse and not escaping, so the JIT keeps its fields in registers.
     */
    private static final class Digits {
        private final int decimals;
        private long units;               // the digits up to the scale
        private int fractionDigits = -1;  // -1 before the point
        private boolean hasDigits;
        private int dropped;              // first digit beyond the scale
        private boolean sticky;           // whether any later digit is non-zero
        private boolean overflow;

        Digits(final int decimals) {
            this.decimals = decimals;
        }

        /**
         * Takes a digit or the point.
         * @return false for any other character
         */
        boolean accept(final int c) {
            final int digit = c - '0';
            if (digit >= 0 && digit <= 9) {
                hasDigits = true;
                if (fractionDigits < decimals) {
                    if (units >= APPEND_LIMIT && (units > APPEND_LIMIT || digit > Long.MAX_VALUE % 10)) {
                        overflow = true; // NaN unless an exponent follows
                    }
                    units = units * 10 + digit;
                    if (fractionDigits >= 0) {
                        ++fractionDigits;
                    }
                } else {
                    if (fractionDigits == decimals) {
                        dropped = digit;
                    } else {
                        sticky |= digit != 0;
                    }
                    ++fractionDigits;
                }
                return true;
            }
            if (c == '.' && fractionDigits < 0) {
                fractionDigits = 0;
                return true;
            }
            return false;
        }

        /**
         * Returns the value of the digits in the scale, rounded.
         * @return value or NaN
         */
        long value(final boolean negative, final DecimalContext context) {
            if (!hasDigits || overflow) {
                return NAN;
            }
            final long magnitude;
            if (fractionDigits < decimals) {
                magnitude = scaleUp(0, units, decimals - Math.max(fractionDigits, 0));
            } else {
                // the dropped digits as a fraction of 20: 2 * dropped plus one when any later digit
                // is non-zero, which is exactly half only for a dropped 5 followed by zeros
                magnitude = round(units, 2L * dropped + (sticky ? 1 : 0), 20, negative, context.mode);
            }
            return magnitude < 0 ? NAN : negative ? -magnitude : magnitude;
        }
    }

    static int toInt(final long value) {
        // NaN, Long.MIN_VALUE, is also out of range
        return value >= -Integer.MAX_VALUE && value <= Integer.MAX_VALUE ? (int) value : INT_NAN;
    }

    static long fromInt(final int value) {
        return value == INT_NAN ? NAN : value;
    }

    static BigDecimal toBigDecimal(final long value, final int decimals) {
        if (value == NAN) {
            throw new ArithmeticException("NaN cannot be converted to BigDecimal");
        }
        return BigDecimal.valueOf(value, decimals);
    }

    static String toString(final long value, final int decimals) {
        return value == NAN ? "NaN" : Decimal64Flyweight.toString(value, decimals);
    }

    /**
     * Writes a value as ASCII like toString, without allocating.
     * @return number of bytes written, at most STRING_LENGTH_MAX
     */
    static int toBytes(final long value, final int decimals, final byte[] bytes, final int offset) {
        if (value == NAN) {
            Objects.checkFromIndexSize(offset, 3, bytes.length);
            bytes[offset] = 'N';
            bytes[offset + 1] = 'a';
            bytes[offset + 2] = 'N';
            return 3;
        }
        final long magnitude = Math.abs(value);
        final long integer = PowersOf10.divide(magnitude, decimals);
        final int integerDigits = Decimal64Flyweight.digitsBase10(integer);
        final int sign = value < 0 ? 1 : 0;
        final int length = sign + integerDigits + (decimals > 0 ? decimals + 1 : 0);
        Objects.checkFromIndexSize(offset, length, bytes.length);
        int end = offset + length;
        if (decimals > 0) {
            writeDigits(bytes, end, magnitude - integer * POWERS10[decimals], decimals);
            end -= decimals + 1;
            bytes[end] = '.';
        }
        writeDigits(bytes, end, integer, integerDigits);
        if (sign != 0) {
            bytes[offset] = '-';
        }
        return length;
    }

    /**
     * Writes the last count digits of a non-negative value, zero padded, before end, eight at a time.
     */
    private static void writeDigits(final byte[] bytes, final int end, final long value, final int count) {
        long remaining = value;
        int position = end;
        int digits = count;
        while (digits >= Long.BYTES) {
            final long upper = PowersOf10.divide(remaining, 8);
            position -= Long.BYTES;
            Swar.store(bytes, position, Swar.formatDigits((int) (remaining - upper * 100_000_000L)));
            remaining = upper;
            digits -= Long.BYTES;
        }
        if (digits > 0) {
            // the last digits of eight, the last digit in the highest byte
            final long word = Swar.formatDigits((int) (remaining - PowersOf10.divide(remaining, 8) * 100_000_000L));
            for (int i = 1; i <= digits; ++i) {
                bytes[position - i] = (byte) (word >>> (Long.SIZE - Byte.SIZE * i));
            }
        }
    }

    private static int numberOfBits(final long value) {
        return Long.SIZE - Long.numberOfLeadingZeros(value);
    }

    /**
     * Returns a 128-bit non-negative value times 10^exponent, or OVERFLOW unless it fits in 63 bits.
     */
    private static long scaleUp(final long high, final long low, final int exponent) {
        if (high != 0 || low < 0) {
            return OVERFLOW;
        }
        final long scale = POWERS10[exponent];
        final long scaledHigh = Math.multiplyHigh(low, scale);
        final long scaled = low * scale;
        return scaledHigh != 0 || scaled < 0 ? OVERFLOW : scaled;
    }

    /**
     * Returns a non-negative value divided by 10^exponent, rounded.
     */
    private static long roundedDivideByPowerOf10(final long value, final int exponent, final boolean negative,
                                                 final DecimalRounding mode) {
        final long quotient = PowersOf10.divide(value, exponent);
        final long divisor = POWERS10[exponent];
        return round(quotient, value - quotient * divisor, divisor, negative, mode);
    }

    /**
     * Returns a non-negative 128-bit value (below 2^126) divided by 10^exponent, rounded, or OVERFLOW.
     */
    private static long roundedDivideByPowerOf10(final long high, final long low, final int exponent,
                                                 final boolean negative, final DecimalContext context) {
        final long divisor = POWERS10[exponent];
        if (exponent > DECIMALS_MAX) {
            return roundedDivide(high, low, divisor, negative, context);
        }
        if (high >= divisor) {
            return OVERFLOW; // quotient of at least 2^64
        }
        // divide in 32-bit digits: high is below the divisor, which is below 2^30, so every partial
        // dividend is below 2^62 and every partial quotient below 2^32
        final long upper = (high << Integer.SIZE) | (low >>> Integer.SIZE);
        final long upperQuotient = PowersOf10.divide(upper, exponent);
        final long lower = ((upper - upperQuotient * divisor) << Integer.SIZE) | (low & INT_MASK);
        final long lowerQuotient = PowersOf10.divide(lower, exponent);
        final long quotient = (upperQuotient << Integer.SIZE) | lowerQuotient;
        if (quotient < 0) {
            return OVERFLOW;
        }
        return round(quotient, lower - lowerQuotient * divisor, divisor, negative, context.mode);
    }

    /**
     * Returns a non-negative 128-bit value (below 2^126) divided by a positive divisor, rounded, or OVERFLOW.
     */
    private static long roundedDivide(final long high, final long low, final long divisor, final boolean negative,
                                      final DecimalContext context) {
        if (high == 0 && low >= 0) {
            final long quotient = low / divisor;
            return round(quotient, low - quotient * divisor, divisor, negative, context.mode);
        }
        if (high >= divisor) {
            return OVERFLOW; // quotient of at least 2^64
        }
        final long quotient = MutableUnsigned128.divide(high, low, divisor);
        if (quotient < 0) {
            return OVERFLOW;
        }
        // the remainder is below 2^64, so the low 64 bits of low - quotient * divisor are exact
        return round(quotient, low - quotient * divisor, divisor, negative, context.mode);
    }

    /**
     * Returns a non-negative value divided by a positive divisor times 10^exponent, rounded.
     */
    private static long roundedDivideByScaledDivisor(final long value, final long divisor, final int exponent,
                                                     final boolean negative, final DecimalRounding mode) {
        final long scale = POWERS10[exponent];
        final long high = Math.multiplyHigh(divisor, scale);
        final long low = divisor * scale;
        if (high == 0 && low >= 0) {
            final long quotient = value / low;
            return round(quotient, value - quotient * low, low, negative, mode);
        }
        // A scaled divisor of at least 2^63 exceeds the value, so the quotient is 0 and the
        // remainder the value. A divisor of 2^64 or more is replaced by 2^64 - 1 (unsigned), which
        // compares the same with a remainder below 2^63.
        return round(0, value, high == 0 ? low : -1L, negative, mode);
    }

    /**
     * Returns a truncated non-negative quotient rounded according to the rounding mode.
     * @param quotient  non-negative truncated quotient
     * @param remainder remainder, below the divisor
     * @param divisor   unsigned divisor
     * @param negative  whether the signed result is negative
     * @param mode      rounding mode
     * @return non-negative rounded quotient, or a negative value for overflow or an inexact
     * result with UNNECESSARY
     */
    private static long round(final long quotient, final long remainder, final long divisor,
                              final boolean negative, final DecimalRounding mode) {
        // Compare remainder / divisor with 0.5 without overflowing 2 * remainder
        final long half = divisor - remainder;
        if (mode == DecimalRounding.HALF_UP) {
            // common case kept small enough to inline into the arithmetic
            return Long.compareUnsigned(remainder, half) >= 0 ? quotient + 1 : quotient;
        }
        if (remainder == 0) {
            return quotient;
        }
        if (mode == DecimalRounding.UNNECESSARY) {
            return OVERFLOW;
        }
        final boolean increment = switch (mode) {
            case UP -> true;
            case DOWN, UNNECESSARY -> false;
            case CEILING -> !negative;
            case FLOOR -> negative;
            case HALF_UP -> Long.compareUnsigned(remainder, half) >= 0;
            case HALF_DOWN -> Long.compareUnsigned(remainder, half) > 0;
            case HALF_EVEN -> Long.compareUnsigned(remainder, half) > 0 || (remainder == half && (quotient & 1) != 0);
        };
        return increment ? quotient + 1 : quotient;
    }
}
