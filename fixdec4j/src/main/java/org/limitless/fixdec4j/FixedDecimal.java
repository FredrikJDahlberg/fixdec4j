package org.limitless.fixdec4j;

import java.math.BigDecimal;

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
 * Held in a static final field, the record is a constant to the JIT; otherwise the divisions by
 * powers of ten still compile to multiplications through a switch of constant divisors.
 * @param decimals number of decimals, 0 to DECIMALS_MAX
 * @param <S> phantom type of the values
 * @author fredrikdahlberg
 */
public record FixedDecimal<S>(int decimals) {
    public static final int DECIMALS_MAX = FixedFlyweight.DECIMALS_MAX;
    public static final long NAN = FixedFlyweight.NAN;
    public static final long MAX_VALUE = Long.MAX_VALUE;
    public static final long MIN_VALUE = -Long.MAX_VALUE;

    public FixedDecimal {
        if (decimals < 0 || decimals > DECIMALS_MAX) {
            throw new IllegalArgumentException("decimals must be 0 to " + DECIMALS_MAX + ": " + decimals);
        }
    }

    /**
     * Returns a scale with a number of decimals.
     * @param decimals number of decimals, 0 to DECIMALS_MAX
     * @param <S> phantom type of the values
     * @return scale
     */
    public static <S> FixedDecimal<S> of(final int decimals) {
        return new FixedDecimal<>(decimals);
    }

    public static boolean isNaN(final long value) {
        return value == NAN;
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
     * Returns a value parsed from a string, rounded if it has more decimals than the scale.
     * @param value   decimal string, e.g. "-101.25"
     * @param context rounding mode
     * @return value, or NAN indicating overflow or an invalid string
     */
    public long valueOf(final String value, final DecimalContext context) {
        return FixedFlyweight.valueOf(value, decimals, context);
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

    public long add(final long value, final long term) {
        return FixedFlyweight.add(value, term);
    }

    public long subtract(final long value, final long term) {
        return FixedFlyweight.subtract(value, term);
    }

    public long negate(final long value) {
        return -value; // NaN is Long.MIN_VALUE, which negates to itself
    }

    public long abs(final long value) {
        return Math.abs(value); // NaN is Long.MIN_VALUE, whose absolute value is itself
    }

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
