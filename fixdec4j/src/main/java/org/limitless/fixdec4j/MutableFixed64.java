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
 * The type parameter is erased at runtime, so raw types or unchecked casts bypass the check;
 * running with assertions enabled also checks the scales of same-kind operands.
 * @param <S> phantom type of the value
 * @author fredrikdahlberg
 */
public final class MutableFixed64<S> implements Comparable<MutableFixed64<S>> {
    private final FixedDecimal<S> scale;
    private long value;

    /**
     * Constructs a zero value.
     * @param scale scale
     */
    public MutableFixed64(final FixedDecimal<S> scale) {
        this.scale = scale;
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

    public static <S> MutableFixed64<S> valueOf(final FixedDecimal<S> scale, final CharSequence value,
                                                final DecimalContext context) {
        return fromRaw(scale, scale.valueOf(value, context));
    }

    public static <S> MutableFixed64<S> valueOf(final FixedDecimal<S> scale, final BigDecimal value,
                                                final DecimalContext context) {
        return fromRaw(scale, scale.valueOf(value, context));
    }

    public FixedDecimal<S> scale() {
        return scale;
    }

    /**
     * Returns the raw representation, the value times 10^decimals.
     * @return raw value
     */
    public long raw() {
        return value;
    }

    public MutableFixed64<S> setRaw(final long raw) {
        value = raw;
        return this;
    }

    public MutableFixed64<S> set(final MutableFixed64<S> other) {
        assert sameScale(other);
        value = other.value;
        return this;
    }

    public boolean isNaN() {
        return value == FixedDecimal.NAN;
    }

    public MutableFixed64<S> add(final MutableFixed64<S> term) {
        assert sameScale(term);
        value = FixedFlyweight.add(value, term.value);
        return this;
    }

    public MutableFixed64<S> subtract(final MutableFixed64<S> term) {
        assert sameScale(term);
        value = FixedFlyweight.subtract(value, term.value);
        return this;
    }

    public MutableFixed64<S> negate() {
        value = -value;
        return this;
    }

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
        this.value = FixedFlyweight.multiply(value.value, value.scale.decimals(), factor.value,
            factor.scale.decimals(), scale.decimals(), context);
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
        value = FixedFlyweight.divide(dividend.value, dividend.scale.decimals(), divisor.value,
            divisor.scale.decimals(), scale.decimals(), context);
        return this;
    }

    /**
     * Sets this instance to a value of any kind converted to its scale, rounded if decimals are removed.
     * @param other   value
     * @param context rounding mode
     * @return this instance
     */
    public MutableFixed64<S> convert(final MutableFixed64<?> other, final DecimalContext context) {
        value = FixedFlyweight.rescale(other.value, other.scale.decimals(), scale.decimals(), context);
        return this;
    }

    public BigDecimal toBigDecimal() {
        return scale.toBigDecimal(value);
    }

    @Override
    public int compareTo(final MutableFixed64<S> other) {
        assert sameScale(other);
        return Long.compare(value, other.value);
    }

    @Override
    public boolean equals(final Object object) {
        return object instanceof MutableFixed64<?> other && value == other.value && scale.equals(other.scale);
    }

    @Override
    public int hashCode() {
        return Long.hashCode(value) * 31 + scale.decimals();
    }

    @Override
    public String toString() {
        return scale.toString(value);
    }

    private boolean sameScale(final MutableFixed64<?> other) {
        return scale.decimals() == other.scale.decimals();
    }
}
