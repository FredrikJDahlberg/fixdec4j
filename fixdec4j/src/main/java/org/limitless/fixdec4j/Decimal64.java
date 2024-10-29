package org.limitless.fixdec4j;

/**
 * This class implements an immutable fixed decimal number, see the DecimalFlyweight for details.
 * @author Fredah
 */
public final class Decimal64 implements Comparable<Decimal64>
{
	public static final long MANTISSA_MAX = Decimal64Flyweight.MANTISSA_MAX;
	public static final long MANTISSA_MIN = Decimal64Flyweight.MANTISSA_MIN;
	public static final long MANTISSA_ERROR = Decimal64Flyweight.MANTISSA_ERROR;
	
	public static final Decimal64 MAX_VALUE = new Decimal64(Decimal64Flyweight.MAX_VALUE);
	public static final Decimal64 MIN_VALUE = new Decimal64(Decimal64Flyweight.MIN_VALUE);
	public static final Decimal64 ZERO = new Decimal64(0);
	public static final Decimal64 NAN = new Decimal64(Decimal64Flyweight.NAN);

	private final long m_fixedValue;

	private Decimal64(final long inFixedValue)
	{
		m_fixedValue = inFixedValue;
	}

	/**
	 * Constructs an immutable decimal from another decimal.
	 * @param inValue decimal instance
	 */
	public Decimal64(final Decimal64 inValue)
	{
		m_fixedValue = inValue.m_fixedValue;
	}

	/**
	 * Constructs an immutable decimal from a scaled mantissa and an exponent.
	 * @param inScaledMantissa scaled mantissa
	 * @param inExponent exponent
	 */
	public Decimal64(final long inScaledMantissa, final int inExponent)
	{
		m_fixedValue = Decimal64Flyweight.valueOf(inScaledMantissa, inExponent);
	}

	/**
	 * Constructs an immutable decimal from a scaled mantissa and an exponent.
	 * @param inScaledMantissa scaled mantissa
	 * @param inExponent exponent
	 * @return decimal instance
	 */
	public static Decimal64 valueOf(final long inScaledMantissa, final int inExponent)
	{
		return new Decimal64(inScaledMantissa, inExponent);
	}

	/**
	 * Constructs an immutable decimal value from a string.
	 * @param inValue string
	 * @return new mutable decimal instance
	 */
	public static Decimal64 valueOf(final String inValue)
	{
		return new Decimal64(Decimal64Flyweight.valueOf(inValue));
	}

	/**
	 * Constructs an immutable decimal value from a double.
	 * @param inValue double
	 * @param inDecimals number of decimals
	 * @return new mutable decimal instance
	 */
	public static Decimal64 valueOf(final double inValue, final int inDecimals)
	{
		return new Decimal64(Decimal64Flyweight.valueOf(inValue, inDecimals));
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

		return m_fixedValue == ((Decimal64) inObject).m_fixedValue;
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
	public int compareTo(final Decimal64 inValue)
	{
		if (inValue == null)
		{
			return 1;
		}

		return Decimal64Flyweight.compareTo(m_fixedValue, inValue.m_fixedValue);
	}

	/**
	 * Returns the unscaled mantissa of this instance
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
	 * @return a new instance with the sum or NAN indicating overflow.
	 */
	public Decimal64 add(final Decimal64 inValue)
	{
		return new Decimal64(Decimal64Flyweight.add(m_fixedValue, inValue.m_fixedValue));
	}

	/**
	 * Returns the difference of this instance and its argument.
	 * @param inValue term
	 * @return a new instance with the difference or NAN indicating overflow.
	 */
	public Decimal64 subtract(final Decimal64 inValue)
	{
		return new Decimal64(Decimal64Flyweight.subtract(m_fixedValue, inValue.m_fixedValue));
	}

	/**
	 * Returns an negated immutable decimal. 
	 * @return a negated new instance. 
	 */
	public Decimal64 minus()
	{
		return new Decimal64(Decimal64Flyweight.minus(m_fixedValue));
	}

	/**
	 * Returns the product of this instance and its argument rounding away from zero.
	 * @param inValue factor
	 * @return a new instance with the product or NAN indicating overflow.
	 */
	public Decimal64 multiply(final Decimal64 inValue)
	{
		return new Decimal64(Decimal64Flyweight.multiply(m_fixedValue, inValue.m_fixedValue, DecimalRounding.UP));
	}

	/**
	 * Returns the product of this instance and its argument.
	 * @param inValue factor
	 * @param inMode decimal rounding mode
	 * @return a new instance with the product or NAN indicating overflow.
	 */
	public Decimal64 multiply(final Decimal64 inValue, final DecimalRounding inMode)
	{
		return new Decimal64(Decimal64Flyweight.multiply(m_fixedValue, inValue.m_fixedValue, inMode));
	}

	/**
	 * Returns the quotient of this instance and its argument rounding away from zero.
	 * @param inValue divisor
	 * @return a new instance with the quotient or NAN indicating overflow.
	 */
	public Decimal64 divide(final Decimal64 inValue)
	{
		return new Decimal64(Decimal64Flyweight.divide(m_fixedValue, inValue.m_fixedValue, DecimalRounding.UP));
	}

	/**
	 * Returns the quotient of this instance.
	 * @param inValue divisor
	 * @param inMode decimal rounding mode
	 * @return a new instance with the quotient or NAN indicating overflow.
	 */
	public Decimal64 divide(final Decimal64 inValue, final DecimalRounding inMode)
	{
		return new Decimal64(Decimal64Flyweight.divide(m_fixedValue, inValue.m_fixedValue, inMode));
	}

	/**
	 * Returns a decimal instance rounded according to the decimal rounding mode.
	 * @param inDecimals decimal value
	 * @param inMode rounding mode
	 * @return new instance rounded according to the rounding mode or NAN indicating overflow
	 */
	public Decimal64 round(int inDecimals, DecimalRounding inMode)
	{
		return new Decimal64(Decimal64Flyweight.round(m_fixedValue, inDecimals, inMode));
	}
	
	/**
	 * Returns the absolute value of the decimal.
	 * @return new absolute value instance
	 */
	public Decimal64 abs()
	{
		return new Decimal64(Decimal64Flyweight.abs(m_fixedValue));
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
	 * Returns a decimal instance constructed from a decimal fly-weight representation.
	 * @param inFixedValue decimal fly-weight value
	 * @return decimal instance
	 */
	public static Decimal64 fromLongBits(final long inFixedValue)
	{
		return new Decimal64(inFixedValue);
	}
}
