package org.limitless.fixdec4j;

import java.math.RoundingMode;

/**
 * Rounding modes for Decimal64Flyweight, Decimal64 and MutableDecimal64. The names and semantics
 * match {@link java.math.RoundingMode}; the examples show rounding to zero decimals.
 * @author fredrikdahlberg
 */
public enum DecimalRounding {
    UP,          // Round away from zero, e.g. 5.1 = 6, -5.1 = -6
    DOWN,        // Round towards zero (truncate), e.g. 5.9 = 5, -5.9 = -5
    CEILING,     // Round towards positive infinity, e.g. 5.1 = 6, -5.9 = -5
    FLOOR,       // Round towards negative infinity, e.g. 5.9 = 5, -5.1 = -6
    HALF_UP,     // Round to nearest, ties away from zero, e.g. 5.5 = 6, -5.5 = -6
    HALF_DOWN,   // Round to nearest, ties towards zero, e.g. 5.5 = 5, -5.5 = -5
    HALF_EVEN,   // Round to nearest, ties to the even neighbour (banker's rounding), e.g. 5.5 = 6, 6.5 = 6
    UNNECESSARY; // Exact result required, an inexact result is NaN

    /**
     * Returns the corresponding java.math rounding mode.
     * @return rounding mode
     */
    public RoundingMode toRoundingMode() {
        return switch (this) {
            case UP -> RoundingMode.UP;
            case DOWN -> RoundingMode.DOWN;
            case CEILING -> RoundingMode.CEILING;
            case FLOOR -> RoundingMode.FLOOR;
            case HALF_UP -> RoundingMode.HALF_UP;
            case HALF_DOWN -> RoundingMode.HALF_DOWN;
            case HALF_EVEN -> RoundingMode.HALF_EVEN;
            case UNNECESSARY -> RoundingMode.UNNECESSARY;
        };
    }

    /**
     * Returns the decimal rounding mode corresponding to a java.math rounding mode.
     * @param mode java.math rounding mode
     * @return decimal rounding mode
     */
    public static DecimalRounding valueOf(final RoundingMode mode) {
        return switch (mode) {
            case UP -> UP;
            case DOWN -> DOWN;
            case CEILING -> CEILING;
            case FLOOR -> FLOOR;
            case HALF_UP -> HALF_UP;
            case HALF_DOWN -> HALF_DOWN;
            case HALF_EVEN -> HALF_EVEN;
            case UNNECESSARY -> UNNECESSARY;
        };
    }
}
