package org.limitless.fixdec4j;

/**
 * This class provides 128-bit unsigned arithmetics with mutable semantics. It is an internal
 * building block for the decimal types and not part of the public API.
 * <a href="https://www.codeproject.com/Tips/784635/UInt-Bit-Operations">Unsigned integer 128 bit operations</a>
 * @author fredrikdahlberg
 */
final class MutableUnsigned128 implements Comparable<MutableUnsigned128> {
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
     */
    private static void multiply(final long value1, final long value2, final MutableUnsigned128 result) {
        result.highBits = Math.unsignedMultiplyHigh(value1, value2);
        result.lowBits = value1 * value2;
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
                quotientLow = divide(highBits, lowBits, divisor.lowBits);
            } else {
                quotientHigh = Unsigned64Flyweight.divide(highBits, divisor.lowBits);
                final long remainderHigh = Unsigned64Flyweight.remainder(highBits, divisor.lowBits);
                quotientLow = divide(remainderHigh, lowBits, divisor.lowBits);
            }
            inRemainder.highBits = 0;
            // the remainder is below the divisor, so the low 64 bits of the difference are exact
            inRemainder.lowBits = lowBits - quotientLow * divisor.lowBits;
            highBits = quotientHigh;
            lowBits = quotientLow;
        } else {
            final MutableUnsigned128 v1 = context.v1;
            final MutableUnsigned128 u1 = context.u1;
            final MutableUnsigned128 q1 = context.q1;
            final int zeros = Long.numberOfLeadingZeros(divisor.highBits);
            v1.set(divisor).shiftLeft(zeros);
            u1.set(this).shiftRight(1);
            q1.lowBits = divide(u1.highBits, u1.lowBits, v1.highBits);
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
     * Iterative division of 128-bit dividend by 64-bit divisor. The quotient must fit in 64 bits,
     * i.e. dividendHigh is below the divisor (unsigned).
     * @param dividendHigh 64 higher bits
     * @param dividendLow  64 lower bits
     * @param divisor      64-bit
     * @return 64-bit quotient; the remainder is dividendLow - quotient * divisor (mod 2^64)
     * From <a href="http://www.codeproject.com/Tips/785014/UInt-Division-Modulus"
     * >www.codeproject.com</a>
     */
    static long divide(final long dividendHigh, final long dividendLow, final long divisor) {
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
        long rhat = un32 - q1 * vn1; // below vn1, so exact modulo 2^64
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
        rhat = un21 - q0 * vn1;
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

        return (q1 << Integer.SIZE) | q0;
    }

    static final class Context {
        final MutableUnsigned128 term = new MutableUnsigned128();
        final MutableUnsigned128 factor = new MutableUnsigned128();
        final MutableUnsigned128 divisor = new MutableUnsigned128();
        final MutableUnsigned128 remainder1 = new MutableUnsigned128();
        final MutableUnsigned128 quotient = new MutableUnsigned128();
        final MutableUnsigned128 v1 = new MutableUnsigned128();
        final MutableUnsigned128 u1 = new MutableUnsigned128();
        final MutableUnsigned128 q1 = new MutableUnsigned128();
    }
}
