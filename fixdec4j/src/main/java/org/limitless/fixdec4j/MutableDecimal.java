package org.limitless.fixdec4j;

/**
 * This class implements a mutable fixed decimal number, see the DecimalFlyweight class
 * for details.
 * @author Fredah
 */
public final class MutableDecimal implements Comparable<MutableDecimal>
{
	// mantissa limits
	public static final long MANTISSA_MAX = Decimal64Flyweight.MANTISSA_MAX;
	public static final long MANTISSA_MIN = Decimal64Flyweight.MANTISSA_MIN;
	public static final long MANTISSA_ERROR = Decimal64Flyweight.MANTISSA_ERROR;
	
	// constants
	public static final MutableDecimal MAX_VALUE = new MutableDecimal(Decimal64Flyweight.MAX_VALUE);
	public static final MutableDecimal MIN_VALUE = new MutableDecimal(Decimal64Flyweight.MIN_VALUE);
	public static final MutableDecimal ZERO = new MutableDecimal(0);
	public static final MutableDecimal NAN = new MutableDecimal(Decimal64Flyweight.NAN);

	private long m_fixedValue;

	/**
	 * Constructs an empty mutable decimal.
	 */
	public MutableDecimal()
	{
		m_fixedValue = 0;
	}

	/**
	 * Constructs a mutable decimal from a decimal fly-weight value
	 * @param inFixedValue decimal fly-weight value
	 */
	private MutableDecimal(final long inFixedValue)
	{
		m_fixedValue = inFixedValue;
	}

	/**
	 * Constructs a mutable decimal from another instance.
	 * @param inValue fixed decimal
	 */
	public MutableDecimal(final MutableDecimal inValue)
	{
		m_fixedValue = inValue.m_fixedValue;
	}

	/**
	 * Constructs a mutable decimal from a scaled mantissa and an exponent.
	 * @param inScaledMantissa scaled mantissa
	 * @param inExponent exponent
	 */
	public MutableDecimal(final long inScaledMantissa, final int inExponent)
	{
		m_fixedValue = Decimal64Flyweight.valueOf(inScaledMantissa, inExponent);
	}

	/**
	 * Constructs a mutable decimal from a mantissa and an exponent.
	 * @param inScaledMantissa scaled mantissa
	 * @param inExponent exponent
	 * @return mutable decimal instance
	 */
	public static MutableDecimal valueOf(final long inScaledMantissa, final int inExponent)
	{
		return new MutableDecimal(inScaledMantissa, inExponent);
	}

	/**
	 * Constructs a mutable decimal value from a string.
	 * @param inValue string
	 * @return new mutable decimal instance
	 */
	public static MutableDecimal valueOf(final String inValue)
	{
		final MutableDecimal fixed = new MutableDecimal();
		fixed.m_fixedValue = Decimal64Flyweight.valueOf(inValue);
		return fixed;
	}

	/**
	 * Constructs a mutable decimal value from a double.
	 * @param inValue double
	 * @param inDecimals number of decimals
	 * @return new mutable decimal instance
	 */
	public static MutableDecimal valueOf(final double inValue, final int inDecimals)
	{
		final MutableDecimal fixed = new MutableDecimal();
		fixed.m_fixedValue = Decimal64Flyweight.valueOf(inValue, inDecimals);
		return fixed;
	}

	/**
	 * Returns a string representation of the object.
	 * @return string representation
	 */
	@Override
	public String toString()
	{
		return Decimal64Flyweight.toString(m_fixedValue);
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

		return m_fixedValue == ((MutableDecimal) inObject).m_fixedValue;
	}

	/**
	 * Returns a hash code value for the object
	 * @return hash code
	 */
	@Override
	public int hashCode()
	{
		return Decimal64Flyweight.hashCode(m_fixedValue);
	}
	
	/**
	 * Compares this object with the specified object for order.
	 * @param inValue other object
	 * @return less than (-1), equals (0) or greater than (1) the other object 
	 */
	@Override
	public int compareTo(final MutableDecimal inValue)
	{
		if (inValue == null)
		{
			return 1;
		}

		return Decimal64Flyweight.compareTo(m_fixedValue, inValue.m_fixedValue);
	}
	
	/**
	 * Returns the unscaled mantissa of this instance.
	 * @return unscaled mantissa
	 */
	public long mantissa()
	{
		return Decimal64Flyweight.mantissa(m_fixedValue);
	}

	/**
	 * Returns the normalized exponent of this instance. Positive exponents are stored as a 
	 * scaled mantissa with a zero exponent.
	 * @return negative exponent or 0
	 */
	public int exponent()
	{
		return Decimal64Flyweight.exponent(m_fixedValue);
	}

	/**
	 * Returns true when the instance is equal to NAN.
	 * @return true when equal to NAN
	 */
	public boolean isNaN()
	{
		return Decimal64Flyweight.isNaN(m_fixedValue);
	}
	
	/**
	 * Returns true when the mantissa of the value is zero (disregards the exponent)
	 * @return true for zero values
	 */
	public boolean isZero()
	{
		return Decimal64Flyweight.isZero(m_fixedValue);
	}
	
	/**
	 * Returns the sum of this instance and its argument.
	 * @param inValue term
	 * @return this instance with the sum or NAN indicating overflow
	 */
	public MutableDecimal add(final MutableDecimal inValue)
	{
		m_fixedValue = Decimal64Flyweight.add(m_fixedValue, inValue.m_fixedValue);
		return this;
	}

	/**
	 * Returns the difference of this instance and its argument.
	 * @param inValue term
	 * @return this instance with the difference or NAN indicating overflow.
	 */
	public MutableDecimal subtract(final MutableDecimal inValue)
	{
		m_fixedValue = Decimal64Flyweight.subtract(m_fixedValue, inValue.m_fixedValue);
		return this;
	}

	/**
	 * Negates the value of this instance.
	 * @return this instance with a negated value. 
	 */
	public MutableDecimal minus()
	{
		m_fixedValue = Decimal64Flyweight.minus(m_fixedValue);
		return this;
	}

	/**
	 * Returns the product of this instance and its argument rounding away from zero.
	 * @param inValue factor
	 * @return this instance with the product or NAN indicating overflow.
	 */
	public MutableDecimal multiply(final MutableDecimal inValue)
	{
		m_fixedValue = Decimal64Flyweight.multiply(m_fixedValue, inValue.m_fixedValue, DecimalRounding.UP);
		return this;
	}
	
	/**
	 * Returns the product of this instance and its argument rounding away from zero.
	 * @param inValue factor
	 * @param inMode decimal rounding mode
	 * @return this instance with the product or NAN indicating overflow.
	 */
	public MutableDecimal multiply(final MutableDecimal inValue, final DecimalRounding inMode)
	{
		m_fixedValue = Decimal64Flyweight.multiply(m_fixedValue, inValue.m_fixedValue, inMode);
		return this;
	}

	/**
	 * Returns the quotient of this instance and its argument rounding away from zero.
	 * @param inValue divisor
	 * @return this instance with the quotient or NAN indicating overflow.
	 */
	public MutableDecimal divide(final MutableDecimal inValue)
	{
		m_fixedValue = Decimal64Flyweight.divide(m_fixedValue, inValue.m_fixedValue, DecimalRounding.UP);
		return this;
	}

	/**
	 * Returns the quotient of this instance and its argument rounding away from zero.
	 * @param inValue divisor
	 * @param inMode decimal rounding mode
	 * @return this instance with the quotient or NAN indicating overflow.
	 */
	public MutableDecimal divide(final MutableDecimal inValue, final DecimalRounding inMode)
	{
		m_fixedValue = Decimal64Flyweight.divide(m_fixedValue, inValue.m_fixedValue, inMode);
		return this;
	}

	/**
	 * Returns this instance rounded according to the decimal rounding mode.
	 * @param inDecimals mutable decimal value
	 * @param inMode rounding mode
	 * @return this instance rounded according to the rounding mode
	 */
	public MutableDecimal round(final int inDecimals, final DecimalRounding inMode)
	{
		m_fixedValue = Decimal64Flyweight.round(m_fixedValue, inDecimals, inMode);
		return this;
	}
	
	/**
	 * Returns the absolute value of this instance.
	 * @return this instance with absolute value applied.
	 */
	public MutableDecimal abs()
	{
		m_fixedValue = Decimal64Flyweight.abs(m_fixedValue);
		return this;
	}

	/**
	 * Returns the value of the specified number as a byte, which may involve rounding or truncation.
	 * @return byte value
	 */
	public byte byteValue()
	{
		return Decimal64Flyweight.byteValue(m_fixedValue);
	}

	/**
	 * Returns the value of the specified number as a short, which may involve rounding or truncation.
	 * @return short value
	 */
	public short shortValue()
	{
		return Decimal64Flyweight.shortValue(m_fixedValue);
	}

	/**
	 * Returns the value of the specified number as an integer, which may involve rounding or truncation.
	 * @return integer value
	 */
	public int intValue()
	{
		return Decimal64Flyweight.intValue(m_fixedValue);
	}


	/**
	 * Returns the value of the specified number as an integer, which may involve rounding or truncation.
	 * @return long value
	 */
	public long longValue()
	{
		return Decimal64Flyweight.longValue(m_fixedValue);
	}

	/**
	 * Returns the value of the specified number as a float, which may involve rounding.
	 * @return float value
	 */
	public float floatValue()
	{
		return Decimal64Flyweight.floatValue(m_fixedValue);
	}

	/**
	 * Returns the value of the specified number as a double, which may involve rounding.
	 * @return double value
	 */
	public double doubleValue()
	{
		return Decimal64Flyweight.doubleValue(m_fixedValue);
	}
	
	/**
	 * Returns the decimal fly-weight representation of this instance.
	 * @return decimal fly-weight value
	 */
	public long toLongBits()
	{
		return m_fixedValue;
	}

	/**
	 * Returns a mutable decimal instance constructed from a decimal fly-weight representation.
	 * @param inFixedValue decimal fly-weight value
	 * @return decimal instance
	 */
	public MutableDecimal fromLongBits(final long inFixedValue)
	{
		m_fixedValue = inFixedValue;
		return this;
	}
}
