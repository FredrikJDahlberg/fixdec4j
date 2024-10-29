package org.limitless.fixdec4j;

/**
 * This class implements fixed decimal arithmetic using a 61-bit (two's
 * complement) mantissa and 3-bit unsigned value for decimals: 1mmm mmmm 2mmm
 * mmmm 3mmm mmmm 4mmm mmmm 5mmm mmmm 6mmm mmmm 7mmm mmmm 8mmm mddd.
 * <p>
 * The largest mantissa ranges from -2^60-2 to 2^60-1, decimals 0 - 7 and Not a
 * Number is represented as 2^60-1.
 * <p>
 * The multiplication and division methods will round the result properly to the
 * largest precision of its operands. Note that only the number of decimals is
 * stored, e.g 100e0 and 1e2 are normalized to 100e0.
 * <p>
 * The supported decimal rounding modes are Up and Down, see RoundingMode.
 * <p>
 * The arithmetic operations will not overflow unless the result cannot be
 * represented within the limits above (intermediate values use 128-bit
 * arithmetic when necessary).
 */
public final class DecimalFlyweight
{
	private static final long DECIMAL_BITS = 3;
	private static final long DECIMAL_MASK = (1L << DECIMAL_BITS) - 1;

	// limits
	private static final long MANTISSA_BITS = Long.SIZE - DECIMAL_BITS;
	public static final int DECIMALS_MAX = (1 << DECIMAL_BITS) - 1;
	public static final int EXPONENT_MIN = -DECIMALS_MAX;
	public static final int EXPONENT_MAX = 18;

	public static final long MANTISSA_MAX = (Long.MAX_VALUE >>> DECIMAL_BITS);
	public static final long MANTISSA_MIN = -MANTISSA_MAX + 1;
	public static final long MANTISSA_ERROR = -MANTISSA_MAX;

	// constants
	public static final long NAN = MANTISSA_ERROR << DECIMAL_BITS;
	public static final long MAX_VALUE = valueOf(MANTISSA_MAX, 0);
	public static final long MIN_VALUE = valueOf(MANTISSA_MIN, EXPONENT_MIN);

	private static final int PARSE_STATE_MANTISSA = 1;
	private static final int PARSE_STATE_EXPONENT = 2;
	private static final int PARSE_POINT = '.' - '0';

	/**
	 * Constructs a decimal flyweight from a scaled mantissa and an exponent. A
	 * positive exponent is stored as zero decimals, with the mantissa scaled
	 * accordingly.
	 * @param inScaledMantissa scaled mantissa, e.g. 200.50 is 20050e-2
	 * @param inExponent exponent
	 * @return decimal flyweight or NAN indicating overflow
	 */
	public static long valueOf(final long inScaledMantissa, final int inExponent)
	{
		boolean valid = inExponent >= -DECIMALS_MAX && inExponent <= EXPONENT_MAX;
		long mantissa = Math.abs(inScaledMantissa);
		int exponent = inExponent;

		if (valid)
		{
			if (inExponent >= 1)
			{
				final long scale = Powers10[inExponent];
				final int bitCount = Unsigned.numberOfBits(Math.abs(mantissa))
						+ Unsigned.numberOfBits(scale);
				valid = bitCount < MANTISSA_BITS;
				mantissa *= scale;
				exponent = 0;
			}
		}
		if (valid)
		{
			valid = mantissa <= MANTISSA_MAX;
		}
		if (inScaledMantissa < 0)
		{
			mantissa = -mantissa;
		}

		return valid ? encode(mantissa, exponent) : NAN;
	}

	/**
	 * Constructs a decimal flyweight value from a string ("-123.45678").
	 * Exponent notation is not supported.
	 * @param inString string
	 * @return 64-bit encoded fixed decimal or NaN
	 */
	public static long valueOf(final String inString)
	{
		if (inString == null)
		{
			return NAN;
		}

		final long length = inString.length();
		if (length == 0)
		{
			return NAN;
		}

		final boolean positive = inString.charAt(0) != '-';
		int position = positive ? 0 : 1;
		if (position == length)
		{
			return NAN;
		}

		int digit = inString.charAt(position++) - '0';
		if (digit < 0 || digit >= 10)
		{
			return NAN;
		}

		final long minimum = Long.MIN_VALUE / 10;
		long mantissa = -digit;
		int decimals = 0;
		int state = PARSE_STATE_MANTISSA;

		while (position < length)
		{
			digit = inString.charAt(position++) - '0';
			if ((digit < 0 && digit != PARSE_POINT) || digit >= 10 || mantissa < minimum)
			{
				return NAN;
			}
			if (digit == PARSE_POINT)
			{
				if (state != PARSE_STATE_MANTISSA)
				{
					return NAN;
				}
				state = PARSE_STATE_EXPONENT;
			}
			else
			{
				if (state == PARSE_STATE_EXPONENT)
				{
					++decimals;
				}
				mantissa *= 10;
				mantissa -= digit;
			}
		}
		if (digit == PARSE_POINT || mantissa < -MANTISSA_MAX || decimals > DECIMALS_MAX)
		{
			return NAN;
		}
		if (positive)
		{
			mantissa = -mantissa;
		}
		return encode(mantissa, -decimals);
	}

	/**
	 * Constructs a decimal flyweight from a double.
	 * @param inValue double value
	 * @param inDecimals number of decimals
	 * @return decimal flyweight or NAN indicating overflow
	 */
	public static long valueOf(final double inValue, final int inDecimals)
	{
		if (Double.isNaN(inValue) || inDecimals < 0 || inDecimals > DECIMALS_MAX)
		{
			return NAN;
		}
		final long mantissa = Math.round(Math.abs(inValue) * Powers10[inDecimals]);

		return encode(inValue < 0 ? -mantissa : mantissa, -inDecimals);
	}

	/**
	 * Returns a string representation of the object.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @return string or "NAN" indicating overflow
	 */
	public static String toString(long inDecimalFlyweight)
	{
		if (isNaN(inDecimalFlyweight))
		{
			return "NaN";
		}
		int exponent = exponent(inDecimalFlyweight);
		int decimalCount = exponent < 0 ? -exponent : 0;
		long mantissa = mantissa(inDecimalFlyweight);
		char sign = 0;

		if (mantissa < 0)
		{
			sign = '-';
			mantissa = -mantissa;
		}

		final char[] string = new char[32];
		int offset = string.length;
		long integerValue = mantissa;
		long decimalValue;

		if (exponent < 0)
		{
			integerValue = mantissa / Powers10[decimalCount];
			decimalValue = mantissa % Powers10[decimalCount];
			offset = getChars(decimalValue, offset, string);

			int padding = decimalCount - (string.length - offset);

			switch (padding)
			{
				case 5:
					string[--offset] = '0';
				case 4:
					string[--offset] = '0';
				case 3:
					string[--offset] = '0';
				case 2:
					string[--offset] = '0';
				case 1:
					string[--offset] = '0';
					break;
				default:
					while (padding > 0)
					{
						string[--offset] = '0';
						--padding;
					}
			}
			string[--offset] = '.';
		}
		offset = getChars(integerValue, offset, string);
		if (sign != 0)
		{
			string[--offset] = sign;
		}

		return String.valueOf(string, offset, string.length - offset);
	}

	/**
	 * Returns a hash code for a decimal flyweight
	 * @param inDecimalFlyweight decimal flyweight
	 * @return hash code
	 */
	public static int hashCode(long inDecimalFlyweight)
	{
		return Long.hashCode(inDecimalFlyweight);
	}

	/**
	 * Compares two flyweight values for order.
	 * @param inDecimalFlyweight1 decimal flyweight value
	 * @param inDecimalFlyweight2 decimal flyweight value
	 * @return less than (-1), equals (0) or greater than (1) the other object.
	 */
	public static int compareTo(final long inDecimalFlyweight1, final long inDecimalFlyweight2)
	{
		if (inDecimalFlyweight1 == inDecimalFlyweight2)
		{
			return 0;
		}

		long mantissa1 = mantissa(inDecimalFlyweight1);
		long mantissa2 = mantissa(inDecimalFlyweight2);

		if ((mantissa1 < 0) != (mantissa2 < 0))
		{
			return mantissa1 < 0 ? -1 : 1;
		}

		final int decimals1 = -exponent(inDecimalFlyweight1);
		final int decimals2 = -exponent(inDecimalFlyweight2);
		int mantissaBits1 = Unsigned.numberOfBits(Math.abs(mantissa1));
		int mantissaBits2 = Unsigned.numberOfBits(Math.abs(mantissa2));

		if (decimals1 != decimals2)
		{
			final long scale = Powers10[Math.abs(decimals1 - decimals2)];
			final long scaleBits = Unsigned.numberOfBits(scale);

			if (decimals1 < decimals2)
			{
				mantissaBits1 += scaleBits;
				mantissa1 *= scale;
			}
			else
			{
				mantissaBits2 += scaleBits;
				mantissa2 *= scale;
			}
		}
		if (mantissa1 == mantissa2)
		{
			return 0;
		}
		else
		{
			final int result = mantissaBits1 < mantissaBits2 ? -1 : 1;
			return mantissa1 < 0 && mantissa2 < 0 ? -result : result;
		}
	}

	/**
	 * Returns the unscaled mantissa of its argument.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @return unscaled mantissa
	 */
	public static long mantissa(long inDecimalFlyweight)
	{
		return inDecimalFlyweight == NAN ? MANTISSA_ERROR : inDecimalFlyweight >> DECIMAL_BITS;
	}

	/**
	 * Returns the normalized exponent of this instance. Positive exponents are
	 * stored as a scaled mantissa with a zero exponent.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @return negative exponent or 0
	 */
	public static int exponent(long inDecimalFlyweight)
	{
		return (int) -(inDecimalFlyweight & DECIMAL_MASK);
	}

	/**
	 * Returns true when the instance is equal to NAN.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @return true when equal to NAN
	 */
	public static boolean isNaN(final long inDecimalFlyweight)
	{
		return inDecimalFlyweight == NAN;
	}

	/**
	 * Returns true when the mantissa of the value is zero (disregards the
	 * exponent)
	 * @param inDecimalFlyweight decimal flyweight value
	 * @return true for zero values
	 */
	public static boolean isZero(final long inDecimalFlyweight)
	{
		return mantissa(inDecimalFlyweight) == 0;
	}

	/**
	 * Returns the sum of two decimal flyweight values.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @param inTermFlyweight decimal flyweight value
	 * @return sum or NAN indicating overflow.
	 */
	public static long add(final long inDecimalFlyweight, final long inTermFlyweight)
	{
		if (isNaN(inDecimalFlyweight) || isNaN(inTermFlyweight))
		{
			return NAN;
		}

		return add(mantissa(inDecimalFlyweight), -exponent(inDecimalFlyweight),
				mantissa(inTermFlyweight), -exponent(inTermFlyweight));
	}

	/**
	 * Returns the sum of its arguments
	 * @param inValueMantissa value mantissa
	 * @param inValueDecimals number of value decimals
	 * @param inTermMantissa term mantissa
	 * @param inTermDecimals number of term decimals
	 * @return sum or NAN indicating overflow
	 */
	private static long add(final long inValueMantissa,
							final int inValueDecimals,
							final long inTermMantissa,
							final int inTermDecimals)
	{
		final boolean sameSign = inValueMantissa < 0 == inTermMantissa < 0;
		long valueMantissa = inValueMantissa;
		long termMantissa = inTermMantissa;
		int valuePower = Unsigned.numberOfBits(Math.abs(inValueMantissa));
		int termPower = Unsigned.numberOfBits(Math.abs(inTermMantissa));
		int resultPower = sameSign ? Math.max(valuePower, termPower)
				: Math.abs(valuePower - termPower);
		long result = MANTISSA_ERROR;

		// The maximum value of the mantissa is equal to Long.MAX_VALUE/8 so the
		// additions below cannot
		// overflow, MANTISSA_MAX*2 = Long.MAX_VALUE/4.
		if (resultPower > MANTISSA_BITS)
		{
			return NAN;
		}
		if (inValueDecimals != inTermDecimals)
		{
			final long scale = Powers10[Math.abs(inValueDecimals - inTermDecimals)];

			if (inValueDecimals < inTermDecimals)
			{
				valueMantissa *= scale;
				valuePower = Unsigned.numberOfBits(Math.abs(valueMantissa));
			}
			else
			{
				termMantissa *= scale;
				termPower = Unsigned.numberOfBits(Math.abs(termMantissa));
			}
			resultPower = sameSign ? Math.max(valuePower, termPower)
					: Math.abs(valuePower - termPower);
		}
		if (resultPower <= MANTISSA_BITS)
		{
			result = valueMantissa + termMantissa;
		}

		return encode(result, -Math.max(inValueDecimals, inTermDecimals));
	}

	/**
	 * Returns the difference of two decimal flyweight values.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @param inTermFlyweight decimal flyweight value
	 * @return difference or NAN indicating overflow.
	 */
	public static long subtract(final long inDecimalFlyweight, final long inTermFlyweight)
	{
		if (isNaN(inDecimalFlyweight) || isNaN(inTermFlyweight))
		{
			return NAN;
		}
		return add(mantissa(inDecimalFlyweight), -exponent(inDecimalFlyweight),
				-mantissa(inTermFlyweight), -exponent(inTermFlyweight));
	}

	/**
	 * Returns the negated value of its argument.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @return a negated decimal flyweight value.
	 */
	public static long minus(final long inDecimalFlyweight)
	{
		if (isNaN(inDecimalFlyweight))
		{
			return NAN;
		}
		final long mantissa = -mantissa(inDecimalFlyweight);
		final int exponent = exponent(inDecimalFlyweight);

		return encode(mantissa, exponent);
	}

	/**
	 * Returns the product of two decimal flyweight values rounded according to
	 * the rounding mode.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @param inFixedFactor decimal flyweight value
	 * @return product of the values or NAN indicating overflow
	 */
	public static long multiply(final long inDecimalFlyweight,
								final long inFixedFactor,
								DecimalRounding mode)
	{
		if (isNaN(inDecimalFlyweight) || isNaN(inFixedFactor))
		{
			return NAN;
		}

		final int valueDecimals = -exponent(inDecimalFlyweight);
		final int factorDecimals = -exponent(inFixedFactor);
		final long scaleDiff = Powers10[Math.abs(valueDecimals - factorDecimals)];
		final long valueMantissa = mantissa(inDecimalFlyweight);
		final long factorMantissa = mantissa(inFixedFactor);
		final int valuePower = Unsigned.numberOfBits(Math.abs(valueMantissa));
		final int factorPower = Unsigned.numberOfBits(Math.abs(factorMantissa));
		final int scalePower = Unsigned.numberOfBits(scaleDiff);
		final int decimals = Math.max(valueDecimals, factorDecimals);
		final long result;

		if (valuePower + factorPower + scalePower < MANTISSA_BITS)
		{
			result = roundedMultiply(valueMantissa, valueDecimals, factorMantissa, factorDecimals,
					mode);
		}
		else
		{
			result = roundedMultiply128(valueMantissa, valueDecimals, factorMantissa,
					factorDecimals, mode);
		}
		return encode(result, -decimals);
	}

	/**
	 * Divides 64-bit dividend with 64-bit divisor (using 128-bit arithmetic)
	 * @param inFixedDividend 64-bit fixed decimal
	 * @param inFixedDivisor 64-bit fixed decimal
	 * @return rounded quotient 64-bit fixed decimal
	 */
	public static long divide(	final long inFixedDividend,
								final long inFixedDivisor,
								final DecimalRounding inMode)
	{
		if (isNaN(inFixedDividend) || isNaN(inFixedDivisor) || inFixedDivisor == 0)
		{
			return NAN;
		}
		final int dividendDecimals = -exponent(inFixedDividend);
		final int divisorDecimals = -exponent(inFixedDivisor);
		final long dividendMantissa = mantissa(inFixedDividend);
		final long divisorMantissa = mantissa(inFixedDivisor);
		int quotientPower2 = Unsigned.numberOfBits(inFixedDividend);

		if (dividendDecimals != divisorDecimals)
		{
			if (dividendDecimals < divisorDecimals)
			{
				quotientPower2 += Unsigned.numberOfBits(dividendMantissa);
			}
			else
			{
				quotientPower2 += Unsigned.numberOfBits(divisorMantissa);
			}
		}

		final int decimals = Math.max(dividendDecimals, divisorDecimals);
		final long scaling = Powers10[decimals + Math.abs(dividendDecimals - divisorDecimals)];
		quotientPower2 += Unsigned.numberOfBits(scaling);

		final long quotient;
		if (quotientPower2 < Long.SIZE)
		{
			quotient = roundedDivide(dividendMantissa, dividendDecimals, divisorMantissa,
					divisorDecimals, inMode);
		}
		else
		{
			quotient = roundedDivide128(dividendMantissa, dividendDecimals, divisorMantissa,
					divisorDecimals, inMode);
		}
		return encode(quotient, -decimals);
	}

	/**
	 * Returns a decimal flyweight rounded according to the decimal rounding
	 * mode.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @param inMode decimal rounding mode
	 * @return decimal flyweight according to the rounding mode or NAN
	 * indicating overflow
	 */
	public static long round(	final long inDecimalFlyweight,
								final int inDecimals,
								final DecimalRounding inMode)
	{
		if (isNaN(inDecimalFlyweight) || inDecimals < 0 || inDecimals > DECIMALS_MAX)
		{
			return NAN;
		}

		final int decimals = -exponent(inDecimalFlyweight);
		if (decimals == inDecimals)
		{
			return inDecimalFlyweight;
		}

		final long fixedScale = valueOf(Powers10[Math.abs(decimals - inDecimals)], 0);
		long rounded = 0;
		if (decimals > inDecimals)
		{
			rounded = divide(inDecimalFlyweight, fixedScale, inMode);
		}
		else
		{
			rounded = multiply(inDecimalFlyweight, fixedScale, inMode);
		}

		return encode(mantissa(rounded), -inDecimals);
	}

	/**
	 * Returns the absolute value of the decimal flyweight.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @return the absolute value its argument
	 */
	public static long abs(final long inDecimalFlyweight)
	{
		if (isNaN(inDecimalFlyweight) || inDecimalFlyweight >= 0)
		{
			return inDecimalFlyweight;
		}

		return encode(-mantissa(inDecimalFlyweight), exponent(inDecimalFlyweight));
	}

	/**
	 * Returns the value of the specified number as a byte, which may involve
	 * rounding or truncation.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @return byte value
	 */
	public static byte byteValue(final long inDecimalFlyweight)
	{
		return (byte) longValue(inDecimalFlyweight);
	}

	/**
	 * Returns the value of the specified number as a short, which may involve
	 * rounding or truncation.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @return short value
	 */
	public static short shortValue(final long inDecimalFlyweight)
	{
		return (short) longValue(inDecimalFlyweight);
	}

	/**
	 * Returns the value of the specified number as an integer, which may
	 * involve rounding or truncation.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @return integer value
	 */
	public static int intValue(final long inDecimalFlyweight)
	{
		return (int) longValue(inDecimalFlyweight);
	}

	/**
	 * Returns the value of the specified number as an integer, which may
	 * involve rounding or truncation.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @return long value or NAN indicating overflow
	 */
	public static long longValue(final long inDecimalFlyweight)
	{
		if (isNaN(inDecimalFlyweight))
		{
			return NAN;
		}

		return mantissa(round(inDecimalFlyweight, 0, DecimalRounding.UP));
	}

	/**
	 * Returns the value of the specified number as a float, which may involve
	 * rounding.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @return float value or NAN indicating overflow
	 */
	public static float floatValue(long inDecimalFlyweight)
	{
		if (isNaN(inDecimalFlyweight))
		{
			return Float.NaN;
		}

		final long mantissa = mantissa(inDecimalFlyweight);
		final int exponent = exponent(inDecimalFlyweight);

		return Float.valueOf(mantissa) / Powers10[-exponent];
	}

	/**
	 * Returns the value of the specified number as a double, which may involve
	 * rounding.
	 * @param inDecimalFlyweight decimal flyweight value
	 * @return double value or NAN indicating overflow
	 */
	public static double doubleValue(long inDecimalFlyweight)
	{
		if (isNaN(inDecimalFlyweight))
		{
			return Double.NaN;
		}

		final long mantissa = mantissa(inDecimalFlyweight);
		final int exponent = exponent(inDecimalFlyweight);

		return Double.valueOf(mantissa) / Powers10[-exponent];
	}

	/**
	 * Returns the product of two decimal flyweight values rounded according to
	 * the rounding mode.
	 * @param inValueMantissa scaled mantissa of the value
	 * @param inValueDecimals unsigned decimal count of the value
	 * @param inFactorMantissa scaled mantissa of the factor
	 * @param inFactorDecimals unsigned decimal count of the factor
	 * @return
	 */
	private static long roundedMultiply(final long inValueMantissa,
										final int inValueDecimals,
										final long inFactorMantissa,
										final int inFactorDecimals,
										final DecimalRounding inMode)
	{
		final int decimals = Math.max(inValueDecimals, inFactorDecimals);
		long product = Math.abs(inValueMantissa * inFactorMantissa);

		if (inValueDecimals != inFactorDecimals)
		{
			product *= Powers10[Math.abs(inValueDecimals - inFactorDecimals)];
		}

		final long scale = Powers10[decimals];
		final long rounding = scale >>> 1;

		if (inMode == DecimalRounding.UP)
		{
			product += rounding;
		}
		product /= scale;
		if ((inValueMantissa < 0) != (inFactorMantissa < 0))
		{
			product = -product;
		}
		return product;
	}

	/**
	 * Returns the product of two decimal flyweight values rounded according to
	 * the rounding mode.
	 * @param inValueMantissa scaled mantissa of the value
	 * @param inValueDecimals unsigned decimal count of the value
	 * @param inFactorMantissa scaled mantissa of the factor
	 * @param inFactorDecimals unsigned decimal count of the factor
	 * @return
	 */
	private static long roundedMultiply128(	final long inValueMantissa,
											final int inValueDecimals,
											final long inFactorMantissa,
											final int inFactorDecimals,
											final DecimalRounding inMode)
	{
		final MutableUnsigned128 product = new MutableUnsigned128(Math.abs(inValueMantissa));
		final MutableUnsigned128 factor = new MutableUnsigned128(Math.abs(inFactorMantissa));
		final long scaleDiff = Powers10[Math.abs(inValueDecimals - inFactorDecimals)];

		if (inValueDecimals != inFactorDecimals)
		{
			product.multiply(scaleDiff);
		}
		product.multiply(factor);

		final long scale = Powers10[Math.max(inValueDecimals, inFactorDecimals)];
		final MutableUnsigned128 rounding = new MutableUnsigned128(scale).shiftRight(1);

		if (rounding.isZero())
		{
			product.divide(scale);
		}
		else
		{
			final MutableUnsigned128 remainder = new MutableUnsigned128();

			new MutableUnsigned128(product).divide(rounding, remainder);
			if (remainder.isZero())
			{
				product.divide(new MutableUnsigned128(scale), remainder);
				if (remainder.compareTo(rounding) == 0 && inMode == DecimalRounding.UP)
				{
					product.increment();
				}
			}
			else
			{
				if (inMode == DecimalRounding.UP)
				{
					product.add(rounding);
				}
				product.divide(scale);
			}
		}
		return mantissaValue(product, inValueMantissa < 0 != inFactorMantissa < 0);
	}

	/**
	 * Returns the quotient of its arguments.
	 * @param inDividendMantissa scaled dividend mantissa
	 * @param inDividendDecimals number of dividend decimals
	 * @param inDivisorMantissa scaled divisor mantissa
	 * @param inDivisorDecimals number of divisor decimals
	 * @param inMode decimal rounding mode
	 * @return signed 64-bit quotient
	 */
	private static long roundedDivide(	final long inDividendMantissa,
										final int inDividendDecimals,
										final long inDivisorMantissa,
										final int inDivisorDecimals,
										final DecimalRounding inMode)
	{
		long quotientMantissa = Math.abs(inDividendMantissa);
		long divisorMantissa = Math.abs(inDivisorMantissa);
		int decimals = Math.max(inDivisorDecimals, inDividendDecimals);

		if (inDividendDecimals != inDivisorDecimals)
		{
			final long scale = Powers10[Math.abs(inDividendDecimals - inDivisorDecimals)];
			if (inDividendDecimals < inDivisorDecimals)
			{
				quotientMantissa *= scale;
			}
			else
			{
				divisorMantissa *= scale;
			}
		}
		quotientMantissa *= Powers10[decimals];

		final long rounding = divisorMantissa >>> 1;
		if (rounding == 0)
		{
			quotientMantissa /= divisorMantissa;
		}
		else
		{
			if (quotientMantissa % rounding == 0)
			{
				final long remainder = quotientMantissa % divisorMantissa;
				quotientMantissa /= divisorMantissa;
				if (remainder == rounding && inMode == DecimalRounding.UP)
				{
					++quotientMantissa;
				}
			}
			else
			{
				if (inMode == DecimalRounding.UP)
				{
					quotientMantissa += rounding;
				}
				quotientMantissa /= divisorMantissa;
			}
		}
		if ((inDivisorMantissa < 0) != (inDividendMantissa < 0))
		{
			quotientMantissa = -quotientMantissa;
		}

		return quotientMantissa;
	}

	/**
	 * Returns the quotient of its arguments.
	 * @param inDividendMantissa scaled dividend mantissa
	 * @param inDividendDecimals number of dividend decimals
	 * @param inDivisorMantissa scaled divisor mantissa
	 * @param inDivisorDecimals number of divisor decimals
	 * @param inMode decimal rounding mode
	 * @return signed 64-bit quotient
	 */
	private static long roundedDivide128(	final long inDividendMantissa,
											final int inDividendDecimals,
											final long inDivisorMantissa,
											final int inDivisorDecimals,
											final DecimalRounding inMode)
	{
		final MutableUnsigned128 quotient = new MutableUnsigned128(Math.abs(inDividendMantissa));
		final MutableUnsigned128 divisor = new MutableUnsigned128(Math.abs(inDivisorMantissa));

		if (inDividendDecimals != inDivisorDecimals)
		{
			final long scale = Powers10[Math.abs(inDividendDecimals - inDivisorDecimals)];
			if (inDividendDecimals < inDivisorDecimals)
			{
				quotient.multiply(scale);
			}
			else
			{
				divisor.multiply(scale);
			}
		}
		quotient.multiply(Powers10[Math.max(inDividendDecimals, inDivisorDecimals)]);

		final MutableUnsigned128 rounding = new MutableUnsigned128(divisor).shiftRight(1);
		if (rounding.isZero())
		{
			quotient.divide(divisor);
		}
		else
		{
			final MutableUnsigned128 remainder = new MutableUnsigned128();
			new MutableUnsigned128(quotient).divide(rounding, remainder);
			if (remainder.isZero())
			{
				quotient.divide(divisor, remainder);
				if (remainder.compareTo(rounding) == 0 && inMode == DecimalRounding.UP)
				{
					quotient.increment();
				}
			}
			else
			{
				if (inMode == DecimalRounding.UP)
				{
					quotient.add(rounding);
				}
				quotient.divide(divisor);
			}
		}

		return mantissaValue(quotient, inDividendMantissa < 0 != inDivisorMantissa < 0);
	}

	/**
	 * Returns an encoded decimal flyweight, but preserves error values.
	 * @param inScaledMantissa scaled mantissa
	 * @param inExponent exponent
	 * @return decimal flyweight value or NAN indicating overflow.
	 */
	private static long encode(final long inScaledMantissa, final int inExponent)
	{
		if (inScaledMantissa >= MANTISSA_MIN && inScaledMantissa <= MANTISSA_MAX)
		{
			return (inScaledMantissa << DECIMAL_BITS) | (-inExponent & DECIMAL_MASK);
		}
		else
		{
			return NAN;
		}
	}

	/**
	 * Returns a mantissa value checked for overflow
	 * @param inValue 128-bit unsigned value
	 * @param inSigned sign indicator
	 * @return verified mantissa or MANTISSA_ERROR indicating overflow
	 */
	private static long mantissaValue(final MutableUnsigned128 inValue, final boolean inSigned)
	{
		long mantissa = MANTISSA_ERROR;
		if (inValue.fitsLong())
		{
			mantissa = inValue.longValue();
			if (mantissa >= MANTISSA_MIN && mantissa <= MANTISSA_MAX)
			{
				if (inSigned)
				{
					mantissa = -mantissa;
				}
			}
			else
			{
				mantissa = MANTISSA_ERROR;
			}
		}
		return mantissa;
	}

	/**
	 * This code is adapted from the Long class with minimal changes.
	 * @param i long value
	 * @param index position of last character
	 * @param buf output character buffer
	 * @return position of first character
	 */
	private static int getChars(long i, int index, char[] buf)
	{
		long q;
		int r;
		int charPos = index;

		// Get 2 digits/iteration using longs until quotient fits into an int
		while (i > Integer.MAX_VALUE)
		{
			q = i / 100;
			// really: r = i - (q * 100);
			r = (int) (i - ((q << 6) + (q << 5) + (q << 2)));
			i = q;
			buf[--charPos] = DigitOnes[r];
			buf[--charPos] = DigitTens[r];
		}

		// Get 2 digits/iteration using integers
		int q2;
		int i2 = (int) i;
		while (i2 >= 65536)
		{
			q2 = i2 / 100;
			// really: r = i2 - (q * 100);
			r = i2 - ((q2 << 6) + (q2 << 5) + (q2 << 2));
			i2 = q2;
			buf[--charPos] = DigitOnes[r];
			buf[--charPos] = DigitTens[r];
		}

		// Fall thru to fast mode for smaller numbers
		// assert(i2 <= 65536, i2);
		for (;;)
		{
			q2 = (i2 * 52429) >>> (16 + 3);
			r = i2 - ((q2 << 3) + (q2 << 1)); // r = i2-(q2*10) ...
			buf[--charPos] = (char) (r + '0');
			i2 = q2;
			if (i2 == 0)
				break;
		}

		return charPos;
	}

	private static final long[] Powers10 =
	{
			1L, // 10^0
			10L, // 10^1
			100L, // 10^2
			1000L, // 10^3
			10000L, // 10^4
			100000L, // 10^5
			1000000L, // 10^6
			10000000L, // 10^7
			100000000L, // 10^8
			1000000000L, // 10^9
			10000000000L, // 10^10
			100000000000L, // 10^11
			1000000000000L, // 10^12
			10000000000000L, // 10^13
			100000000000000L, // 10^14
			1000000000000000L, // 10^15
			10000000000000000L, // 10^16
			100000000000000000L, // 10^17
			1000000000000000000L, // 10^18
			// 9223372036854775807L
	};

	private final static char[] DigitTens =
	{
			'0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '1', '1', '1', '1', '1', '1',
			'1', '1', '1', '2', '2', '2', '2', '2', '2', '2', '2', '2', '2', '3', '3', '3', '3',
			'3', '3', '3', '3', '3', '3', '4', '4', '4', '4', '4', '4', '4', '4', '4', '4', '5',
			'5', '5', '5', '5', '5', '5', '5', '5', '5', '6', '6', '6', '6', '6', '6', '6', '6',
			'6', '6', '7', '7', '7', '7', '7', '7', '7', '7', '7', '7', '8', '8', '8', '8', '8',
			'8', '8', '8', '8', '8', '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
	};

	private final static char[] DigitOnes =
	{
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6',
			'7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3',
			'4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0',
			'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7',
			'8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '1', '2', '3', '4',
			'5', '6', '7', '8', '9', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
	};
}
