package org.limitless.fixdec4j;

/**
 * This class provides 128-bit unsigned arithmetics with mutable semantics.
 * <a href="https://www.codeproject.com/Tips/784635/UInt-Bit-Operations">Unsigned integer 128 bit operations</a>
 * @author fredrikdahlberg
 */
public final class MutableUnsigned128 implements Comparable<MutableUnsigned128> {
    private static final long INT_BITS = 0xffffffffL;
    private static final long LIMIT = 1L << Integer.SIZE;

    public static final MutableUnsigned128 MIN_VALUE = new MutableUnsigned128(0L, 0L);
    public static final MutableUnsigned128 MAX_VALUE = new MutableUnsigned128(-1L, -1L);

    private static final float FLOAT_POWER64 = 1.8446744073709551616e19F; // 2^64
    private static final double DOUBLE_POWER64 = 1.8446744073709551616e19D; // 2^64

    private long highBits;
    private long lowBits;

    //private final ThreadLocal<MutableUnsigned128.Context> threadLocal = ThreadLocal.withInitial(MutableUnsigned128.Context::new);

    /**
     * Constructs an empty instance
     */
    public MutableUnsigned128() {
    }

    /**
     * Constructs an object from a long value.
     * @param value 4-bit unsigned value
     */
    public MutableUnsigned128(final long value) {
        highBits = 0;
        lowBits = value;
    }

    /**
     * Constructs an object from two 64-bit unsigned values
     * @param highBits the high unsigned 64-bits
     * @param lowBits  the low unsigned 64-bits
     */
    public MutableUnsigned128(final long highBits, final long lowBits) {
        this.highBits = highBits;
        this.lowBits = lowBits;
    }

    /**
     * Constructs an object from another instance
     * @param value 128-bit unsigned value
     */
    public MutableUnsigned128(final MutableUnsigned128 value) {
        set(value);
    }

    public MutableUnsigned128 set(final MutableUnsigned128 value) {
        highBits = value.highBits;
        lowBits = value.lowBits;
        return this;
    }

    public MutableUnsigned128 set(final long value) {
        highBits = 0;
        lowBits = value;
        return this;
    }

    /**
     * Returns a string representation of the object.
     * @return string representation
     */
    @Override
    public String toString() {
        return String.format("{<UnsignedLong128>, 0x%016x %016x, high=%d, low=%d }",
            highBits, lowBits, highBits, lowBits);
    }

    /**
     * Returns a hash code value for the object
     * @return hash code
     */
    @Override
    public int hashCode() {
        return Long.hashCode(highBits) * 31 + Long.hashCode(lowBits);
    }

    /**
     * Indicates whether some other object is equal to this one.
     * @param object other object
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }

        final MutableUnsigned128 other = (MutableUnsigned128) object;

        return highBits == other.highBits && lowBits == other.lowBits;
    }

    /**
     * Compares this object with the specified object for order.
     * @param value other object
     * @return less than (-1), equals (0) or greater than (1) the other object
     */
    @Override
    public int compareTo(final MutableUnsigned128 value) {
        final int highBits = Unsigned64Flyweight.compare(this.highBits, value.highBits);
        if (highBits != 0) {
            return highBits;
        } else {
            return Unsigned64Flyweight.compare(lowBits, value.lowBits);
        }
    }

    /**
     * Returns the highest 64 bits.
     * @return the highest 64 bits.
     */
    public long highBits() {
        return highBits;
    }

    /**
     * Returns the lowest 64 bits.
     * @return lowest 64 bits.
     */
    public long lowBits() {
        return lowBits;
    }

    /**
     * Returns whether the instance is equal to zero.
     * @return true when equal to zero
     */
    public boolean isZero() {
        return (highBits | lowBits) == 0;
    }

    /**
     * Returns the value of the specified number as a byte, which may involve rounding or truncation.
     * @return byte value
     */
    public byte byteValue() {
        return (byte) lowBits;
    }

    /**
     * Returns the value of the specified number as a short, which may involve rounding or truncation.
     * @return short value
     */
    public short shortValue() {
        return (short) lowBits;
    }

    /**
     * Returns the value of the specified number as an integer, which may involve rounding or truncation.
     * @return integer value
     */
    public int intValue() {
        return (int) lowBits;
    }

    /**
     * Returns the value of the specified number as an integer, which may involve rounding or truncation.
     * @return long value
     */
    public long longValue() {
        return lowBits;
    }

    /**
     * Returns the value of the specified number as a float, which may involve rounding.
     * @return float value
     */
    public float floatValue() {
        return Math.abs(highBits * FLOAT_POWER64 + lowBits);
    }

    /**
     * Returns the value of the specified number as a double, which may involve rounding.
     * @return double value
     */
    public double doubleValue() {
        return Math.abs(highBits * DOUBLE_POWER64 + lowBits);
    }

    /**
     * Returns true when the instance fits an 64-bit singed long.
     * @return true when the instance fits an 64-bits signed long.
     */
    public boolean fitsLong() {
        return highBits == 0;
    }

    /**
     * Add 128-bit unsigned value to this object
     * @param term 128-bit unsigned value
     * @return this instance with sum
     * From <a href="https://www.codeproject.com/Tips/617214/UInt-Addition-Subtraction"
     * >www.codeproject.com</a>
     */
    public MutableUnsigned128 add(final MutableUnsigned128 term) {
        final long carry = (((lowBits & term.lowBits) & 1) + (lowBits >>> 1) + (term.lowBits >>> 1)) >>> 63;
        highBits += term.highBits + carry;
        lowBits += term.lowBits;
        return this;
    }

    public MutableUnsigned128 add(final long value, MutableUnsigned128.Context context) {
        return add(context.term.set(value));
    }

    /**
     * Subtract 64-bit unsigned value from this object
     * @param term 64-bit unsigned value
     * @return this instance with difference
     * From <a href="https://www.codeproject.com/Tips/617214/UInt-Addition-Subtraction"
     * >www.codeproject.com</a>
     */
    public MutableUnsigned128 subtract(final long term) {
        lowBits -= term;
        final long carry = (((lowBits & term) & 1) + (term >>> 1) + (lowBits >>> 1)) >>> 63;
        highBits -= carry;
        return this;
    }

    /**
     * Subtract 128-bit unsigned value from this object
     * @param term 128-bit unsigned object
     * @return this instance with difference
     * From <a href="https://www.codeproject.com/Tips/617214/UInt-Addition-Subtraction"
     * >www.codeproject.com</a>
     */
    public MutableUnsigned128 subtract(final MutableUnsigned128 term) {
        final long low = lowBits;
        final long high = highBits;
        lowBits = low - term.lowBits;
        final long carry = (((lowBits & term.lowBits) & 1) + (term.lowBits >>> 1) + (lowBits >>> 1)) >>> 63;
        highBits = high - (term.highBits + carry);
        return this;
    }

    /**
     * Multiply this object with 128-bit unsigned factor
     * @param factor 128-bit unsigned value
     * @return this instance with product
     * From <a href="https://www.codeproject.com/Tips/618570/UInt-Multiplication-Squaring"
     * >www.codeproject.com</a>
     */
    public MutableUnsigned128 multiply(MutableUnsigned128 factor) {
        final long highBits = this.highBits;
        final long lowBits = this.lowBits;
        multiply(this.lowBits, factor.lowBits, this);
        this.highBits += highBits * factor.lowBits;
        this.highBits += lowBits * factor.highBits;
        return this;
    }

    public MutableUnsigned128 multiply(long factor, MutableUnsigned128.Context context) {
        return multiply(context.factor.set(factor));
    }

    /**
     * Increase value by one
     * @return this instance with result
     * From <a href="https://www.codeproject.com/Tips/617214/UInt-Addition-Subtraction"
     * >www.codeproject.com</a>
     */
    public MutableUnsigned128 increment() {
        final long value = lowBits + 1;
        highBits += ((lowBits ^ value) & lowBits) >>> 63;
        lowBits = value;
        return this;
    }

    /**
     * Decrease value by one
     * @return this instance with result
     * From <a href="https://www.codeproject.com/Tips/617214/UInt-Addition-Subtraction"
     * >www.codeproject.com</a>
     */
    public MutableUnsigned128 decrement() {
        final long value = lowBits - 1;
        highBits -= ((value ^ lowBits) & value) >>> 63;
        lowBits = value;
        return this;
    }

    /**
     * Bit-wise not
     * @return this instance with result
     * From <a href="https://www.codeproject.com/Tips/784635/UInt-Bit-Operations"
     * >www.codeproject.com</a>
     */
    public MutableUnsigned128 not() {
        highBits = ~highBits;
        lowBits = ~lowBits;
        return this;
    }

    /**
     * This instance bit-wise or with value
     * @param value 128-bit unsigned integer
     * @return this instance with result
     * From <a href="https://www.codeproject.com/Tips/784635/UInt-Bit-Operations"
     * >www.codeproject.com</a>
     */
    public MutableUnsigned128 or(final MutableUnsigned128 value) {
        highBits |= value.highBits;
        lowBits |= value.lowBits;
        return this;
    }

    /**
     * This instance bit-wise and with value
     * @param value unsigned 128-bit integer
     * @return this instance with result
     * From <a href="https://www.codeproject.com/Tips/784635/UInt-Bit-Operations"
     * >www.codeproject.com</a>
     */
    public MutableUnsigned128 and(final MutableUnsigned128 value) {
        highBits &= value.highBits;
        lowBits &= value.lowBits;
        return this;
    }

    /**
     * Number of leading zero bits
     * @return count
     */
    public int numberOfLeadingZeros() {
        return (highBits == 0) ? Long.numberOfLeadingZeros(lowBits) + 64
            : Long.numberOfLeadingZeros(highBits);
    }

    /**
     * Shift value left
     * @param count number of steps
     * @return this instance with result
     * From <a href="https://www.codeproject.com/Tips/784635/UInt-Bit-Operations"
     * >www.codeproject.com</a>
     */
    public MutableUnsigned128 shiftLeft(final int count) {
        long bits = count & 127;
        final long M1 = ((((bits + 127) | bits) & 64) >>> 6) - 1L;
        final long M2 = (bits >>> 6) - 1L;
        final long high = highBits;
        final long low = lowBits;

        bits &= 63;
        highBits = (low << bits) & (~M2);
        lowBits = (low << bits) & M2;
        highBits |= ((high << bits) | ((low >>> (64 - bits)) & M1)) & M2;
        return this;
    }

    /**
     * Shift value right
     * @param count number of steps
     * @return this instance with result
     * From <a href="https://www.codeproject.com/Tips/784635/UInt-Bit-Operations"
     * >www.codeproject.com</a>
     */
    public MutableUnsigned128 shiftRight(final int count) {
        long bits = count & 127;
        final long M1 = ((((bits + 127) | bits) & 64) >>> 6) - 1L;
        final long M2 = (bits >>> 6) - 1L;
        final long high = highBits;
        final long low = lowBits;
        bits &= 63;
        lowBits = (high >>> bits) & (~M2);
        highBits = (high >>> bits) & M2;
        lowBits |= ((low >>> bits) | ((high << (64 - bits)) & M1)) & M2;
        return this;
    }

    /**
     * Multiplies two 64-bit factors and produces a 128-bit product
     * @param value1 64-bit unsigned factor
     * @param value2 64-bit unsigned factor
     * @param result 128-bit unsigned product
     *                 From <a href="https://www.codeproject.com/Tips/618570/UInt-Multiplication-Squaring"
     *                 >www.codeproject.com</a>
     */
    private static void multiply(final long value1, final long value2, final MutableUnsigned128 result) {
        int powerFactor1 = Long.SIZE - Long.numberOfLeadingZeros(value1);
        int powerFactor2 = Long.SIZE - Long.numberOfLeadingZeros(value2);
        if (powerFactor1 + powerFactor2 < Long.SIZE) {
            result.highBits = 0;
            result.lowBits = value1 * value2;
        } else {
            long factor1 = value1;
            long factor2 = value2;
            final long u1 = factor1 & INT_BITS;
            final long v1 = factor2 & INT_BITS;
            long t = u1 * v1;
            long w3 = t & INT_BITS;
            long k = t >>> Integer.SIZE;
            factor1 >>>= Integer.SIZE;
            t = (factor1 * v1) + k;
            k = t & INT_BITS;

            final long w1 = t >>> Integer.SIZE;
            factor2 >>>= Integer.SIZE;
            t = (u1 * factor2) + k;
            k = t >>> Integer.SIZE;
            result.highBits = (factor1 * factor2) + w1 + k;
            result.lowBits = (t << Integer.SIZE) + w3;
        }
    }

    public MutableUnsigned128 divide(final long divisor, final MutableUnsigned128.Context context) {
        return divide(context.divisor.set(divisor), context.remainder1, context);
    }

    public MutableUnsigned128 divide(final MutableUnsigned128 divisor, final MutableUnsigned128.Context context) {
        return divide(divisor, context.remainder1, context);
    }

    /**
     * Divides this 128-bit unsigned value with divisor
     * @param divisor   divisor 128-bit unsigned
     * @param inRemainder remainder of the division 128-bit unsigned
     * @return this instance updated with the 128-bit quotient
     * From <a href="http://www.codeproject.com/Tips/785014/UInt-Division-Modulus"
     * >www.codeproject.com</a>
     */
    public MutableUnsigned128 divide(final MutableUnsigned128 divisor,
                                     final MutableUnsigned128 inRemainder,
                                     final MutableUnsigned128.Context context) {
        final MutableUnsigned128 remainder = context.remainder2; //new MutableUnsigned128();
        if ((highBits | divisor.highBits) == 0) {
            final long lowBits = this.lowBits;
            highBits = 0;
            this.lowBits = Unsigned64Flyweight.divide(lowBits, divisor.lowBits);
            inRemainder.highBits = 0;
            inRemainder.lowBits = Unsigned64Flyweight.remainder(lowBits, divisor.lowBits);
        } else if (divisor.highBits == 0) {
            final long quotientLow;
            long quotientHigh = 0;
            if (Unsigned64Flyweight.compare(highBits, divisor.lowBits) <= -1) {
                // remainder contains quotient and reminder
                quotientLow = divide(highBits, lowBits, divisor.lowBits, remainder);
            } else {
                quotientHigh = Unsigned64Flyweight.divide(highBits, divisor.lowBits);
                final long remainderHigh = Unsigned64Flyweight.remainder(highBits, divisor.lowBits);
                quotientLow = divide(remainderHigh, lowBits, divisor.lowBits, remainder);
            }
            highBits = quotientHigh;
            lowBits = quotientLow;
            inRemainder.highBits = 0;
            inRemainder.lowBits = remainder.lowBits;
        } else {
            final MutableUnsigned128 v1 = context.v1;
            final MutableUnsigned128 u1 = context.u1;
            final MutableUnsigned128 q1 = context.q1;
            final int zeros = Long.numberOfLeadingZeros(divisor.highBits);
            v1.set(divisor).shiftLeft(zeros);
            u1.set(this).shiftRight(1);
            q1.lowBits = divide(u1.highBits, u1.lowBits, v1.highBits, remainder);
            q1.highBits = 0;
            q1.shiftRight(63 - zeros);
            if ((q1.highBits | q1.lowBits) != 0) {
                q1.decrement();
            }

            final MutableUnsigned128 quotient = context.quotient;
            quotient.set(q1);
            q1.multiply(divisor);
            inRemainder.highBits = highBits;
            inRemainder.lowBits = lowBits;
            inRemainder.subtract(q1);
            if (inRemainder.compareTo(divisor) >= 0) {
                quotient.increment();
                inRemainder.subtract(divisor);
            }
            highBits = quotient.highBits;
            lowBits = quotient.lowBits;
        }
        return this;
    }

    /**
     * Iterative division of 128-bit dividend by 64-bit divisor.
     * @param dividendHigh 64 higher bits
     * @param dividendLow  64 lower bits
     * @param divisor      64-bit
     * @param result       returns the quotient and remainder as high bits and low bits respectively
     * @return 64-bit quotient
     * From <a href="http://www.codeproject.com/Tips/785014/UInt-Division-Modulus"
     * >www.codeproject.com</a>
     */
    private static long divide(final long dividendHigh,
                               final long dividendLow,
                               final long divisor,
                               final MutableUnsigned128 result) {
        final int zeros = Long.numberOfLeadingZeros(divisor);
        long v = divisor << zeros;
        final long vn1 = v >>> Integer.SIZE;
        final long vn0 = v & INT_BITS;
        final long un32;
        final long un10;
        if (zeros > 0) {
            un32 = (dividendHigh << zeros) | (dividendLow >>> (Long.SIZE - zeros));
            un10 = dividendLow << zeros;
        } else {
            un32 = dividendHigh;
            un10 = dividendLow;
        }

        final long un1 = un10 >>> Integer.SIZE;
        final long un0 = un10 & INT_BITS;
        long q1 = Unsigned64Flyweight.divide(un32, vn1);
        long rhat = Unsigned64Flyweight.remainder(un32, vn1);
        long left = q1 * vn0;
        long right = (rhat << Integer.SIZE) + un1;
        while (Unsigned64Flyweight.compare(q1, LIMIT) >= 0 || Unsigned64Flyweight.compare(left, right) >= 1) {
            --q1;
            rhat += vn1;
            if (Unsigned64Flyweight.compare(rhat, LIMIT) < 0) {
                left -= vn0;
                right = (rhat << Integer.SIZE) | un1;
            } else {
                break;
            }
        }

        final long un21 = (un32 << Integer.SIZE) + (un1 - (q1 * v));
        long q0 = Unsigned64Flyweight.divide(un21, vn1);
        rhat = Unsigned64Flyweight.remainder(un21, vn1);
        left = q0 * vn0;
        right = (rhat << Integer.SIZE) | un0;
        while (Unsigned64Flyweight.compare(q0, LIMIT) >= 0 || Unsigned64Flyweight.compare(left, right) >= 1) {
            --q0;
            rhat += vn1;
            if (Unsigned64Flyweight.compare(rhat, LIMIT) < 0) {
                left -= vn0;
                right = (rhat << Integer.SIZE) | un0;
            } else {
                break;
            }
        }

        final long quotient = (q1 << Integer.SIZE) | q0;
        final long remainder = ((un21 << Integer.SIZE) + (un0 - (q0 * v))) >>> zeros;
        result.highBits = quotient;
        result.lowBits = remainder;
        return quotient;
    }

    public static class Context {
        final MutableUnsigned128 term = new MutableUnsigned128();
        final MutableUnsigned128 factor = new MutableUnsigned128();
        final MutableUnsigned128 divisor = new MutableUnsigned128();
        final MutableUnsigned128 remainder1 = new MutableUnsigned128();
        final MutableUnsigned128 remainder2 = new MutableUnsigned128();
        final MutableUnsigned128 product = new MutableUnsigned128();
        final MutableUnsigned128 scale = new MutableUnsigned128();
        final MutableUnsigned128 rounding = new MutableUnsigned128();
        final MutableUnsigned128 quotient = new MutableUnsigned128();
        final MutableUnsigned128 v1 = new MutableUnsigned128();
        final MutableUnsigned128 u1 = new MutableUnsigned128();
        final MutableUnsigned128 q1 = new MutableUnsigned128();

        final DecimalRounding mode;

        Context() {
            this(DecimalRounding.UP);
        }

        public Context(DecimalRounding mode) {
            this.mode = mode;
        }

        public DecimalRounding roundingMode() {
            return mode;
        }

        public static Context build(DecimalRounding mode) {
            return new Context(mode);
        }
    }
}
