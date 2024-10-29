package org.limitless.fixdec4j;

/**
 * Rounding modes for Decimal, MutableDecimal and MutableUnsigned128
 * @author Fredah
 */
public enum DecimalRounding {
    UP,   // Rounding mode to round away from zero, e.g. 5.5 = 6, -5.5 = -6
    DOWN  // Rounding mode to round towards zero, e.g. 5.5 = 5, -5.5 = -5
}
