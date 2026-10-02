package org.limitless.fixdec4j;

import java.util.Objects;

/**
 * Rounding mode for the operations that round (multiply, divide, round and conversions), shared
 * by the Decimal64 and fixed-scale types. There is one immutable instance per rounding mode,
 * shared by all threads, e.g. {@code DecimalContext.HALF_UP} or {@code DecimalContext.of(mode)}.
 * @author fredrikdahlberg
 */
public final class DecimalContext {
    /** Round away from zero. */
    public static final DecimalContext UP = new DecimalContext(DecimalRounding.UP);
    /** Round towards zero (truncate). */
    public static final DecimalContext DOWN = new DecimalContext(DecimalRounding.DOWN);
    /** Round towards positive infinity. */
    public static final DecimalContext CEILING = new DecimalContext(DecimalRounding.CEILING);
    /** Round towards negative infinity. */
    public static final DecimalContext FLOOR = new DecimalContext(DecimalRounding.FLOOR);
    /** Round to nearest, ties away from zero. */
    public static final DecimalContext HALF_UP = new DecimalContext(DecimalRounding.HALF_UP);
    /** Round to nearest, ties towards zero. */
    public static final DecimalContext HALF_DOWN = new DecimalContext(DecimalRounding.HALF_DOWN);
    /** Round to nearest, ties to the even neighbour (banker's rounding). */
    public static final DecimalContext HALF_EVEN = new DecimalContext(DecimalRounding.HALF_EVEN);
    /** Exact result required, an inexact result is NaN. */
    public static final DecimalContext UNNECESSARY = new DecimalContext(DecimalRounding.UNNECESSARY);

    // indexed by the ordinal of the rounding mode
    private static final DecimalContext[] CONTEXTS = {
        UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN, UNNECESSARY
    };

    final DecimalRounding mode;

    private DecimalContext(final DecimalRounding mode) {
        this.mode = mode;
    }

    /**
     * Returns the shared context of a rounding mode, e.g. a mode read from configuration.
     * @param mode rounding mode
     * @return shared context
     * @throws NullPointerException if mode is null
     */
    public static DecimalContext of(final DecimalRounding mode) {
        return CONTEXTS[Objects.requireNonNull(mode, "mode").ordinal()];
    }

    /**
     * Returns the rounding mode of this context.
     * @return rounding mode
     */
    public DecimalRounding roundingMode() {
        return mode;
    }

    /**
     * Returns a string representation, e.g. "DecimalContext[roundingMode=HALF_UP]".
     * @return string representation
     */
    @Override
    public String toString() {
        return "DecimalContext[roundingMode=" + mode + "]";
    }
}
