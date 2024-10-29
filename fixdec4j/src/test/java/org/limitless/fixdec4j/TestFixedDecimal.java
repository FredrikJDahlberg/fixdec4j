package org.limitless.fixdec4j;

import jdk.jfr.Description;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.TestWatcher;

import static org.junit.jupiter.api.Assertions.*;
//import static org.junit.jupiter.api.Rule;

public class TestFixedDecimal
{
	/*
	@Rule
	public TestWatcher watcher = new TestWatcher()
	{
		@Override
		protected void starting(Description description)
		{
			System.out.println("Test: " + description.getMethodName() + " Started");
		}

		@Override
		protected void failed(Throwable e, Description description)
		{
			System.out.println("      " + description.getMethodName() + " failed");
		}

		@Override
		protected void succeeded(Description description)
		{
			System.out.println("      " + description.getMethodName() + " OK");
		}
	};
*/
	private static long of(final long mantissa, final int exponent)
	{
		return DecimalFlyweight.valueOf(mantissa, exponent);
	}

	private static void add(final long a, final long b, final long c)
	{
		System.out.format("%de%d, %s + %s = %s%n", DecimalFlyweight.mantissa(c),
				DecimalFlyweight.exponent(c), DecimalFlyweight.toString(a),
				DecimalFlyweight.toString(b), DecimalFlyweight.toString(c));
		final long r = DecimalFlyweight.add(a, b);

		assertEquals(r, c, "Expected " + DecimalFlyweight.toString(c) + ", Value="
			+ DecimalFlyweight.toString(r));

		final MutableDecimal a1 = MutableDecimal.valueOf(DecimalFlyweight.mantissa(a),
				DecimalFlyweight.exponent(a));
		final MutableDecimal b1 = MutableDecimal.valueOf(DecimalFlyweight.mantissa(b),
				DecimalFlyweight.exponent(b));
		a1.add(b1);
		assertEquals(a1.toLongBits(), c, "Expected " + DecimalFlyweight.toString(c) + ", Value=" + a1);

		final Decimal a2 = Decimal.valueOf(DecimalFlyweight.mantissa(a),
				DecimalFlyweight.exponent(a));
		final Decimal b2 = Decimal.valueOf(DecimalFlyweight.mantissa(b),
				DecimalFlyweight.exponent(b));
		final Decimal c2 = a2.add(b2);
		final long f2 = of(c2.mantissa(), c2.exponent());
		assertEquals(f2, c, "Expected " + DecimalFlyweight.toString(c) + ", Value=" + c2);
	}

	private static void subtract(final long a, final long b, final long c)
	{
		System.out.format("%de%d, %s - %s = %s%n", DecimalFlyweight.mantissa(c),
				DecimalFlyweight.exponent(c), DecimalFlyweight.toString(a),
				DecimalFlyweight.toString(b), DecimalFlyweight.toString(c));
		final long r = DecimalFlyweight.subtract(a, b);
		assertEquals(r, c, "Expected " + DecimalFlyweight.toString(c) + ", Value="
			+ DecimalFlyweight.toString(r));

		final MutableDecimal a1 = MutableDecimal.valueOf(DecimalFlyweight.mantissa(a),
				DecimalFlyweight.exponent(a));
		final MutableDecimal b1 = MutableDecimal.valueOf(DecimalFlyweight.mantissa(b),
				DecimalFlyweight.exponent(b));
		a1.subtract(b1);
		assertEquals(a1.toLongBits(), c, "Expected " + DecimalFlyweight.toString(c) + ", Value=" + a1);

		final Decimal a2 = Decimal.valueOf(DecimalFlyweight.mantissa(a),
				DecimalFlyweight.exponent(a));
		final Decimal b2 = Decimal.valueOf(DecimalFlyweight.mantissa(b),
				DecimalFlyweight.exponent(b));
		final Decimal c2 = a2.subtract(b2);
		final long f2 = of(c2.mantissa(), c2.exponent());
		assertEquals(f2, c, "Expected " + DecimalFlyweight.toString(c) + ", Value=" + c2);
	}

	private static void multiply(final long a, final long b, final long c)
	{
		System.out.format("%de%d, %s * %s = %s%n", DecimalFlyweight.mantissa(c),
				DecimalFlyweight.exponent(c), DecimalFlyweight.toString(a),
				DecimalFlyweight.toString(b), DecimalFlyweight.toString(c));
		final long r = DecimalFlyweight.multiply(a, b, DecimalRounding.UP);
		assertEquals(r, c, "Expected " + DecimalFlyweight.toString(c) + ", Value="
			+ DecimalFlyweight.toString(r));

		final MutableDecimal a1 = MutableDecimal.valueOf(DecimalFlyweight.mantissa(a),
				DecimalFlyweight.exponent(a));
		final MutableDecimal b1 = MutableDecimal.valueOf(DecimalFlyweight.mantissa(b),
				DecimalFlyweight.exponent(b));
		a1.multiply(b1);
		assertEquals(a1.toLongBits(),c, "Expected " + DecimalFlyweight.toString(c) + ", Value=" + a1);

		final Decimal a2 = Decimal.valueOf(DecimalFlyweight.mantissa(a),
				DecimalFlyweight.exponent(a));
		final Decimal b2 = Decimal.valueOf(DecimalFlyweight.mantissa(b),
				DecimalFlyweight.exponent(b));
		final Decimal c2 = a2.multiply(b2);
		final long f2 = of(c2.mantissa(), c2.exponent());
		assertEquals(f2, c, "Expected " + DecimalFlyweight.toString(c) + ", Value=" + c2);
	}

	private static void divide(final long a, final long b, final long c)
	{
		System.out.format("%de%d, %s / %s = %s%n", DecimalFlyweight.mantissa(c),
				DecimalFlyweight.exponent(c), DecimalFlyweight.toString(a),
				DecimalFlyweight.toString(b), DecimalFlyweight.toString(c));
		final long r = DecimalFlyweight.divide(a, b, DecimalRounding.UP);
		assertEquals(r, c, "Expected " + DecimalFlyweight.toString(c) + ", Value="
			+ DecimalFlyweight.toString(r));

		final MutableDecimal a1 = MutableDecimal.valueOf(DecimalFlyweight.mantissa(a),
				DecimalFlyweight.exponent(a));
		final MutableDecimal b1 = MutableDecimal.valueOf(DecimalFlyweight.mantissa(b),
				DecimalFlyweight.exponent(b));
		a1.divide(b1);
		assertEquals(a1.toLongBits(),c, "Expected " + DecimalFlyweight.toString(c) + ", Value=" + a1);

		final Decimal a2 = Decimal.valueOf(DecimalFlyweight.mantissa(a),
				DecimalFlyweight.exponent(a));
		final Decimal b2 = Decimal.valueOf(DecimalFlyweight.mantissa(b),
				DecimalFlyweight.exponent(b));
		final Decimal c2 = a2.divide(b2);
		final long f2 = of(c2.mantissa(), c2.exponent());
		assertEquals(f2, c, "Expected " + DecimalFlyweight.toString(c) + ", Value=" + c2);
	}

	private static void parse(final long f, final String s)
	{
		System.out.format("%de%d, parse(%s) = %s%n", DecimalFlyweight.mantissa(f),
				DecimalFlyweight.exponent(f), s, DecimalFlyweight.toString(f));
		final long r = DecimalFlyweight.valueOf(s);
		assertEquals(f, r, "Expected " + DecimalFlyweight.toString(f) + ", Value="
			+ DecimalFlyweight.toString(r));
	}

	@Test
	public void checkConstruction()
	{
        assertNotEquals(DecimalFlyweight.NAN, 0);

		final MutableDecimal decimal = new MutableDecimal();
		assertEquals(0, decimal.mantissa());
		assertEquals(0, decimal.exponent());

		final MutableDecimal decimal1 = new MutableDecimal(12050, -2);
		assertEquals(12050, decimal1.mantissa());
		assertEquals(-2, decimal1.exponent());

		final MutableDecimal decimal2 = new MutableDecimal(-102, 2);
		assertEquals(-10200, decimal2.mantissa());
		assertEquals(0, decimal2.exponent());

		final MutableDecimal decimal3 = new MutableDecimal(-1080, -4);
		assertEquals(-1080, decimal3.mantissa());
		assertEquals(-4, decimal3.exponent());

		final MutableDecimal decimal4 = new MutableDecimal(10008, -7);
		assertEquals(10008, decimal4.mantissa());
		assertEquals(-7, decimal4.exponent());

		final MutableDecimal decimal5 = new MutableDecimal(1, 5);
		assertEquals(100_000, decimal5.mantissa());
		assertEquals(0, decimal5.exponent());

		assertTrue(new MutableDecimal(1, 50).isNaN());
		final MutableDecimal value = new MutableDecimal().fromLongBits(0);
		assertEquals(MutableDecimal.ZERO, value);
		assertEquals(Decimal.ZERO, Decimal.fromLongBits(0));
		assertTrue(new Decimal(1, 50).isNaN());

		assertTrue(Decimal.ZERO.equals(new Decimal(Decimal.ZERO)));
		assertTrue(Decimal.NAN.equals(new Decimal(Decimal.NAN)));
		
		assertTrue(MutableDecimal.ZERO.equals(new MutableDecimal(MutableDecimal.ZERO)));
		assertTrue(MutableDecimal.NAN.equals(new MutableDecimal(MutableDecimal.NAN)));
		
		assertEquals(DecimalFlyweight.NAN, DecimalFlyweight.valueOf(1, -8));
		assertEquals(DecimalFlyweight.NAN, DecimalFlyweight.valueOf(DecimalFlyweight.MANTISSA_MAX, 3));
	}

	/*
	@Ignore
	@Test
	public void checkIsValid()
	{
		assertEquals(100_00000, DecimalFlyweight.mantissa(DecimalFlyweight.valueOf(100, 5)));
		assertEquals(-100_00000, DecimalFlyweight.mantissa(DecimalFlyweight.valueOf(-100, 5)));
		assertEquals(100_00000, DecimalFlyweight.mantissa(DecimalFlyweight.valueOf(100_00000, -5)));
		assertEquals(-100_00000, DecimalFlyweight.mantissa(DecimalFlyweight.valueOf(-100_00000, -5)));
	}
*/
	@Test
	public void checkAddition()
	{
		add(of(DecimalFlyweight.MANTISSA_MIN+1, 0), of(-1, 0), of(DecimalFlyweight.MANTISSA_MIN, 0));
		add(of(DecimalFlyweight.MANTISSA_MAX, 0), of(DecimalFlyweight.MANTISSA_MIN, 0), of(1, 0));
		add(of(DecimalFlyweight.MANTISSA_MAX, 0), of(DecimalFlyweight.MANTISSA_MAX, 0), DecimalFlyweight.NAN);
		add(of(DecimalFlyweight.MANTISSA_MAX, 0), of(1, 0), DecimalFlyweight.NAN);
		add(of(DecimalFlyweight.MANTISSA_MIN, 0), of(-1, 0), DecimalFlyweight.NAN);
		
		add(of(DecimalFlyweight.MANTISSA_MAX-1, 0), of(1, 0), of(DecimalFlyweight.MANTISSA_MAX, 0));

		add(of(DecimalFlyweight.MANTISSA_MAX/2, 0), of(DecimalFlyweight.MANTISSA_MAX/2, 0),
				of(DecimalFlyweight.MANTISSA_MAX/2*2, 0));

		add(of(100_000_000_000_000000L, -6), of(200_000_000_000_0000L, -4), of(300_000_000_000_000000L, -6));
		add(of(-100_000_000_000_000000L, -6), of(200_000_000_000_0000L, -4), of(100_000_000_000_000000L, -6));
		add(of(100_000_000_000_000000L, -6), of(-200_000_000_000_0000L, -4), of(-100_000_000_000_000000L, -6));
		add(of(-100_000_000_000_000000L, -6), of(-200_000_000_000_0000L, -4), of(-300_000_000_000_000000L, -6));
		
		add(of(100_000_000_000L, 0), of(200_000_000_000_0000L, -4), of(300_000_000_000_0000L, -4));
		add(of(-100_000_000_000L, 0), of(200_000_000_000_0000L, -4), of(100_000_000_000_0000L, -4));
		add(of(100_000_000_000L, 0), of(-200_000_000_000_0000L, -4), of(-100_000_000_000_0000L, -4));
		add(of(-100_000_000_000L, 0), of(-200_000_000_000_0000L, -4), of(-300_000_000_000_0000L, -4));

		add(of(1, 2), of(1, 0), of(101, 0));
		add(of(1, 2), of(-1, 0), of(99, 0));
		add(of(10_00, -2), of(5, -1), of(10_50, -2));
		add(of(10_00, -2), of(-50, -4), of(9_9950, -4));
		add(DecimalFlyweight.NAN, DecimalFlyweight.valueOf(3, 0), DecimalFlyweight.NAN);
		add(DecimalFlyweight.valueOf(100, -2), DecimalFlyweight.NAN, DecimalFlyweight.NAN);
	}

	@Test
	public void checkSubtraction()
	{
		subtract(DecimalFlyweight.NAN, of(1, 2), DecimalFlyweight.NAN);
		subtract(of(1, 2), DecimalFlyweight.NAN, DecimalFlyweight.NAN);
		
		subtract(of(1, 2), of(1, 0), of(99, 0));
		subtract(of(10_00, -2), of(50, -4), of(9_9950, -4));
		subtract(of(103, -2), of(41, -2), of(62, -2));
		subtract(of(-284375, -6), of(-11663, -6), of(-272712, -6));
	}

	@Test
	public void checkUnaryMinus()
	{
		assertEquals(DecimalFlyweight.NAN, DecimalFlyweight.minus(DecimalFlyweight.NAN));
		assertEquals(DecimalFlyweight.valueOf(-1, 2), DecimalFlyweight.minus(DecimalFlyweight.valueOf(1, 2)));
		assertEquals(DecimalFlyweight.valueOf(1, 2), DecimalFlyweight.minus(DecimalFlyweight.valueOf(-1, 2)));

		assertEquals(Decimal.valueOf(-1, 2), Decimal.valueOf(1, 2).minus());
		assertEquals(Decimal.valueOf(1, 2), Decimal.valueOf(-1, 2).minus());
		assertEquals(Decimal.NAN, Decimal.NAN.minus());

		assertEquals(MutableDecimal.valueOf(-1, 2), MutableDecimal.valueOf(1, 2).minus());
		assertEquals(MutableDecimal.valueOf(1, 2), MutableDecimal.valueOf(-1, 2).minus());
		assertEquals(MutableDecimal.NAN, MutableDecimal.NAN.minus());

	}

	@Test
	public void checkMultiplication()
	{
		multiply(DecimalFlyweight.NAN, 0, DecimalFlyweight.NAN);

		multiply(of(2, -2), of(1, -2), of(0, -2));

		multiply(of(435, 0), of(1, 2), of(435_00, 0));
		multiply(of(435, -2), of(1, 2), of(435_00, -2));
		multiply(of(-435, -2), of(1, 2), of(-435_00, -2));
		multiply(of(4897, -3), of(1, -1), of(490, -3));
		multiply(of(-4897, -3), of(1, -1), of(-490, -3));
		multiply(of(4897, -3), of(-1, -1), of(-490, -3));

		multiply(of(1300_5000, -4), of(1, -4), of(1301, -4));

		multiply(of(DecimalFlyweight.MANTISSA_MAX / 100, -1), of(10_0, -1),
				of(DecimalFlyweight.MANTISSA_MAX / 100 * 10, -1));
		multiply(of(-DecimalFlyweight.MANTISSA_MAX / 100, -1), of(10_0, -1),
				of(-DecimalFlyweight.MANTISSA_MAX / 100 * 10, -1));

		multiply(of(5_000_000_000_000L, 0), of(400_000, 0), of(2_000_000_000_000_000_000L, 0));

		multiply(of(100_000_000_000_000L, -3), of(1_123456, -6), of(112_345_600_000_000000L, -6));
		multiply(of(100_000_000_000_0L, -1), of(1_123, -3), of(112_300_000_000_000L, -3));
		multiply(of(100_000_000_000_5L, -1), of(1_123, -3), of(112_300_000_000_562L, -3));

		multiply(of(100_000_000_400000L, -6), of(1_123455, -6), of(112_345_500_449382L, -6));
		multiply(of(100_000_000_500000L, -6), of(1_123455, -6), of(112_345_500_561728L, -6));
		multiply(of(100_000_000_600000L, -6), of(1_123455, -6), of(112_345_500_674073L, -6));

		multiply(of(100_000_000_123456L, -6), of(1_125555, -6), of(112_555_500_138957L, -6));

		multiply(of(DecimalFlyweight.MANTISSA_MAX, 0), of(1, 0),
				of(DecimalFlyweight.MANTISSA_MAX, 0));

		multiply(of(4_897, -3), of(100, -3), of(490, -3));
		multiply(of(4_897, -3), of(10_000, -3), of(48_970, -3));
		multiply(of(-4_897, -3), of(100, -3), of(-490, -3));
		multiply(of(-4_897, -3), of(10_000, -3), of(-48_970, -3));

		multiply(of(4_895, -3), of(100, -3), of(490, -3));
		multiply(of(4_895, -3), of(10_000, -3), of(48_950, -3));
		multiply(of(-4_895, -3), of(100, -3), of(-490, -3));
		multiply(of(-4_895, -3), of(10_000, -3), of(-48_950, -3));
	}

	@Test
	public void checkDivision()
	{
		divide(of(10, 0), 0, DecimalFlyweight.NAN);
		divide(of(10, 0), of(0, 3), DecimalFlyweight.NAN);
		divide(DecimalFlyweight.NAN, of(1, 0), DecimalFlyweight.NAN);
		divide(of(1, 0), DecimalFlyweight.NAN, DecimalFlyweight.NAN);

		divide(of(12_000005, -6), of(10_000000, -6), of(1_200001, -6));
		divide(of(-12_000005, -6), of(10_000000, -6), of(-1_200001, -6));
		divide(of(12_000004, -6), of(10_000000, -6), of(1_200000, -6));
		divide(of(12_000004, -6), of(-10_000000, -6), of(-1_200000, -6));

		divide(of(100, 0), of(20, 0), of(5, 0));

		divide(of(272712, -6), of(29167, -6), of(9_350019, -6)); // -9.350018856927349
		divide(of(-272712, -6), of(29167, -6), of(-9_350019, -6)); // -9.350018856927349

		divide(of(1, -6), of(2, -6), of(500000, -6));

		divide(of(4897, -3), of(10, 0), of(490, -3));

		divide(of(4897, -3), of(10_000, -3), of(490, -3));
		divide(of(-4897, -3), of(10_000, -3), of(-490, -3));
		divide(of(4897, -3), of(-10_000, -3), of(-490, -3));
		divide(of(-4897, -3), of(-10_000, -3), of(490, -3));

		divide(of(87_000_000_000_0000L, -4), of(100_000_000_000_0000L, -4), of(8700, -4));
		divide(of(90_000_000_000_0000L, -4), of(15_000_000_000_0000L, -4), of(6_0000, -4));

		divide(of(1, 0), of(2, 0), of(1, 0));
		divide(of(1, 0), of(20, -1), of(5, -1));

		divide(of(DecimalFlyweight.MANTISSA_MAX, 0), of(1, 0),
				of(DecimalFlyweight.MANTISSA_MAX, 0));
		divide(of(DecimalFlyweight.MANTISSA_MAX, -7), of(1, -1), DecimalFlyweight.NAN);
		divide(of(DecimalFlyweight.MANTISSA_MAX, -6), of(1, -2), DecimalFlyweight.NAN);
		divide(of(DecimalFlyweight.MANTISSA_MAX, -5), of(1, -3), DecimalFlyweight.NAN);
		divide(of(10_000, -3), of(1, -3), of(10_000_000, -3));

		divide(of(DecimalFlyweight.MANTISSA_MIN / 10, -1), of(1, -1),
				of(DecimalFlyweight.MANTISSA_MIN / 10 * 10, -1));
		divide(of(DecimalFlyweight.MANTISSA_MIN / 100, -2), of(1, -1),
				of(DecimalFlyweight.MANTISSA_MIN / 100 * 10, -2));
		divide(of(DecimalFlyweight.MANTISSA_MIN / 10, -1), of(1, -1),
				of(DecimalFlyweight.MANTISSA_MIN / 10 * 10, -1));
		divide(of(DecimalFlyweight.MANTISSA_MIN / 100, -2), of(1, -1),
				of(DecimalFlyweight.MANTISSA_MIN / 100 * 10, -2));

		divide(of(DecimalFlyweight.MANTISSA_MAX, -3), of(1, -5), DecimalFlyweight.NAN);
		divide(of(DecimalFlyweight.MANTISSA_MAX, -2), of(1, -6), DecimalFlyweight.NAN);
		divide(of(DecimalFlyweight.MANTISSA_MAX, -1), of(1, -7), DecimalFlyweight.NAN);

		divide(of(4897, -3), of(10, 0), of(490, -3));
		divide(of(-4897, -3), of(10, 0), of(-490, -3));
		divide(of(4897, -3), of(100, -3), of(48_970, -3));
		divide(of(-4897, -3), of(100, -3), of(-48_970, -3));

		divide(of(4895, -3), of(10, 0), of(490, -3));
		divide(of(-4895, -3), of(10, 0), of(-490, -3));
		divide(of(4895, -3), of(100, -3), of(48_950, -3));
		divide(of(-4895, -3), of(100, -3), of(-48_950, -3));

		divide(of(233336, -6), of(29167, -6), of(8_000000, -6));
	}

	@Test
	public void checkMantissaAndExponent()
	{
		assertEquals(100_50, DecimalFlyweight.mantissa(of(100_50, -2)));
		assertEquals(110_50, DecimalFlyweight.mantissa(of(110_50, -2)));
		assertEquals(DecimalFlyweight.NAN, of(DecimalFlyweight.MANTISSA_ERROR, 0));
		assertEquals(-2, DecimalFlyweight.exponent(of(100_50, -2)));
		assertEquals(0, DecimalFlyweight.exponent(of(100_50, 5)));
	}

	@Test
	public void checkCompareTo()
	{
		assertEquals(1, new Decimal(0, 0).compareTo(null));
		assertEquals(-1, new Decimal(-1, 0).compareTo(Decimal.ZERO));
		assertEquals(0, new Decimal(0, 0).compareTo(Decimal.ZERO));
		assertEquals(1, new Decimal(1, 0).compareTo(Decimal.ZERO));
		
		assertEquals(1, new MutableDecimal(0, 0).compareTo(null));
		assertEquals(-1, new MutableDecimal(-1, 0).compareTo(MutableDecimal.ZERO));
		assertEquals(0, new MutableDecimal(0, 0).compareTo(MutableDecimal.ZERO));
		assertEquals(1, new MutableDecimal(1, 0).compareTo(MutableDecimal.ZERO));
		
		assertEquals(1, DecimalFlyweight.compareTo(0, DecimalFlyweight.NAN));
		assertEquals(0, DecimalFlyweight.compareTo(DecimalFlyweight.NAN, DecimalFlyweight.NAN));

		assertEquals(0, DecimalFlyweight.compareTo(of(0, 0), of(0, 0)));
		assertEquals(-1, DecimalFlyweight.compareTo(of(100, 0), of(200, 0)));
		assertEquals(-1, DecimalFlyweight.compareTo(of(100, -1), of(200, -1)));
		assertEquals(-1, DecimalFlyweight.compareTo(of(-100, 0), of(-20, 0)));
		assertEquals(-1, DecimalFlyweight.compareTo(of(-100, -1), of(-20, -1)));
		assertEquals(-1, DecimalFlyweight.compareTo(of(100, -3), of(20, -1)));
		assertEquals(-1, DecimalFlyweight.compareTo(of(-100, -3), of(20, -1)));

		assertEquals(1, DecimalFlyweight.compareTo(of(300, 0), of(200, 0)));
		assertEquals(1, DecimalFlyweight.compareTo(of(300, -1), of(200, -1)));
		assertEquals(1, DecimalFlyweight.compareTo(of(-100, 0), of(-200, 0)));
		assertEquals(1, DecimalFlyweight.compareTo(of(-100, -1), of(-200, -1)));
		assertEquals(1, DecimalFlyweight.compareTo(of(-100, -4), of(-20, -2)));

		assertEquals(-1, DecimalFlyweight.compareTo(of(1, 2), of(1, 3)));
		assertEquals(1, DecimalFlyweight.compareTo(of(123_000, -3), of(1, 2)));
		assertEquals(1, DecimalFlyweight.compareTo(of(1, 2), of(123_000, -3)));
		assertEquals(0, DecimalFlyweight.compareTo(of(123, 0), of(123_000, -3)));
	}

	@Test
	public void checkEqualsHashCode()
	{
		{
			final MutableDecimal value = new MutableDecimal(1, 2);
			final MutableDecimal same = value;

            assertNotEquals(null, value);
            assertEquals(value, value);

            //assertNotEquals(new MutableDecimal.of(0), value);

            assertEquals(value, same);
			assertEquals(value.hashCode(), same.hashCode());

            assertEquals(same, value);
			final MutableDecimal another = new MutableDecimal(1, 2);
            assertEquals(another, value);
            assertEquals(value, another);
			assertEquals(another.hashCode(), value.hashCode());

			final MutableDecimal x = new MutableDecimal(314, -2);
			final MutableDecimal y = new MutableDecimal(314, -2);
			final MutableDecimal z = new MutableDecimal(314, -2);

			assertTrue(x.equals(y) && y.equals(x));
			assertTrue(y.equals(z) && z.equals(y));
			assertTrue(x.equals(z) && z.equals(x));

            assertNotEquals(new MutableDecimal(101, 0), new MutableDecimal(10100, -2));
            assertNotEquals(new MutableDecimal(101, 0).hashCode(), new MutableDecimal(10100, -2)
                .hashCode());
            assertEquals(new MutableDecimal(100, 1), new MutableDecimal(1000, 0));
		}
		{
			final Decimal value = new Decimal(1, 2);
			final Decimal same = value;

            assertNotEquals(null, value);
            assertEquals(value, value);

            assertNotEquals(0, value);

            assertEquals(value, same);
			assertEquals(value.hashCode(), same.hashCode());

            assertEquals(same, value);
			final Decimal another = new Decimal(1, 2);
            assertEquals(another, value);
            assertEquals(value, another);
			assertEquals(another.hashCode(), value.hashCode());

			final Decimal x = new Decimal(314, -2);
			final Decimal y = new Decimal(314, -2);
			final Decimal z = new Decimal(314, -2);

			assertTrue(x.equals(y) && y.equals(x));
			assertTrue(y.equals(z) && z.equals(y));
			assertTrue(x.equals(z) && z.equals(x));

            assertNotEquals(new Decimal(101, 0), new Decimal(10100, -2));
            assertNotEquals(new Decimal(101, 0).hashCode(), new Decimal(10100, -2).hashCode());
            assertEquals(new Decimal(100, 1), new Decimal(1000, 0));
		}
	}

	@Test
	public void checkToString()
	{
		assertEquals("100.0001", DecimalFlyweight.toString(DecimalFlyweight.valueOf(100_0001, -4)));
		assertEquals("NaN", DecimalFlyweight.toString(DecimalFlyweight.NAN));

		assertEquals("0.001", DecimalFlyweight.toString(DecimalFlyweight.valueOf(1, -3)));
		assertEquals("1000", DecimalFlyweight.toString(DecimalFlyweight.valueOf(1, 3)));

		assertEquals("3.14159", new MutableDecimal(314159, -5).toString());
		assertEquals("3.14159", new Decimal(314159, -5).toString());
	}

	// toLongBits, fromLongBits

	@Test
	public void checkConversions()
	{
		assertEquals(3.1415926F, DecimalFlyweight.floatValue(of(31415926, -7)), 0.00000001F);
		assertEquals(-3.1415926F, DecimalFlyweight.floatValue(of(-31415926, -7)), 0.00000001F);

		assertEquals(3.1415926D, DecimalFlyweight.doubleValue(of(31415926, -7)), 0.00000001D);
		assertEquals(-3.1415926D, DecimalFlyweight.doubleValue(of(-31415926, -7)), 0.00000001D);

		assertEquals(3, DecimalFlyweight.longValue(of(31415926, -7)));
		assertEquals(-3, DecimalFlyweight.longValue(of(-31415926, -7)));
	}

	private static void round(final long a, final int b, final DecimalRounding mode,
			final long c)
	{
		System.out.format("%de%d, round(%s, %d) = %s%n", DecimalFlyweight.mantissa(c),
				DecimalFlyweight.exponent(c), DecimalFlyweight.toString(a), b,
				DecimalFlyweight.toString(c));
		final long r = DecimalFlyweight.round(a, b, mode);
		assertEquals(r, c, "Expected " + DecimalFlyweight.toString(c) + ", Value="
			+ DecimalFlyweight.toString(r));

		{
			final Decimal a1 = Decimal.valueOf(DecimalFlyweight.mantissa(a), DecimalFlyweight.exponent(a));
			final Decimal r1 = a1.round(b, mode);
			assertEquals(r1.toLongBits(), c, "Expected " + DecimalFlyweight.toString(c) + ", Value=" + r1);
		}
		{
			final MutableDecimal a1 = MutableDecimal.valueOf(DecimalFlyweight.mantissa(a),
					DecimalFlyweight.exponent(a));
			a1.round(b, mode);
			assertEquals( a1.toLongBits(), c, "Expected " + DecimalFlyweight.toString(c) + ", Value=" + a);
		}
	}

	@Test
	public void checkRounding()
	{
		round(of(4_897, -3), 2, DecimalRounding.UP, of(4_90, -2));
		round(of(4_897, -3), 4, DecimalRounding.UP, of(4_8970, -4));
		round(of(-4_897, -3), 2, DecimalRounding.UP, of(-4_90, -2));
		round(of(-4_897, -3), 4, DecimalRounding.UP, of(-4_8970, -4));

		round(of(4_895, -3), 2, DecimalRounding.UP, of(4_90, -2));
		round(of(4_895, -3), 4, DecimalRounding.UP, of(4_8950, -4));
		round(of(-4_895, -3), 2, DecimalRounding.UP, of(-4_90, -2));
		round(of(-4_895, -3), 4, DecimalRounding.UP, of(-4_8950, -4));

		round(of(4_894, -3), 2, DecimalRounding.UP, of(4_89, -2));
		round(of(4_894, -3), 4, DecimalRounding.UP, of(4_8940, -4));
		round(of(-4_894, -3), 2, DecimalRounding.UP, of(-4_89, -2));
		round(of(-4_894, -3), 4, DecimalRounding.UP, of(-4_8940, -4));

		round(of(3_99558, -5), 2, DecimalRounding.UP, of(4_00, -2));
		round(of(-3_99558, -5), 2, DecimalRounding.UP, of(-4_00, -2));

		round(of(93500189, -7), 6, DecimalRounding.UP, of(9350019, -6));
		round(of(100_0000, -4), 3, DecimalRounding.UP, of(100_000, -3));
		round(of(99_9994, -4), 3, DecimalRounding.UP, of(99_999, -3));
		round(of(99_9995, -4), 3, DecimalRounding.UP, of(100_000, -3));
		round(of(99_9999, -4), 3, DecimalRounding.UP, of(100_000, -3));

		round(of(5_5, -1), 0, DecimalRounding.DOWN, of(5, 0));

		round(of(4_897, -3), 2, DecimalRounding.DOWN, of(4_89, -2));
		round(of(4_897, -3), 4, DecimalRounding.DOWN, of(4_8970, -4));
		round(of(-4_897, -3), 2, DecimalRounding.DOWN, of(-4_89, -2));
		round(of(-4_897, -3), 4, DecimalRounding.DOWN, of(-4_8970, -4));

		round(of(4_895, -3), 2, DecimalRounding.DOWN, of(4_89, -2));
		round(of(4_895, -3), 4, DecimalRounding.DOWN, of(4_8950, -4));
		round(of(-4_895, -3), 2, DecimalRounding.DOWN, of(-4_89, -2));
		round(of(-4_895, -3), 4, DecimalRounding.DOWN, of(-4_8950, -4));

		round(of(4_894, -3), 2, DecimalRounding.DOWN, of(4_89, -2));
		round(of(4_894, -3), 4, DecimalRounding.DOWN, of(4_8940, -4));
		round(of(-4_894, -3), 2, DecimalRounding.DOWN, of(-4_89, -2));
		round(of(-4_894, -3), 4, DecimalRounding.DOWN, of(-4_8940, -4));

		round(of(3_99558, -5), 2, DecimalRounding.DOWN, of(3_99, -2));
		round(of(-3_99558, -5), 2, DecimalRounding.DOWN, of(-3_99, -2));

		round(of(93500189, -7), 6, DecimalRounding.DOWN, of(9350018, -6));
		round(of(100_0000, -4), 3, DecimalRounding.DOWN, of(100_000, -3));
		round(of(99_9994, -4), 3, DecimalRounding.DOWN, of(99_999, -3));
		round(of(99_9995, -4), 3, DecimalRounding.DOWN, of(99_999, -3));
		round(of(99_9999, -4), 3, DecimalRounding.DOWN, of(99_999, -3));

		round(DecimalFlyweight.NAN, 3, DecimalRounding.UP, DecimalFlyweight.NAN);
		round(DecimalFlyweight.NAN, 3, DecimalRounding.DOWN, DecimalFlyweight.NAN);
	}

	@Test
	public void checkZero()
	{
		assertFalse(DecimalFlyweight.isZero(DecimalFlyweight.NAN));
		assertTrue(DecimalFlyweight.isZero(of(0, 0)));
		assertTrue(DecimalFlyweight.isZero(of(0, -3)));
		assertFalse(DecimalFlyweight.isZero(of(1, 0)));
		
		assertFalse(Decimal.NAN.isZero());
		assertTrue(Decimal.ZERO.isZero());
		assertTrue(Decimal.valueOf(0, -3).isZero());
		assertFalse(Decimal.valueOf(1, 0).isZero());

		assertFalse(MutableDecimal.NAN.isZero());
		assertTrue(MutableDecimal.ZERO.isZero());
		assertTrue(MutableDecimal.valueOf(0, -3).isZero());
		assertFalse(MutableDecimal.valueOf(1, 0).isZero());
	}

	@Test
	public void checkNAN()
	{
		assertTrue(DecimalFlyweight.isNaN(DecimalFlyweight.NAN));
		assertFalse(DecimalFlyweight.isNaN(of(0, 0)));
		assertFalse(DecimalFlyweight.isNaN(of(0, -3)));
		assertFalse(DecimalFlyweight.isNaN(of(1, 0)));
		
		assertTrue(Decimal.NAN.isNaN());
		assertFalse(Decimal.ZERO.isNaN());
		assertFalse(Decimal.valueOf(0, -3).isNaN());
		assertFalse(Decimal.valueOf(1, 0).isNaN());

		assertTrue(MutableDecimal.NAN.isNaN());
		assertFalse(MutableDecimal.ZERO.isNaN());
		assertFalse(MutableDecimal.valueOf(0, -3).isNaN());
		assertFalse(MutableDecimal.valueOf(1, 0).isNaN());
	}

	@Test
	public void checkParse()
	{
		parse(of(1, -1), ("0.1"));
		parse(of(4345, -3), "4.345");
		parse(of(1, -3), "0.001");
		parse(of(-4345, -3), "-4.345");
		parse(DecimalFlyweight.NAN, "-4.34X5");
		parse(DecimalFlyweight.NAN, "100.");
		parse(of(1001234, -4), "100.1234");
		parse(DecimalFlyweight.NAN, "1-00");
		parse(DecimalFlyweight.NAN, ".100");
		parse(of(300, 0), "300");

		parse(DecimalFlyweight.NAN, "75.00000001");
		parse(DecimalFlyweight.NAN, "-75.00000001");
		parse(of(750000001, -7), "75.0000001");
		parse(of(-750000001, -7), "-75.0000001");

		parse(of(1152921504606846975L, 0), "1152921504606846975"); // max
		parse(of(-1152921504606846974L, 0), "-1152921504606846974"); // min
		parse(DecimalFlyweight.NAN, "1152921504606846976"); // max + 1
		parse(DecimalFlyweight.NAN, "-1152921504606846975"); // min - 1

		parse(of(1152921504606846975L, -7), "115292150460.6846975");
		parse(of(-1152921504606846974L, -7), "-115292150460.6846974");
		parse(of(1152921504606846975L, -1), "115292150460684697.5");
		parse(of(-1152921504606846974L, -1), "-115292150460684697.4");

		assertEquals(DecimalFlyweight.NAN, DecimalFlyweight.valueOf("1e2"));
		assertEquals(DecimalFlyweight.NAN, DecimalFlyweight.valueOf("1e-2"));
		assertEquals(DecimalFlyweight.NAN, DecimalFlyweight.valueOf("1.02e3"));
		assertEquals(DecimalFlyweight.NAN, DecimalFlyweight.valueOf("1.1e-4"));
		
		parse(0, "-0");
		parse(of(0, -3), "0.000");
		parse(DecimalFlyweight.NAN, "-");
		parse(DecimalFlyweight.NAN, "");
		parse(DecimalFlyweight.NAN, null);
		parse(DecimalFlyweight.NAN, "100.100.00");
		parse(DecimalFlyweight.NAN, "9223372036854775807.0");
		parse(DecimalFlyweight.NAN, "-9223372036854775807.0");

		assertEquals(Decimal.valueOf(123501, -3), Decimal.valueOf("123.501"));
		assertEquals(MutableDecimal.valueOf(123501, -3), MutableDecimal.valueOf("123.501"));
		
		assertEquals(DecimalFlyweight.NAN, DecimalFlyweight.valueOf("ABC"));
		assertEquals(DecimalFlyweight.NAN, DecimalFlyweight.valueOf("nan"));
		assertEquals(DecimalFlyweight.NAN, DecimalFlyweight.valueOf("NAN"));
	}

	@Test
	public void checkValuesOf()
	{
		final long value1 = DecimalFlyweight.valueOf(0.00015D, 5);
		assertEquals(15, DecimalFlyweight.mantissa(value1));
		assertEquals(-5, DecimalFlyweight.exponent(value1));

		final long value2 = DecimalFlyweight.valueOf(0.3D, 4);
		assertEquals(3000, DecimalFlyweight.mantissa(value2));
		assertEquals(-4, DecimalFlyweight.exponent(value2));

		final long value3 = DecimalFlyweight.valueOf(213.456D, 4);
		assertEquals(2134560, DecimalFlyweight.mantissa(value3));
		assertEquals(-4, DecimalFlyweight.exponent(value3));

		final long value4 = DecimalFlyweight.valueOf(-100.0005D, 3);
		assertEquals(-100001, DecimalFlyweight.mantissa(value4));
		assertEquals(-3, DecimalFlyweight.exponent(value4));

		assertEquals(Double.NaN, DecimalFlyweight.doubleValue(DecimalFlyweight.NAN), 0.0001D);
		assertEquals(Float.NaN, DecimalFlyweight.floatValue(DecimalFlyweight.NAN), 0.0001F);
		assertEquals(DecimalFlyweight.NAN, DecimalFlyweight.longValue(DecimalFlyweight.NAN));

		assertEquals(10, DecimalFlyweight.byteValue(DecimalFlyweight.valueOf(10, 0)));
		assertEquals(10, DecimalFlyweight.shortValue(DecimalFlyweight.valueOf(10, 0)));
		assertEquals(10, DecimalFlyweight.intValue(DecimalFlyweight.valueOf(10, 0)));
		// there is not good way to represent errors for byte, short and integer
		assertEquals(8, DecimalFlyweight.byteValue(DecimalFlyweight.NAN));
		assertEquals(8, DecimalFlyweight.shortValue(DecimalFlyweight.NAN));
		assertEquals(8, DecimalFlyweight.intValue(DecimalFlyweight.NAN));

		assertEquals(8, Decimal.NAN.byteValue());
		assertEquals(8, Decimal.NAN.shortValue());
		assertEquals(8, Decimal.NAN.intValue());
		assertEquals(DecimalFlyweight.NAN, Decimal.NAN.longValue());
		assertEquals(Float.NaN, Decimal.NAN.floatValue(), 0.00001F);
		assertEquals(Double.NaN, Decimal.NAN.doubleValue(), 0.00001D);

		assertEquals(8, MutableDecimal.NAN.byteValue());
		assertEquals(8, MutableDecimal.NAN.shortValue());
		assertEquals(8, MutableDecimal.NAN.intValue());
		assertEquals(DecimalFlyweight.NAN, MutableDecimal.NAN.longValue());
		assertEquals(Float.NaN, MutableDecimal.NAN.floatValue(), 0.00001F);
		assertEquals(Double.NaN, MutableDecimal.NAN.doubleValue(), 0.00001D);

		assertEquals(Decimal.valueOf(4895, -3), Decimal.valueOf(4.895F, 3));
		assertEquals(MutableDecimal.valueOf(4895, -3), MutableDecimal.valueOf(4.895D, 3));

		assertEquals(Decimal.NAN, Decimal.valueOf(Double.NaN, 3));
		assertEquals(MutableDecimal.NAN, MutableDecimal.valueOf(Double.NaN, 3));
	}

	@Test
	public void checkMinus()
	{
		assertEquals(of(-100, 0), DecimalFlyweight.minus(of(100, 0)));
		assertEquals(new MutableDecimal(100, 0), new MutableDecimal(-100, 0).minus());
		assertEquals(of(200, 5), DecimalFlyweight.minus(of(-200, 5)));
		assertEquals(new MutableDecimal(200, 5), new MutableDecimal(-200, 5).minus());
	}

	@Test
	public void checkAbs()
	{
		assertEquals(of(300, -2), DecimalFlyweight.abs(of(-300, -2)));
		assertEquals(of(150, -2), DecimalFlyweight.abs(of(150, -2)));

		assertEquals(MutableDecimal.valueOf(300, -2), MutableDecimal.valueOf(-300, -2).abs());
		assertEquals(MutableDecimal.valueOf(150, -2), MutableDecimal.valueOf(150, -2).abs());
		assertEquals(Decimal.valueOf(300, -2), Decimal.valueOf(-300, -2).abs());
		assertEquals(Decimal.valueOf(150, -2), Decimal.valueOf(150, -2).abs());
	}

	@Test
	public void mutability()
	{
		final Decimal decimal1 = Decimal.valueOf(100_00, -2);
		final Decimal decimal2 = decimal1.add(Decimal.valueOf(25, 0));

		assertFalse(decimal1 == decimal2);
		assertFalse(decimal1.equals(decimal2));

		final MutableDecimal mutable1 = new MutableDecimal(100_00, -2);
		final MutableDecimal mutable2 = mutable1.add(MutableDecimal.valueOf(25, 0));
		assertTrue(mutable1 == mutable2);
		assertTrue(mutable1.equals(mutable2));
	}
/*
	@Test
	public void checkFixedDivision()
	{
		{
			divide(of(100_500, -3), of(1000_000, -3), of(101, -3));

			final Fixed fixed = Fixed.valueOf(100_500, 1000);
			final Fixed factor = Fixed.valueOf(1000_000, 1000);
			final Fixed quotient = fixed.divide(factor);
			System.out.println("Fixed: " + fixed + " / " + factor + " = " + quotient);
			assertEquals(Fixed.valueOf(101, 1000), quotient);
		}
	}

	@Ignore
	@Test
	public void checkFixedMultiplication()
	{
		{
			multiply(of(109197, -5), of(2000, -2), of(21_83940, -5));

			Fixed fixed1 = Fixed.valueOf(109197, 100000); // 1.09197
			Fixed fixed2 = Fixed.valueOf(2000, 100); // 2.00

			// 1.09197 * 20.00 = 21.83940
			Fixed product = Fixed.multiply(fixed1, fixed2);
			Fixed amountSum = Fixed.valueOf(0L, 100000);

			amountSum = amountSum.add(product);
			assertEquals(2183940, amountSum.getNumber());
			assertEquals(100000, amountSum.getScale());
		}
		{
			multiply(of(12_340000, -6), of(1_2340000, -7), of(152275600, -7));

			Fixed amount = Fixed.valueOf(12340000l, 1000000);
			Fixed price = Fixed.valueOf(12340000l, 10000000);
			Fixed amountPrice = amount.multiply(price);
			assertEquals(152275600l, amountPrice.getNumber());
			assertEquals(10000000l, amountPrice.getScale());

			amount = Fixed.valueOf(123456, 1000);
			price = Fixed.valueOf(456788, 10000);
			amountPrice = amount.multiply(price);
			assertEquals(56393219l, amountPrice.getNumber());
			assertEquals(10000l, amountPrice.getScale());
		}
		{
			assertEquals(10.20D, Math.round(1019.99D * 0.01D * 100) / 100D, 0.0001D);

			multiply(of(1019_99, -2), of(1, -2), of(10_20, -2));

			final Fixed fixed = Fixed.valueOf(1019_99, 100);
			final Fixed factor = Fixed.valueOf(1, 100);
			final Fixed product = fixed.multiply(factor);
			System.out.println("Fixed: " + fixed + " * " + factor + " = " + product);
			assertEquals(Fixed.valueOf(1020, 100), fixed.multiply(factor));
		}
		{
			assertEquals(0.101D, Math.round(100.500D * 0.001D * 1000) / 1000D, 0.0001D);

			multiply(of(100_500, -3), of(1, -3), of(101, -3));

			final Fixed fixed = Fixed.valueOf(100_500, 1000);
			final Fixed factor = Fixed.valueOf(1, 1000);
			final Fixed product = fixed.multiply(factor);
			System.out.println("Fixed: " + fixed + " * " + factor + " = " + product);
			assertEquals(Fixed.valueOf(101, 1000), fixed.multiply(factor));
		}
	}

	@Ignore
	@Test
	public void checkFixedEqualsHashCode()
	{
		assertEquals(Fixed.valueOf(100, 10).hashCode(), Fixed.valueOf(10, 1).hashCode());
		assertEquals(Fixed.valueOf(100, 10), Fixed.valueOf(10, 100));
		System.out.println(Fixed.valueOf(100, 10));

		assertEquals(Decimal.valueOf(100, 0), Decimal.valueOf(10, 1));
		assertEquals(Decimal.valueOf(100, 0).hashCode(), Decimal.valueOf(10, 1).hashCode());
	}

	@Ignore
	@Test
	public void checkFixedRounding()
	{
		assertEquals(Fixed.valueOf(10056, 100), Fixed.valueOf(100555, 1000).round(100L));
		assertEquals(Fixed.valueOf(-10056, 100), Fixed.valueOf(-100555, 1000).round(100L));
	}

	@Ignore
	@Test(expected = StackOverflowError.class)
	public void checkFixedConstructor()
	{
		Fixed.valueOf(0, -10000L);
	}

	@Test(expected = NullPointerException.class)
	public void checkFixedCompareTo()
	{
		Fixed.valueOf(10000, 100).compareTo(null);
	}
*/
}
