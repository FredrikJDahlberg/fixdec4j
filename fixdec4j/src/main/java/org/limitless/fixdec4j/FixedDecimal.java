package org.limitless.fixdec4j;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A fixed number of decimals and the arithmetic on values with that scale. A value is a long
 * holding the value times 10^decimals, e.g. 101.25 with 2 decimals is 10125, and Long.MIN_VALUE
 * is NaN. The type parameter is a phantom type naming what the values represent, e.g.
 * {@code FixedDecimal<Price>}, which MutableFixed64 uses to reject mixing at compile time.
 * <pre>
 * static final FixedDecimal&lt;Price&gt; PRICE = FixedDecimal.of(8);
 * static final FixedDecimal&lt;Qty&gt; QTY = FixedDecimal.of(2);
 * static final FixedDecimal&lt;Notional&gt; NOTIONAL = FixedDecimal.of(2);
 *
 * long total = PRICE.add(price1, price2);
 * long notional = NOTIONAL.multiply(price, PRICE, quantity, QTY, context);
 * </pre>
 * Operations that combine scales return a value in the scale of the receiver. The raw long values
 * carry no scale, so passing a value of another scale to a same-scale operation is not detected.
 * <p>
 * The divisions by powers of ten multiply by a precomputed reciprocal, so a scale read from
 * configuration is as fast as one held in a static final field.
 * @param decimals number of decimals, 0 to DECIMALS_MAX
 * @param <S> phantom type of the values
 * @author fredrikdahlberg
 */
public record FixedDecimal<S>(int decimals) {
    /** Largest number of decimals of a scale. */
    public static final int DECIMALS_MAX = FixedFlyweight.DECIMALS_MAX;
    /** Not a number, Long.MIN_VALUE, the result of an overflow, an invalid string or a division by zero. */
    public static final long NAN = FixedFlyweight.NAN;
    /** Largest raw value in any scale. */
    public static final long MAX_VALUE = Long.MAX_VALUE;
    /** Smallest raw value in any scale. */
    public static final long MIN_VALUE = -Long.MAX_VALUE;
    /** NaN of a value stored in 32 bits, see toInt. */
    public static final int INT_NAN = FixedFlyweight.INT_NAN;
    /** Largest number of bytes written by toBytes: a sign, 19 digits and the point. */
    public static final int STRING_LENGTH_MAX = FixedFlyweight.STRING_LENGTH_MAX;

    // one shared instance per number of decimals, the type parameter only exists at compile time
    private static final FixedDecimal<?>[] SCALES = new FixedDecimal<?>[DECIMALS_MAX + 1];

    static {
        for (int i = 0; i <= DECIMALS_MAX; ++i) {
            SCALES[i] = new FixedDecimal<>(i);
        }
    }

    /**
     * Constructs a scale, prefer the shared instance returned by of.
     * @param decimals number of decimals, 0 to DECIMALS_MAX
     * @throws IllegalArgumentException if decimals is out of range
     */
    public FixedDecimal {
        if (decimals < 0 || decimals > DECIMALS_MAX) {
            throw new IllegalArgumentException("decimals must be 0 to " + DECIMALS_MAX + ": " + decimals);
        }
    }

    /**
     * Returns a scale with a number of decimals, a shared instance.
     * @param decimals number of decimals, 0 to DECIMALS_MAX
     * @param <S> phantom type of the values
     * @return scale
     * @throws IllegalArgumentException if decimals is out of range
     */
    @SuppressWarnings("unchecked")
    public static <S> FixedDecimal<S> of(final int decimals) {
        if (decimals < 0 || decimals > DECIMALS_MAX) {
            throw new IllegalArgumentException("decimals must be 0 to " + DECIMALS_MAX + ": " + decimals);
        }
        return (FixedDecimal<S>) SCALES[decimals];
    }

    /**
     * Returns whether a value is NaN.
     * @param value value
     * @return true when NaN
     */
    public static boolean isNaN(final long value) {
        return value == NAN;
    }

    /**
     * Returns whether a value stored in 32 bits is NaN.
     * @param value value stored in 32 bits
     * @return true when NaN
     */
    public static boolean isNaN(final int value) {
        return value == INT_NAN;
    }

    /**
     * Narrows a value to 32 bits for compact storage, e.g. in an int[] or a message with 4-byte
     * prices. The number of decimals is unchanged, so the int holds the same raw value, from
     * -Integer.MAX_VALUE to Integer.MAX_VALUE units, e.g. -21474836.47 to 21474836.47 with
     * 2 decimals. Calculate with the 64-bit value and narrow the result.
     * @param value value
     * @return value in 32 bits, or INT_NAN when NaN or out of range
     */
    public static int toInt(final long value) {
        return FixedFlyweight.toInt(value);
    }

    /**
     * Widens a value stored in 32 bits by toInt, mapping INT_NAN to NAN.
     * @param value value stored in 32 bits
     * @return value
     */
    public static long fromInt(final int value) {
        return FixedFlyweight.fromInt(value);
    }

    /**
     * Returns a value from a mantissa and an exponent, e.g. 10125 and -2 for 101.25, rounded if it
     * has more decimals than the scale.
     * @param mantissa mantissa
     * @param exponent exponent, -18 to 0
     * @param context  rounding mode
     * @return value or NAN indicating overflow
     */
    public long valueOf(final long mantissa, final int exponent, final DecimalContext context) {
        if (exponent < -18 || exponent > 0) {
            return NAN;
        }
        return FixedFlyweight.rescale(mantissa, -exponent, decimals, context);
    }

    /**
     * Returns a value parsed from a string, rounded if it has more decimals than the scale. A plain
     * decimal is parsed without allocating; exponent notation, e.g. "1.5E3", is also accepted.
     * @param value   decimal string, e.g. "-101.25", or any CharSequence such as a view of a buffer
     * @param context rounding mode
     * @return value, or NAN indicating overflow or an invalid string
     */
    public long valueOf(final CharSequence value, final DecimalContext context) {
        return FixedFlyweight.valueOf(value, decimals, context);
    }

    /**
     * Returns a value parsed from ASCII bytes, e.g. a field of a FIX message, rounded if it has more
     * decimals than the scale. A plain decimal is parsed eight digits at a time without allocating;
     * exponent notation is also accepted.
     * @param bytes   buffer
     * @param offset  index of the first byte
     * @param length  number of bytes
     * @param context rounding mode
     * @return value, or NAN indicating overflow or an invalid string
     * @throws IndexOutOfBoundsException if the range is outside the buffer
     */
    public long valueOf(final byte[] bytes, final int offset, final int length, final DecimalContext context) {
        Objects.checkFromIndexSize(offset, length, bytes.length);
        return FixedFlyweight.valueOf(bytes, offset, length, decimals, context);
    }

    /**
     * Returns a value from a BigDecimal, rounded if it has more decimals than the scale.
     * @param value   BigDecimal
     * @param context rounding mode
     * @return value or NAN indicating overflow
     */
    public long valueOf(final BigDecimal value, final DecimalContext context) {
        return FixedFlyweight.valueOf(value, decimals, context);
    }

    /**
     * Returns a value of another scale converted to this scale, rounded if decimals are removed.
     * @param value   value
     * @param scale   scale of the value
     * @param context rounding mode
     * @return value in this scale or NAN indicating overflow
     */
    public long convert(final long value, final FixedDecimal<?> scale, final DecimalContext context) {
        return FixedFlyweight.rescale(value, scale.decimals, decimals, context);
    }

    /**
     * Returns the sum of two values in this scale, exact.
     * @param value value in this scale
     * @param term  value in this scale
     * @return sum or NAN indicating overflow
     */
    public long add(final long value, final long term) {
        return FixedFlyweight.add(value, term);
    }

    /**
     * Returns the difference of two values in this scale, exact.
     * @param value value in this scale
     * @param term  value in this scale
     * @return difference or NAN indicating overflow
     */
    public long subtract(final long value, final long term) {
        return FixedFlyweight.subtract(value, term);
    }

    /**
     * Returns the negated value.
     * @param value value in this scale
     * @return negated value, NAN when NaN
     */
    public long minus(final long value) {
        return -value; // NaN is Long.MIN_VALUE, which negates to itself
    }

    /**
     * Returns the absolute value.
     * @param value value in this scale
     * @return absolute value, NAN when NaN
     */
    public long abs(final long value) {
        return Math.abs(value); // NaN is Long.MIN_VALUE, whose absolute value is itself
    }

    /**
     * Compares two values in this scale for order. NaN orders below every other value.
     * @param value1 value in this scale
     * @param value2 value in this scale
     * @return negative, zero or positive as value1 is less than, equal to or greater than value2
     */
    public int compare(final long value1, final long value2) {
        return Long.compare(value1, value2);
    }

    /**
     * Returns the product of two values in this scale, rounded.
     * @param value   value in this scale
     * @param factor  value in this scale
     * @param context rounding mode
     * @return product or NAN indicating overflow
     */
    public long multiply(final long value, final long factor, final DecimalContext context) {
        return FixedFlyweight.multiply(value, decimals, factor, decimals, decimals, context);
    }

    /**
     * Returns the product of two values of any scales in this scale, rounded.
     * @param value       value
     * @param valueScale  scale of the value
     * @param factor      factor
     * @param factorScale scale of the factor
     * @param context     rounding mode
     * @return product in this scale or NAN indicating overflow
     */
    public long multiply(final long value, final FixedDecimal<?> valueScale, final long factor,
                         final FixedDecimal<?> factorScale, final DecimalContext context) {
        return FixedFlyweight.multiply(value, valueScale.decimals, factor, factorScale.decimals, decimals, context);
    }

    /**
     * Returns the quotient of two values in this scale, rounded.
     * @param dividend value in this scale
     * @param divisor  value in this scale
     * @param context  rounding mode
     * @return quotient or NAN indicating overflow or division by zero
     */
    public long divide(final long dividend, final long divisor, final DecimalContext context) {
        return FixedFlyweight.divide(dividend, decimals, divisor, decimals, decimals, context);
    }

    /**
     * Returns the quotient of two values of any scales in this scale, rounded.
     * @param dividend      dividend
     * @param dividendScale scale of the dividend
     * @param divisor       divisor
     * @param divisorScale  scale of the divisor
     * @param context       rounding mode
     * @return quotient in this scale or NAN indicating overflow or division by zero
     */
    public long divide(final long dividend, final FixedDecimal<?> dividendScale, final long divisor,
                       final FixedDecimal<?> divisorScale, final DecimalContext context) {
        return FixedFlyweight.divide(dividend, dividendScale.decimals, divisor, divisorScale.decimals, decimals,
            context);
    }

    /**
     * Returns a value times an integer, e.g. a price times a number of lots. The product is exact,
     * so no rounding mode is needed.
     * @param value   value in this scale
     * @param integer integer factor
     * @return product in this scale or NAN indicating overflow
     */
    public long multiplyByInteger(final long value, final long integer) {
        return FixedFlyweight.multiplyByInteger(value, integer);
    }

    /**
     * Returns a value divided by an integer, rounded, e.g. a notional split into a number of parts.
     * @param value   value in this scale
     * @param integer integer divisor
     * @param context rounding mode
     * @return quotient in this scale or NAN indicating division by zero (or by Long.MIN_VALUE)
     */
    public long divideByInteger(final long value, final long integer, final DecimalContext context) {
        return FixedFlyweight.divide(value, decimals, integer, 0, decimals, context);
    }

    /**
     * Returns a value rounded to fewer decimals, still in this scale, e.g. 101.2567 with 4 decimals
     * rounded to 2 places is 101.2600. Negative places round to tens, hundreds and so on.
     * @param value   value in this scale
     * @param places  number of decimals to keep, at least decimals() - 18; the value is returned
     *                unchanged when places is at least decimals()
     * @param context rounding mode
     * @return rounded value or NAN indicating overflow or places out of range
     */
    public long round(final long value, final int places, final DecimalContext context) {
        return FixedFlyweight.round(value, decimals, places, context);
    }

    /**
     * Returns a value rounded to a multiple of an increment in this scale, e.g. a price rounded to
     * a tick size of 0.05.
     * @param value     value in this scale
     * @param increment positive increment in this scale
     * @param context   rounding mode
     * @return rounded value or NAN indicating overflow or an increment that is not positive
     */
    public long roundToIncrement(final long value, final long increment, final DecimalContext context) {
        return FixedFlyweight.roundToIncrement(value, increment, context);
    }

    /**
     * Returns the largest integer not above a value, in this scale, e.g. -101.25 floors to -102.00.
     * @param value value in this scale
     * @return integer value in this scale
     */
    public long floor(final long value) {
        return FixedFlyweight.floor(value, decimals);
    }

    /**
     * Returns the smallest integer not below a value, in this scale, e.g. 101.25 ceils to 102.00.
     * @param value value in this scale
     * @return integer value in this scale or NAN indicating overflow
     */
    public long ceil(final long value) {
        return FixedFlyweight.ceil(value, decimals);
    }

    /**
     * Returns the remainder of dividing two values in this scale, exact, with the sign of the
     * dividend like BigDecimal.remainder, e.g. 10.25 % 3 is 1.25.
     * @param dividend value in this scale
     * @param divisor  value in this scale
     * @return remainder or NAN indicating division by zero
     */
    public long remainder(final long dividend, final long divisor) {
        return FixedFlyweight.remainder(dividend, divisor);
    }

    /**
     * Writes a value as ASCII like toString, e.g. a field of a FIX message, without allocating.
     * @param value  value
     * @param bytes  buffer
     * @param offset index of the first byte
     * @return number of bytes written, at most STRING_LENGTH_MAX
     * @throws IndexOutOfBoundsException if the string does not fit in the buffer
     */
    public int toBytes(final long value, final byte[] bytes, final int offset) {
        return FixedFlyweight.toBytes(value, decimals, bytes, offset);
    }

    /**
     * Returns the exact value as a BigDecimal with the decimals of this scale.
     * @param value value
     * @return BigDecimal
     * @throws ArithmeticException if the value is NaN
     */
    public BigDecimal toBigDecimal(final long value) {
        return FixedFlyweight.toBigDecimal(value, decimals);
    }

    /**
     * Returns a value as a string with the decimals of this scale, e.g. "101.25", or "NaN".
     * @param value value
     * @return string
     */
    public String toString(final long value) {
        return FixedFlyweight.toString(value, decimals);
    }
}
