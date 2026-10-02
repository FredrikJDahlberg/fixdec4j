package org.limitless.fixdec4j;

import java.math.BigDecimal;

/**
 * A mutable fixed-scale decimal whose type parameter names what it represents, so that values of
 * different kinds cannot be mixed by accident:
 * <pre>
 * MutableFixed64&lt;Price&gt; price = MutableFixed64.valueOf(PRICE, "101.25", context);
 * MutableFixed64&lt;Qty&gt; quantity = MutableFixed64.valueOf(QTY, "300", context);
 *
 * price.add(quantity);                                  // does not compile
 * notional.multiply(price, quantity, context);          // explicit, in the scale of notional
 * </pre>
 * Same-kind operations take the same type parameter; operations combining kinds write the
 * result in the scale of this instance. The operations update this instance and do not allocate.
 * The type parameter is erased at runtime, and two scales of one kind may differ in decimals, so
 * same-kind operations also check that the scales match and throw IllegalArgumentException if not.
 * @param <S> phantom type of the value
 * @author fredrikdahlberg
 */
public final class MutableFixed64<S> implements Comparable<MutableFixed64<S>> {
    // the number of decimals rather than the FixedDecimal, which saves a dependent load per operand
    private final int decimals;
    private long value;

    /**
     * Constructs a zero value.
     * @param scale scale
     */
    public MutableFixed64(final FixedDecimal<S> scale) {
        decimals = scale.decimals();
    }

    /**
     * Constructs a value from its raw representation, the value times 10^decimals.
     * @param scale scale
     * @param raw   raw value
     * @param <S>   phantom type of the value
     * @return new instance
     */
    public static <S> MutableFixed64<S> fromRaw(final FixedDecimal<S> scale, final long raw) {
        return new MutableFixed64<>(scale).setRaw(raw);
    }

    /**
     * Returns a value parsed from a string, rounded if it has more decimals than the scale, see
     * FixedDecimal.valueOf(CharSequence, DecimalContext).
     * @param scale   scale
     * @param value   decimal string, e.g. "-101.25"
     * @param context rounding mode
     * @param <S>     phantom type of the value
     * @return new instance, NaN indicating overflow or an invalid string
     */
    public static <S> MutableFixed64<S> valueOf(final FixedDecimal<S> scale, final CharSequence value,
                                                final DecimalContext context) {
        return fromRaw(scale, scale.valueOf(value, context));
    }

    /**
     * Returns a value from a BigDecimal, rounded if it has more decimals than the scale.
     * @param scale   scale
     * @param value   BigDecimal
     * @param context rounding mode
     * @param <S>     phantom type of the value
     * @return new instance, NaN indicating overflow or an inexact result with UNNECESSARY
     */
    public static <S> MutableFixed64<S> valueOf(final FixedDecimal<S> scale, final BigDecimal value,
                                                final DecimalContext context) {
        return fromRaw(scale, scale.valueOf(value, context));
    }

    /**
     * Returns the scale of this instance.
     * @return scale
     */
    public FixedDecimal<S> scale() {
        return FixedDecimal.of(decimals);
    }

    /**
     * Returns the raw representation, the value times 10^decimals.
     * @return raw value
     */
    public long raw() {
        return value;
    }

    /**
     * Sets the raw representation, the value times 10^decimals in the scale of this instance.
     * @param raw raw value
     * @return this instance
     */
    public MutableFixed64<S> setRaw(final long raw) {
        value = raw;
        return this;
    }

    /**
     * Sets this instance to the value of another of the same kind.
     * @param other value of the same kind
     * @return this instance
     * @throws IllegalArgumentException if the scales differ in decimals
     */
    public MutableFixed64<S> set(final MutableFixed64<S> other) {
        checkScale(other);
        value = other.value;
        return this;
    }

    /**
     * Returns whether this instance is NaN.
     * @return true when NaN
     */
    public boolean isNaN() {
        return value == FixedDecimal.NAN;
    }

    /**
     * Adds a value of the same kind to this instance, exact.
     * @param term value of the same kind
     * @return this instance, NaN on overflow
     * @throws IllegalArgumentException if the scales differ in decimals
     */
    public MutableFixed64<S> add(final MutableFixed64<S> term) {
        checkScale(term);
        value = FixedFlyweight.add(value, term.value);
        return this;
    }

    /**
     * Subtracts a value of the same kind from this instance, exact.
     * @param term value of the same kind
     * @return this instance, NaN on overflow
     * @throws IllegalArgumentException if the scales differ in decimals
     */
    public MutableFixed64<S> subtract(final MutableFixed64<S> term) {
        checkScale(term);
        value = FixedFlyweight.subtract(value, term.value);
        return this;
    }

    /**
     * Negates this instance, NaN stays NaN.
     * @return this instance
     */
    public MutableFixed64<S> minus() {
        value = -value;
        return this;
    }

    /**
     * Sets this instance to its absolute value, NaN stays NaN.
     * @return this instance
     */
    public MutableFixed64<S> abs() {
        value = Math.abs(value);
        return this;
    }

    /**
     * Sets this instance to the product of two values of any kinds, rounded to its scale.
     * @param value   value
     * @param factor  factor
     * @param context rounding mode
     * @return this instance
     */
    public MutableFixed64<S> multiply(final MutableFixed64<?> value, final MutableFixed64<?> factor,
                                      final DecimalContext context) {
        this.value = FixedFlyweight.multiply(value.value, value.decimals, factor.value, factor.decimals, decimals,
            context);
        return this;
    }

    /**
     * Sets this instance to the quotient of two values of any kinds, rounded to its scale.
     * @param dividend dividend
     * @param divisor  divisor
     * @param context  rounding mode
     * @return this instance
     */
    public MutableFixed64<S> divide(final MutableFixed64<?> dividend, final MutableFixed64<?> divisor,
                                    final DecimalContext context) {
        value = FixedFlyweight.divide(dividend.value, dividend.decimals, divisor.value, divisor.decimals, decimals,
            context);
        return this;
    }

    /**
     * Sets this instance to a value of any kind converted to its scale, rounded if decimals are removed.
     * @param other   value
     * @param context rounding mode
     * @return this instance
     */
    public MutableFixed64<S> convert(final MutableFixed64<?> other, final DecimalContext context) {
        value = FixedFlyweight.rescale(other.value, other.decimals, decimals, context);
        return this;
    }

    /**
     * Multiplies this instance by an integer, exactly.
     * @param integer integer factor
     * @return this instance, NaN on overflow
     */
    public MutableFixed64<S> multiplyByInteger(final long integer) {
        value = FixedFlyweight.multiplyByInteger(value, integer);
        return this;
    }

    /**
     * Divides this instance by an integer, rounded.
     * @param integer integer divisor
     * @param context rounding mode
     * @return this instance, NaN on division by zero
     */
    public MutableFixed64<S> divideByInteger(final long integer, final DecimalContext context) {
        value = FixedFlyweight.divide(value, decimals, integer, 0, decimals, context);
        return this;
    }

    /**
     * Rounds this instance to fewer decimals, keeping its scale, see FixedDecimal.round.
     * @param places  number of decimals to keep
     * @param context rounding mode
     * @return this instance
     */
    public MutableFixed64<S> round(final int places, final DecimalContext context) {
        value = FixedFlyweight.round(value, decimals, places, context);
        return this;
    }

    /**
     * Rounds this instance to a multiple of an increment of the same kind, e.g. a tick size.
     * @param increment positive increment
     * @param context   rounding mode
     * @return this instance, NaN when the increment is not positive
     * @throws IllegalArgumentException if the scales differ
     */
    public MutableFixed64<S> roundToIncrement(final MutableFixed64<S> increment, final DecimalContext context) {
        checkScale(increment);
        value = FixedFlyweight.roundToIncrement(value, increment.value, context);
        return this;
    }

    /**
     * Sets this instance to the largest integer not above it.
     * @return this instance
     */
    public MutableFixed64<S> floor() {
        value = FixedFlyweight.floor(value, decimals);
        return this;
    }

    /**
     * Sets this instance to the smallest integer not below it.
     * @return this instance, NaN on overflow
     */
    public MutableFixed64<S> ceil() {
        value = FixedFlyweight.ceil(value, decimals);
        return this;
    }

    /**
     * Sets this instance to the remainder of dividing it by a value of the same kind, exact, with
     * the sign of this instance.
     * @param divisor divisor
     * @return this instance, NaN on division by zero
     * @throws IllegalArgumentException if the scales differ
     */
    public MutableFixed64<S> remainder(final MutableFixed64<S> divisor) {
        checkScale(divisor);
        value = FixedFlyweight.remainder(value, divisor.value);
        return this;
    }

    /**
     * Writes this instance as ASCII like toString, without allocating.
     * @param bytes  buffer
     * @param offset index of the first byte
     * @return number of bytes written, at most FixedDecimal.STRING_LENGTH_MAX
     * @throws IndexOutOfBoundsException if the string does not fit in the buffer
     */
    public int toBytes(final byte[] bytes, final int offset) {
        return FixedFlyweight.toBytes(value, decimals, bytes, offset);
    }

    /**
     * Returns the value as a BigDecimal with the number of decimals of the scale.
     * @return BigDecimal
     * @throws ArithmeticException if this instance is NaN
     */
    public BigDecimal toBigDecimal() {
        return FixedFlyweight.toBigDecimal(value, decimals);
    }

    /**
     * Compares this instance with another of the same kind for order. NaN orders below every
     * other value.
     * @param other value of the same kind
     * @return negative, zero or positive as this instance is less than, equal to or greater than other
     * @throws IllegalArgumentException if the scales differ in decimals
     */
    @Override
    public int compareTo(final MutableFixed64<S> other) {
        checkScale(other);
        return Long.compare(value, other.value);
    }

    /**
     * Indicates whether another object is a MutableFixed64 with the same raw value and number of
     * decimals, e.g. 1.0 with one decimal does not equal 1.00 with two. NaN equals NaN.
     * @param object other object
     * @return true when equal
     */
    @Override
    public boolean equals(final Object object) {
        return object instanceof MutableFixed64<?> other && value == other.value && decimals == other.decimals;
    }

    /**
     * Returns a hash code consistent with equals.
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Long.hashCode(value) * 31 + decimals;
    }

    /**
     * Returns the value with the number of decimals of the scale, e.g. "101.25000000", or "NaN".
     * @return string representation
     */
    @Override
    public String toString() {
        return FixedFlyweight.toString(value, decimals);
    }

    private void checkScale(final MutableFixed64<?> other) {
        if (decimals != other.decimals) {
            throw new IllegalArgumentException("scales differ: " + decimals + " and " + other.decimals + " decimals");
        }
    }
}
