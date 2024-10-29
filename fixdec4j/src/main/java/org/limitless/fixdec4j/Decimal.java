package org.limitless.fixdec4j;

/**
 * This class implements an immutable fixed decimal number, see the DecimalFlyweight for details.
 * @author Fredah
 */
public final class Decimal implements Comparable<Decimal>
{
	public static final long MANTISSA_MAX = DecimalFlyweight.MANTISSA_MAX;
	public static final long MANTISSA_MIN = DecimalFlyweight.MANTISSA_MIN;
	public static final long MANTISSA_ERROR = DecimalFlyweight.MANTISSA_ERROR;
	
	public static final Decimal MAX_VALUE = new Decimal(DecimalFlyweight.MAX_VALUE);
	public static final Decimal MIN_VALUE = new Decimal(DecimalFlyweight.MIN_VALUE);
	public static final Decimal ZERO = new Decimal(0);  
	public static final Decimal NAN = new Decimal(DecimalFlyweight.NAN);

	private final long m_fixedValue;

	private Decimal(final long inFixedValue)
	{
		m_fixedValue = inFixedValue;
	}

	/**
	 * Constructs an immutable decimal from another decimal.
	 * @param inValue decimal instance
	 */
	public Decimal(final Decimal inValue)
	{
		m_fixedValue = inValue.m_fixedValue;
	}

	/**
	 * Constructs an immutable decimal from a scaled mantissa and an exponent.
	 * @param inScaledMantissa scaled mantissa
	 * @param inExponent exponent
	 */
	public Decimal(final long inScaledMantissa, final int inExponent)
	{
		m_fixedValue = DecimalFlyweight.valueOf(inScaledMantissa, inExponent);
	}

	/**
	 * Constructs an immutable decimal from a scaled mantissa and an exponent.
	 * @param inScaledMantissa scaled mantissa
	 * @param inExponent exponent
	 * @return decimal instance
	 */
	public static Decimal valueOf(final long inScaledMantissa, final int inExponent)
	{
		return new Decimal(inScaledMantissa, inExponent);
	}

	/**
	 * Constructs an immutable decimal value from a string.
	 * @param inValue string
	 * @return new mutable decimal instance
	 */
	public static Decimal valueOf(final String inValue)
	{
		return new Decimal(DecimalFlyweight.valueOf(inValue));
	}

	/**
	 * Constructs an immutable decimal value from a double.
	 * @param inValue double
	 * @param inDecimals number of decimals
	 * @return new mutable decimal instance
	 */
	public static Decimal valueOf(final double inValue, final int inDecimals)
	{
		return new Decimal(DecimalFlyweight.valueOf(inValue, inDecimals));
	}

	/**
	 * Returns a string representation of the object.
	 * @return string representation
	 */
	@Override
	public String toString()
	{
		return DecimalFlyweight.toString(m_fixedValue);
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

		return m_fixedValue == ((Decimal) inObject).m_fixedValue;
	}

	/**
	 * Returns a hash code value for the object
	 * @return hash code
	 */
	@Override
	public int hashCode()
	{
		return DecimalFlyweight.hashCode(m_fixedValue);
	}

	/**
	 * Compares this object with the specified object for order.
	 * @param inValue other object
	 * @return less than (-1), equals (0) or greater than (1) the other object 
	 */
	@Override
	public int compareTo(final Decimal inValue)
	{
		if (inValue == null)
		{
			return 1;
		}

		return DecimalFlyweight.compareTo(m_fixedValue, inValue.m_fixedValue);
	}

	/**
	 * Returns the unscaled mantissa of this instance
	 * @return unscaled mantissa
	 */
	public long mantissa()
	{
		return DecimalFlyweight.mantissa(m_fixedValue);
	}

	/**
	 * Returns the normalized exponent of this instance. Positive exponents are stored as a 
	 * scaled mantissa with a zero exponent.
	 * @return negative exponent or 0
	 */
	public int exponent()
	{
		return DecimalFlyweight.exponent(m_fixedValue);
	}
	
	/**
	 * Returns true when the instance is equal to NAN.
	 * @return true when equal to NAN
	 */
	public boolean isNaN()
	{
		return DecimalFlyweight.isNaN(m_fixedValue);
	}
	
	/**
	 * Returns true when the mantissa of the value is zero (disregards the exponent)
	 * @return true for zero values
	 */
	public boolean isZero()
	{
		return DecimalFlyweight.isZero(m_fixedValue);
	}
	
	/**
	 * Returns the sum of this instance and its argument.
	 * @param inValue term
	 * @return a new instance with the sum or NAN indicating overflow.
	 */
	public Decimal add(final Decimal inValue)
	{
		return new Decimal(DecimalFlyweight.add(m_fixedValue, inValue.m_fixedValue));
	}

	/**
	 * Returns the difference of this instance and its argument.
	 * @param inValue term
	 * @return a new instance with the difference or NAN indicating overflow.
	 */
	public Decimal subtract(final Decimal inValue)
	{
		return new Decimal(DecimalFlyweight.subtract(m_fixedValue, inValue.m_fixedValue));
	}

	/**
	 * Returns an negated immutable decimal. 
	 * @return a negated new instance. 
	 */
	public Decimal minus()
	{
		return new Decimal(DecimalFlyweight.minus(m_fixedValue));
	}

	/**
	 * Returns the product of this instance and its argument rounding away from zero.
	 * @param inValue factor
	 * @return a new instance with the product or NAN indicating overflow.
	 */
	public Decimal multiply(final Decimal inValue)
	{
		return new Decimal(DecimalFlyweight.multiply(m_fixedValue, inValue.m_fixedValue, DecimalRounding.UP));
	}

	/**
	 * Returns the product of this instance and its argument.
	 * @param inValue factor
	 * @param inMode decimal rounding mode
	 * @return a new instance with the product or NAN indicating overflow.
	 */
	public Decimal multiply(final Decimal inValue, final DecimalRounding inMode)
	{
		return new Decimal(DecimalFlyweight.multiply(m_fixedValue, inValue.m_fixedValue, inMode));
	}

	/**
	 * Returns the quotient of this instance and its argument rounding away from zero.
	 * @param inValue divisor
	 * @return a new instance with the quotient or NAN indicating overflow.
	 */
	public Decimal divide(final Decimal inValue)
	{
		return new Decimal(DecimalFlyweight.divide(m_fixedValue, inValue.m_fixedValue, DecimalRounding.UP));
	}

	/**
	 * Returns the quotient of this instance.
	 * @param inValue divisor
	 * @param inMode decimal rounding mode
	 * @return a new instance with the quotient or NAN indicating overflow.
	 */
	public Decimal divide(final Decimal inValue, final DecimalRounding inMode)
	{
		return new Decimal(DecimalFlyweight.divide(m_fixedValue, inValue.m_fixedValue, inMode));
	}

	/**
	 * Returns a decimal instance rounded according to the decimal rounding mode.
	 * @param inDecimals decimal value
	 * @param inMode rounding mode
	 * @return new instance rounded according to the rounding mode or NAN indicating overflow
	 */
	public Decimal round(int inDecimals, DecimalRounding inMode)
	{
		return new Decimal(DecimalFlyweight.round(m_fixedValue, inDecimals, inMode));
	}
	
	/**
	 * Returns the absolute value of the decimal.
	 * @return new absolute value instance
	 */
	public Decimal abs()
	{
		return new Decimal(DecimalFlyweight.abs(m_fixedValue));
	}

	/**
	 * Returns the value of the specified number as a byte, which may involve rounding or truncation.
	 * @return byte value
	 */
	public byte byteValue()
	{
		return DecimalFlyweight.byteValue(m_fixedValue);
	}

	/**
	 * Returns the value of the specified number as a short, which may involve rounding or truncation.
	 * @return short value
	 */
	public short shortValue()
	{
		return DecimalFlyweight.shortValue(m_fixedValue);
	}
	
	/**
	 * Returns the value of the specified number as an integer, which may involve rounding or truncation.
	 * @return integer value
	 */
	public int intValue()
	{
		return DecimalFlyweight.intValue(m_fixedValue);
	}

	/**
	 * Returns the value of the specified number as an integer, which may involve rounding or truncation.
	 * @return long value
	 */
	public long longValue()
	{
		return DecimalFlyweight.longValue(m_fixedValue);
	}

	/**
	 * Returns the value of the specified number as a float, which may involve rounding.
	 * @return float value
	 */
	public float floatValue()
	{
		return DecimalFlyweight.floatValue(m_fixedValue);
	}

	/**
	 * Returns the value of the specified number as a double, which may involve rounding.
	 * @return double value
	 */
	public double doubleValue()
	{
		return DecimalFlyweight.doubleValue(m_fixedValue);
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
	 * Returns a decimal instance constructed from a decimal fly-weight representation.
	 * @param inFixedValue decimal fly-weight value
	 * @return decimal instance
	 */
	public static Decimal fromLongBits(final long inFixedValue)
	{
		return new Decimal(inFixedValue);
	}
}
