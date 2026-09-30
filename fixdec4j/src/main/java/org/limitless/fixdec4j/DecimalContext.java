package org.limitless.fixdec4j;

import java.util.Objects;

/**
 * Rounding mode and scratch space for the operations that round (multiply, divide, round and
 * conversions), shared by the Decimal64 and fixed-scale types so that the operations do not
 * allocate. A context is not thread safe, allocate one per thread and reuse it.
 * @author fredrikdahlberg
 */
public final class DecimalContext {
    final DecimalRounding mode;
    // remainder of a 128-bit by 64-bit division
    final MutableUnsigned128 remainder = new MutableUnsigned128();

    /**
     * Constructs a context with a rounding mode.
     * @param mode rounding mode
     */
    public DecimalContext(final DecimalRounding mode) {
        this.mode = Objects.requireNonNull(mode, "mode");
    }

    public DecimalRounding roundingMode() {
        return mode;
    }
}
