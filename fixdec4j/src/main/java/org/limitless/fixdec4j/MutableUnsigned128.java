package org.limitless.fixdec4j;

/**
 * This class provides 128-bit unsigned arithmetics with mutable semantics.
 * https://www.codeproject.com/Tips/784635/UInt-Bit-Operations 
 * 
 * @author Fredah
 *
 */
public final class MutableUnsigned128 implements Comparable<MutableUnsigned128>
{
	private static final long INT_BITS = 0xffffffffL;
	private static final long LIMIT = 1L << Integer.SIZE;

	public static final MutableUnsigned128 MIN_VALUE = new MutableUnsigned128(0L, 0L);	
	public static final MutableUnsigned128 MAX_VALUE = new MutableUnsigned128(-1L, -1L);
	
	private static final float  FLOAT_POWER64 = 1.8446744073709551616e19F; // 2^64
	private static final double DOUBLE_POWER64 = 1.8446744073709551616e19D; // 2^64
	
	private long m_highBits;
	private long m_lowBits;

	/**
	 * Constructs an empty instance
	 */
	public MutableUnsigned128()
	{
	}

	/**
	 * Constructs an object from a long value.
	 * @param inUnsigned 4-bit unsigned value
	 */
	public MutableUnsigned128(final long inUnsigned)
	{
		m_highBits = 0;
		m_lowBits = inUnsigned;
	}

	/**
	 * Constructs an object from two 64-bit unsigned values
	 * @param highBits the high unsigned 64-bits 
	 * @param lowBits the low unsigned 64-bits
	 */
	public MutableUnsigned128(final long highBits, final long lowBits)
	{
		m_highBits = highBits;
		m_lowBits = lowBits;
	}
	
	/**
	 * Constructs an object from another instance 
	 * @param inValue 128-bit unsigned value
	 */
	public MutableUnsigned128(final MutableUnsigned128 inValue)
	{
		m_highBits = inValue.m_highBits;
		m_lowBits = inValue.m_lowBits;
	}

	// FIXME: value of

	/**
	 * Returns a string representation of the object.
	 * @return string representation
	 */
	@Override
	public String toString()
	{
		return String.format("{<UnsignedLong128>, 0x%016x %016x, high=%d, low=%d }", m_highBits,
				m_lowBits, m_highBits, m_lowBits);
	}

	/**
	 * Returns a hash code value for the object
	 * @return hash code
	 */
	@Override
	public int hashCode()
	{
		int result = 31;
		result += (int) (m_highBits ^ (m_highBits >>> 32));
		result *= 31;
		result += (int) (m_lowBits ^ (m_lowBits >>> 32));
		return result;
	}

	/**
	 * Indicates whether some other object is equal to this one.
	 * @param inObject other object
	 */
	@Override
	public boolean equals(Object inObject)
	{
		if (this == inObject)
		{
			return true;
		}
		if (inObject == null || getClass() != inObject.getClass())
		{
			return false;
		}

		final MutableUnsigned128 other = (MutableUnsigned128) inObject;

		return m_highBits == other.m_highBits && m_lowBits == other.m_lowBits;
	}

	/**
	 * Compares this object with the specified object for order.
	 * @param inValue other object
	 * @return less than (-1), equals (0) or greater than (1) the other object 
	 */
	@Override
	public int compareTo(final MutableUnsigned128 inValue)
	{
		final int highBits = Unsigned.compare(m_highBits, inValue.m_highBits); 
		if (highBits != 0)
		{
			return highBits;
		}
		else
		{
			return Unsigned.compare(m_lowBits, inValue.m_lowBits);
		}
	}
	
	/**
	 * Returns the highest 64 bits.
	 * @return the highest 64 bits.
	 */
	public long highBits()
	{
		return m_highBits;
	}
	
	/**
	 * Returns the lowest 64 bits.
	 * @return lowest 64 bits.
	 */
	public long lowBits()
	{
		return m_lowBits;
	}

	/**
	 * Returns whether the instance is equal to zero.
	 * @return true when equal to zero
	 */
	public boolean isZero()
	{
		return (m_highBits | m_lowBits) == 0;
	}
	
	/**
	 * Returns the value of the specified number as a byte, which may involve rounding or truncation.
	 * @return byte value
	 */
	public byte byteValue()
	{
		return (byte) m_lowBits;
	}

	/**
	 * Returns the value of the specified number as a short, which may involve rounding or truncation.
	 * @return short value
	 */
	public short shortValue()
	{
		return (short) m_lowBits;
	}
	
	/**
	 * Returns the value of the specified number as an integer, which may involve rounding or truncation.
	 * @return integer value
	 */
	public int intValue()
	{
		return (int) m_lowBits;
	}
	
	/**
	 * Returns the value of the specified number as an integer, which may involve rounding or truncation.
	 * @return long value
	 */
	public long longValue()
	{
		return m_lowBits;
	}

	/**
	 * Returns the value of the specified number as a float, which may involve rounding.
	 * @return float value
	 */
	public float floatValue()
	{
		return Math.abs(m_highBits * FLOAT_POWER64 + m_lowBits);
	}
	
	/**
	 * Returns the value of the specified number as a double, which may involve rounding.
	 * @return double value
	 */
	public double doubleValue()
	{
		return Math.abs(m_highBits * DOUBLE_POWER64 + m_lowBits);
	}
	
	/**
	 * Returns true when the instance fits an 64-bit singed long.
	 * @return true when the instance fits an 64-bits signed long.
	 */
	public boolean fitsLong()
	{
		return m_highBits == 0;
	}

	/**
	 * Add 64-bit unsigned long to this object
	 * @param inTerm 64-bit unsigned long
	 * @return this instance with sum
	 */
	public MutableUnsigned128 add(final long inTerm)
	{
		final MutableUnsigned128 sum = new MutableUnsigned128(inTerm).add(this);

		m_highBits = sum.m_highBits;
		m_lowBits = sum.m_lowBits;

		return this;
	}

	/**
	 * Add 128-bit unsigned value to this object
	 * @param inTerm 128-bit unsigned value
	 * @return this instance with sum
 	 * From <a href="https://www.codeproject.com/Tips/617214/UInt-Addition-Subtraction"
	 * >www.codeproject.com</a>
	 */
	public MutableUnsigned128 add(final MutableUnsigned128 inTerm)
	{
		final long carry = (((m_lowBits & inTerm.m_lowBits) & 1) + (m_lowBits >>> 1)
				+ (inTerm.m_lowBits >>> 1)) >>> 63;

		m_highBits += inTerm.m_highBits + carry;
		m_lowBits += inTerm.m_lowBits;

		return this;
	}

	/**
	 * Subtract 64-bit unsigned value from this object
	 * @param inTerm 64-bit unsigned value
	 * @return this instance with difference
 	 * From <a href="https://www.codeproject.com/Tips/617214/UInt-Addition-Subtraction"
	 * >www.codeproject.com</a>
	 */
	public MutableUnsigned128 subtract(final long inTerm)
	{
		m_lowBits -= inTerm;
		final long carry = (((m_lowBits & inTerm) & 1) + (inTerm >>> 1) + 
				(m_lowBits >>> 1)) >>> 63;
		m_highBits -= carry;

		return this;
	}

	/**
	 * Subtract 128-bit unsigned value from this object
	 * @param inTerm 128-bit unsigned object
	 * @return this instance with difference
	 * From <a href="https://www.codeproject.com/Tips/617214/UInt-Addition-Subtraction"
	 * >www.codeproject.com</a>
	 */
	public MutableUnsigned128 subtract(final MutableUnsigned128 inTerm)
	{
		final long low = m_lowBits;
		final long high = m_highBits;

		m_lowBits = low - inTerm.m_lowBits;
		final long carry = (((m_lowBits & inTerm.m_lowBits) & 1) + (inTerm.m_lowBits >>> 1)
				+ (m_lowBits >>> 1)) >>> 63;
		m_highBits = high - (inTerm.m_highBits + carry);

		return this;
	}

	/**
	 * Multiply this object with 64-bit unsigned value
	 * @param inFactor 64-bit unsigned value
	 * @return this instance with product
	 */
	public MutableUnsigned128 multiply(final long inFactor)
	{
		final MutableUnsigned128 product = new MutableUnsigned128(inFactor);

		product.multiply(this);
		m_highBits = product.m_highBits;
		m_lowBits = product.m_lowBits;

		return this;
	}

	/**
	 * Multiply this object with 128-bit unsigned factor
	 * @param inFactor 128-bit unsigned value
	 * @return this instance with product
	 * From <a href="https://www.codeproject.com/Tips/618570/UInt-Multiplication-Squaring"
	 * >www.codeproject.com</a>
	 */
	public MutableUnsigned128 multiply(MutableUnsigned128 inFactor)
	{
		final long highBits = m_highBits;
		final long lowBits = m_lowBits;

		multiply(m_lowBits, inFactor.m_lowBits, this);
		m_highBits += highBits * inFactor.m_lowBits;
		m_highBits += lowBits * inFactor.m_highBits;
		
		return this;
	}

	/**
	 * Divide this object with 64-bit unsigned divisor
	 * @param inDivisor 64-bit unsigned divisor
	 * @return this instance with quotient
	 */
	public MutableUnsigned128 divide(final long inDivisor)
	{
		return divide(new MutableUnsigned128(inDivisor));
	}

	/**
	 * Divide this object with 128-bit unsigned divisor
	 * @param inDivisor 128-bit unsinged divisor
	 * @return this instance with quotient
	 */
	public MutableUnsigned128 divide(final MutableUnsigned128 inDivisor)
	{
		return divide(inDivisor, new MutableUnsigned128());
	}

	/**
	 * Increase value by one
	 * @return this instance with result
 	 * From <a href="https://www.codeproject.com/Tips/617214/UInt-Addition-Subtraction"
	 * >www.codeproject.com</a>
	 */
	public MutableUnsigned128 increment()
	{
		final long value = m_lowBits + 1;

		m_highBits += ((m_lowBits ^ value) & m_lowBits) >>> 63;
		m_lowBits = value;

		return this;
	}

	/**
	 * Decrease value by one
	 * @return this instance with result
 	 * From <a href="https://www.codeproject.com/Tips/617214/UInt-Addition-Subtraction"
	 * >www.codeproject.com</a>
	 */
	public MutableUnsigned128 decrement()
	{
		final long value = m_lowBits - 1;

		m_highBits -= ((value ^ m_lowBits) & value) >>> 63;
		m_lowBits = value;

		return this;
	}

	/**
	 * Bit-wise not
	 * @return this instance with result
	 * From <a href="https://www.codeproject.com/Tips/784635/UInt-Bit-Operations"
	 * >www.codeproject.com</a>
	 */
	public MutableUnsigned128 not()
	{
		m_highBits = ~m_highBits;
		m_lowBits = ~m_lowBits;

		return this;
	}

	/**
	 * This instance bit-wise or with value
	 * @param inValue 128-bit unsigned integer
	 * @return this instance with result
	 * From <a href="https://www.codeproject.com/Tips/784635/UInt-Bit-Operations"
	 * >www.codeproject.com</a>
	 */
	public MutableUnsigned128 or(final MutableUnsigned128 inValue)
	{
		m_highBits |= inValue.m_highBits;
		m_lowBits |= inValue.m_lowBits;

		return this;
	}

	/**
	 * This instance bit-wise and with value
	 * @param inValue unsigned 128-bit integer
	 * @return this instance with result
	 * From <a href="https://www.codeproject.com/Tips/784635/UInt-Bit-Operations"
	 * >www.codeproject.com</a>
	 */
	public MutableUnsigned128 and(final MutableUnsigned128 inValue)
	{
		m_highBits &= inValue.m_highBits;
		m_lowBits &= inValue.m_lowBits;

		return this;
	}

	/**
	 * Number of leading zero bits
	 * @return count
	 */
	public int numberOfLeadingZeros()
	{
		return (m_highBits == 0) ? Long.numberOfLeadingZeros(m_lowBits) + 64
				: Long.numberOfLeadingZeros(m_highBits);
	}

	/**
	 * Shift value left
	 * @param inCount number of steps
	 * @return this instance with result
	 * From <a href="https://www.codeproject.com/Tips/784635/UInt-Bit-Operations"
	 * >www.codeproject.com</a>
	 */
	public MutableUnsigned128 shiftLeft(final int inCount)
	{
		long count = inCount & 127;
		final long M1 = ((((count + 127) | count) & 64) >>> 6) - 1L;
		final long M2 = (count >>> 6) - 1L;
		final long high = m_highBits;
		final long low = m_lowBits;

		count &= 63;
		m_highBits = (low << count) & (~M2);
		m_lowBits = (low << count) & M2;
		m_highBits |= ((high << count) | ((low >>> (64 - count)) & M1)) & M2;

		return this;
	}

	/**
	 * Shift value right
	 * @param inCount number of steps
	 * @return this instance with result
	 * From <a href="https://www.codeproject.com/Tips/784635/UInt-Bit-Operations"
	 * >www.codeproject.com</a>
	 */
	public MutableUnsigned128 shiftRight(final int inCount)
	{
		long count = inCount & 127;
		final long M1 = ((((count + 127) | count) & 64) >>> 6) - 1L;
		final long M2 = (count >>> 6) - 1L;
		final long high = m_highBits;
		final long low = m_lowBits;

		count &= 63;
		m_lowBits = (high >>> count) & (~M2);
		m_highBits = (high >>> count) & M2;
		m_lowBits |= ((low >>> count) | ((high << (64 - count)) & M1)) & M2;

		return this;
	}

	/**
	 * Multiplies two 64-bit factors and produces a 128-bit product 
	 * @param inValue1 64-bit unsigned factor
	 * @param inValue2 64-bit unsigned factor
	 * @param inResult 128-bit unsigned product
	 * From <a href="https://www.codeproject.com/Tips/618570/UInt-Multiplication-Squaring"
	 * >www.codeproject.com</a>
	 */
	private static void multiply(final long inValue1, final long inValue2,
			final MutableUnsigned128 inResult)
	{
		int powerFactor1 = Long.SIZE - Long.numberOfLeadingZeros(inValue1);
		int powerFactor2 = Long.SIZE - Long.numberOfLeadingZeros(inValue2);

		if (powerFactor1 + powerFactor2 < Long.SIZE)
		{
			inResult.m_highBits = 0;
			inResult.m_lowBits = inValue1 * inValue2;
		}
		else
		{
			long factor1 = inValue1;
			long factor2 = inValue2;
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

			inResult.m_highBits = (factor1 * factor2) + w1 + k;
			inResult.m_lowBits = (t << Integer.SIZE) + w3;
		}
	}
	
	/**
	 * Divides this 128-bit unsigned value with divisor 
	 * @param inDivisor divisor 128-bit unsigned
	 * @param inRemainder remainder of the division 128-bit unsigned
	 * @return this instance updated with the 128-bit quotient
	 * From <a href="http://www.codeproject.com/Tips/785014/UInt-Division-Modulus"
	 * >www.codeproject.com</a>
	 */
	public MutableUnsigned128 divide(final MutableUnsigned128 inDivisor, final MutableUnsigned128 inRemainder)
	{
		if ((m_highBits | inDivisor.m_highBits) == 0)
		{
			final long lowBits = m_lowBits;
			m_highBits = 0;
			m_lowBits = Unsigned.divide(lowBits, inDivisor.m_lowBits);
			inRemainder.m_highBits = 0;
			inRemainder.m_lowBits = Unsigned.remainder(lowBits, inDivisor.m_lowBits);
		}
		else if (inDivisor.m_highBits == 0)
		{
			final MutableUnsigned128 remainder = new MutableUnsigned128(); 
			long quotientLow = 0;
			long quotientHigh = 0;
			if (Unsigned.compare(m_highBits, inDivisor.m_lowBits) <= -1)
			{
				// remainder contains quotient and reminder
				quotientLow = divide(m_highBits, m_lowBits, inDivisor.m_lowBits, remainder);
				quotientHigh = 0;
			}
			else
			{
				quotientHigh = Unsigned.divide(m_highBits, inDivisor.m_lowBits);
				final long remainderHigh = Unsigned.remainder(m_highBits, inDivisor.m_lowBits);
				quotientLow = divide(remainderHigh, m_lowBits, inDivisor.m_lowBits, remainder);
			}
			m_highBits = quotientHigh;
			m_lowBits = quotientLow;
			inRemainder.m_highBits = 0;
			inRemainder.m_lowBits = remainder.m_lowBits;
		}
		else
		{
			final int zeros = Long.numberOfLeadingZeros(inDivisor.m_highBits);
			final MutableUnsigned128 v1 = new MutableUnsigned128(inDivisor).shiftLeft(zeros);
			final MutableUnsigned128 u1 = new MutableUnsigned128(this).shiftRight(1);
			final MutableUnsigned128 q1 = new MutableUnsigned128();

			q1.m_lowBits = divide(u1.m_highBits, u1.m_lowBits, v1.m_highBits,
					new MutableUnsigned128());
			q1.m_highBits = 0;
			q1.shiftRight(63 - zeros);
			if ((q1.m_highBits | q1.m_lowBits) != 0)
			{
				q1.decrement();
			}

			final MutableUnsigned128 quotient = new MutableUnsigned128(q1);
			q1.multiply(inDivisor);
			inRemainder.m_highBits = m_highBits;
			inRemainder.m_lowBits = m_lowBits;
			inRemainder.subtract(q1);
			if (inRemainder.compareTo(inDivisor) >= 0)
			{
				quotient.increment();
				inRemainder.subtract(inDivisor);
			}
			m_highBits = quotient.m_highBits;
			m_lowBits = quotient.m_lowBits;
		}
		return this;
	}

	/**
	 * Iterative division of 128-bit dividend by 64-bit divisor.
	 * @param inDividendHigh 64 higher bits
	 * @param inDividendLow 64 lower bits
	 * @param inDivisor 64-bit
	 * @param inResult returns the quotient and remainder as high bits and low bits respectively
	 * @return 64-bit quotient
	 * From <a href="http://www.codeproject.com/Tips/785014/UInt-Division-Modulus"
	 * >www.codeproject.com</a>
	 */
	private static long divide(final long inDividendHigh, final long inDividendLow,
			final long inDivisor, final MutableUnsigned128 inResult)
	{
		final int zeros = Long.numberOfLeadingZeros(inDivisor);
		long v = inDivisor << zeros;
		final long vn1 = v >>> Integer.SIZE;
		final long vn0 = v & INT_BITS;
		final long un32;
		final long un10;

		if (zeros > 0)
		{
			un32 = (inDividendHigh << zeros) | (inDividendLow >>> (Long.SIZE - zeros));
			un10 = inDividendLow << zeros;
		}
		else
		{
			un32 = inDividendHigh;
			un10 = inDividendLow;
		}

		final long un1 = un10 >>> Integer.SIZE;
		final long un0 = un10 & INT_BITS;
		long q1 = Unsigned.divide(un32, vn1);
		long rhat = Unsigned.remainder(un32, vn1);
		long left = q1 * vn0;
		long right = (rhat << Integer.SIZE) + un1;

		while (Unsigned.compare(q1, LIMIT) >= 0 || Unsigned.compare(left, right) >= 1)
		{
			--q1;
			rhat += vn1;
			if (Unsigned.compare(rhat, LIMIT) < 0)
			{
				left -= vn0;
				right = (rhat << Integer.SIZE) | un1;
			}
			else
			{
				break;
			}
		}

		final long un21 = (un32 << Integer.SIZE) + (un1 - (q1 * v));
		long q0 = Unsigned.divide(un21, vn1);

		rhat = Unsigned.remainder(un21, vn1);
		left = q0 * vn0;
		right = (rhat << Integer.SIZE) | un0;

		while (Unsigned.compare(q0, LIMIT) >= 0 || Unsigned.compare(left, right) >= 1)
		{
			--q0;
			rhat += vn1;
			if (Unsigned.compare(rhat, LIMIT) < 0)
			{
				left -= vn0;
				right = (rhat << Integer.SIZE) | un0;
			}
			else
			{
				break;
			}
		}
		
		final long quotient = (q1 << Integer.SIZE) | q0;
		final long remainder = ((un21 << Integer.SIZE) + (un0 - (q0 * v))) >>> zeros;

		inResult.m_highBits = quotient;
		inResult.m_lowBits = remainder;

		return quotient;
	}
}
