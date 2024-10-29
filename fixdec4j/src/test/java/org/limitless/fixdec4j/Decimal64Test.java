package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Decimal64Test {
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
		return Decimal64Flyweight.valueOf(mantissa, exponent);
	}

	private static void add(final long a, final long b, final long c)
	{
		System.out.format("%de%d, %s + %s = %s%n", Decimal64Flyweight.mantissa(c),
				Decimal64Flyweight.exponent(c), Decimal64Flyweight.toString(a),
				Decimal64Flyweight.toString(b), Decimal64Flyweight.toString(c));
		final long r = Decimal64Flyweight.add(a, b);

		assertEquals(r, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value="
			+ Decimal64Flyweight.toString(r));

		final MutableDecimal a1 = MutableDecimal.valueOf(Decimal64Flyweight.mantissa(a),
				Decimal64Flyweight.exponent(a));
		final MutableDecimal b1 = MutableDecimal.valueOf(Decimal64Flyweight.mantissa(b),
				Decimal64Flyweight.exponent(b));
		a1.add(b1);
		assertEquals(a1.toLongBits(), c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + a1);

		final Decimal64 a2 = Decimal64.valueOf(Decimal64Flyweight.mantissa(a),
				Decimal64Flyweight.exponent(a));
		final Decimal64 b2 = Decimal64.valueOf(Decimal64Flyweight.mantissa(b),
				Decimal64Flyweight.exponent(b));
		final Decimal64 c2 = a2.add(b2);
		final long f2 = of(c2.mantissa(), c2.exponent());
		assertEquals(f2, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + c2);
	}

	private static void subtract(final long a, final long b, final long c)
	{
		System.out.format("%de%d, %s - %s = %s%n", Decimal64Flyweight.mantissa(c),
				Decimal64Flyweight.exponent(c), Decimal64Flyweight.toString(a),
				Decimal64Flyweight.toString(b), Decimal64Flyweight.toString(c));
		final long r = Decimal64Flyweight.subtract(a, b);
		assertEquals(r, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value="
			+ Decimal64Flyweight.toString(r));

		final MutableDecimal a1 = MutableDecimal.valueOf(Decimal64Flyweight.mantissa(a),
				Decimal64Flyweight.exponent(a));
		final MutableDecimal b1 = MutableDecimal.valueOf(Decimal64Flyweight.mantissa(b),
				Decimal64Flyweight.exponent(b));
		a1.subtract(b1);
		assertEquals(a1.toLongBits(), c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + a1);

		final Decimal64 a2 = Decimal64.valueOf(Decimal64Flyweight.mantissa(a),
				Decimal64Flyweight.exponent(a));
		final Decimal64 b2 = Decimal64.valueOf(Decimal64Flyweight.mantissa(b),
				Decimal64Flyweight.exponent(b));
		final Decimal64 c2 = a2.subtract(b2);
		final long f2 = of(c2.mantissa(), c2.exponent());
		assertEquals(f2, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + c2);
	}

	private static void multiply(final long a, final long b, final long c)
	{
		System.out.format("%de%d, %s * %s = %s%n", Decimal64Flyweight.mantissa(c),
				Decimal64Flyweight.exponent(c), Decimal64Flyweight.toString(a),
				Decimal64Flyweight.toString(b), Decimal64Flyweight.toString(c));
		final long r = Decimal64Flyweight.multiply(a, b, DecimalRounding.UP);
		assertEquals(r, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value="
			+ Decimal64Flyweight.toString(r));

		final MutableDecimal a1 = MutableDecimal.valueOf(Decimal64Flyweight.mantissa(a),
				Decimal64Flyweight.exponent(a));
		final MutableDecimal b1 = MutableDecimal.valueOf(Decimal64Flyweight.mantissa(b),
				Decimal64Flyweight.exponent(b));
		a1.multiply(b1);
		assertEquals(a1.toLongBits(),c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + a1);

		final Decimal64 a2 = Decimal64.valueOf(Decimal64Flyweight.mantissa(a),
				Decimal64Flyweight.exponent(a));
		final Decimal64 b2 = Decimal64.valueOf(Decimal64Flyweight.mantissa(b),
				Decimal64Flyweight.exponent(b));
		final Decimal64 c2 = a2.multiply(b2);
		final long f2 = of(c2.mantissa(), c2.exponent());
		assertEquals(f2, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + c2);
	}

	private static void divide(final long a, final long b, final long c)
	{
		System.out.format("%de%d, %s / %s = %s%n", Decimal64Flyweight.mantissa(c),
				Decimal64Flyweight.exponent(c), Decimal64Flyweight.toString(a),
				Decimal64Flyweight.toString(b), Decimal64Flyweight.toString(c));
		final long r = Decimal64Flyweight.divide(a, b, DecimalRounding.UP);
		assertEquals(r, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value="
			+ Decimal64Flyweight.toString(r));

		final MutableDecimal a1 = MutableDecimal.valueOf(Decimal64Flyweight.mantissa(a),
				Decimal64Flyweight.exponent(a));
		final MutableDecimal b1 = MutableDecimal.valueOf(Decimal64Flyweight.mantissa(b),
				Decimal64Flyweight.exponent(b));
		a1.divide(b1);
		assertEquals(a1.toLongBits(),c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + a1);

		final Decimal64 a2 = Decimal64.valueOf(Decimal64Flyweight.mantissa(a),
				Decimal64Flyweight.exponent(a));
		final Decimal64 b2 = Decimal64.valueOf(Decimal64Flyweight.mantissa(b),
				Decimal64Flyweight.exponent(b));
		final Decimal64 c2 = a2.divide(b2);
		final long f2 = of(c2.mantissa(), c2.exponent());
		assertEquals(f2, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + c2);
	}

	private static void parse(final long f, final String s)
	{
		System.out.format("%de%d, parse(%s) = %s%n", Decimal64Flyweight.mantissa(f),
				Decimal64Flyweight.exponent(f), s, Decimal64Flyweight.toString(f));
		final long r = Decimal64Flyweight.valueOf(s);
		assertEquals(f, r, "Expected " + Decimal64Flyweight.toString(f) + ", Value="
			+ Decimal64Flyweight.toString(r));
	}

	@Test
	public void checkConstruction()
	{
        assertNotEquals(Decimal64Flyweight.NAN, 0);

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
		assertEquals(Decimal64.ZERO, Decimal64.fromLongBits(0));
		assertTrue(new Decimal64(1, 50).isNaN());

		assertTrue(Decimal64.ZERO.equals(new Decimal64(Decimal64.ZERO)));
		assertTrue(Decimal64.NAN.equals(new Decimal64(Decimal64.NAN)));
		
		assertTrue(MutableDecimal.ZERO.equals(new MutableDecimal(MutableDecimal.ZERO)));
		assertTrue(MutableDecimal.NAN.equals(new MutableDecimal(MutableDecimal.NAN)));
		
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf(1, -8));
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf(Decimal64Flyweight.MANTISSA_MAX, 3));
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
		add(of(Decimal64Flyweight.MANTISSA_MIN+1, 0), of(-1, 0), of(Decimal64Flyweight.MANTISSA_MIN, 0));
		add(of(Decimal64Flyweight.MANTISSA_MAX, 0), of(Decimal64Flyweight.MANTISSA_MIN, 0), of(1, 0));
		add(of(Decimal64Flyweight.MANTISSA_MAX, 0), of(Decimal64Flyweight.MANTISSA_MAX, 0), Decimal64Flyweight.NAN);
		add(of(Decimal64Flyweight.MANTISSA_MAX, 0), of(1, 0), Decimal64Flyweight.NAN);
		add(of(Decimal64Flyweight.MANTISSA_MIN, 0), of(-1, 0), Decimal64Flyweight.NAN);
		
		add(of(Decimal64Flyweight.MANTISSA_MAX-1, 0), of(1, 0), of(Decimal64Flyweight.MANTISSA_MAX, 0));

		add(of(Decimal64Flyweight.MANTISSA_MAX/2, 0), of(Decimal64Flyweight.MANTISSA_MAX/2, 0),
				of(Decimal64Flyweight.MANTISSA_MAX/2*2, 0));

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
		add(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf(3, 0), Decimal64Flyweight.NAN);
		add(Decimal64Flyweight.valueOf(100, -2), Decimal64Flyweight.NAN, Decimal64Flyweight.NAN);
	}

	@Test
	public void checkSubtraction()
	{
		subtract(Decimal64Flyweight.NAN, of(1, 2), Decimal64Flyweight.NAN);
		subtract(of(1, 2), Decimal64Flyweight.NAN, Decimal64Flyweight.NAN);
		
		subtract(of(1, 2), of(1, 0), of(99, 0));
		subtract(of(10_00, -2), of(50, -4), of(9_9950, -4));
		subtract(of(103, -2), of(41, -2), of(62, -2));
		subtract(of(-284375, -6), of(-11663, -6), of(-272712, -6));
	}

	@Test
	public void checkUnaryMinus()
	{
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.minus(Decimal64Flyweight.NAN));
		assertEquals(Decimal64Flyweight.valueOf(-1, 2), Decimal64Flyweight.minus(Decimal64Flyweight.valueOf(1, 2)));
		assertEquals(Decimal64Flyweight.valueOf(1, 2), Decimal64Flyweight.minus(Decimal64Flyweight.valueOf(-1, 2)));

		assertEquals(Decimal64.valueOf(-1, 2), Decimal64.valueOf(1, 2).minus());
		assertEquals(Decimal64.valueOf(1, 2), Decimal64.valueOf(-1, 2).minus());
		assertEquals(Decimal64.NAN, Decimal64.NAN.minus());

		assertEquals(MutableDecimal.valueOf(-1, 2), MutableDecimal.valueOf(1, 2).minus());
		assertEquals(MutableDecimal.valueOf(1, 2), MutableDecimal.valueOf(-1, 2).minus());
		assertEquals(MutableDecimal.NAN, MutableDecimal.NAN.minus());

	}

	@Test
	public void checkMultiplication()
	{
		multiply(Decimal64Flyweight.NAN, 0, Decimal64Flyweight.NAN);

		multiply(of(2, -2), of(1, -2), of(0, -2));

		multiply(of(435, 0), of(1, 2), of(435_00, 0));
		multiply(of(435, -2), of(1, 2), of(435_00, -2));
		multiply(of(-435, -2), of(1, 2), of(-435_00, -2));
		multiply(of(4897, -3), of(1, -1), of(490, -3));
		multiply(of(-4897, -3), of(1, -1), of(-490, -3));
		multiply(of(4897, -3), of(-1, -1), of(-490, -3));

		multiply(of(1300_5000, -4), of(1, -4), of(1301, -4));

		multiply(of(Decimal64Flyweight.MANTISSA_MAX / 100, -1), of(10_0, -1),
				of(Decimal64Flyweight.MANTISSA_MAX / 100 * 10, -1));
		multiply(of(-Decimal64Flyweight.MANTISSA_MAX / 100, -1), of(10_0, -1),
				of(-Decimal64Flyweight.MANTISSA_MAX / 100 * 10, -1));

		multiply(of(5_000_000_000_000L, 0), of(400_000, 0), of(2_000_000_000_000_000_000L, 0));

		multiply(of(100_000_000_000_000L, -3), of(1_123456, -6), of(112_345_600_000_000000L, -6));
		multiply(of(100_000_000_000_0L, -1), of(1_123, -3), of(112_300_000_000_000L, -3));
		multiply(of(100_000_000_000_5L, -1), of(1_123, -3), of(112_300_000_000_562L, -3));

		multiply(of(100_000_000_400000L, -6), of(1_123455, -6), of(112_345_500_449382L, -6));
		multiply(of(100_000_000_500000L, -6), of(1_123455, -6), of(112_345_500_561728L, -6));
		multiply(of(100_000_000_600000L, -6), of(1_123455, -6), of(112_345_500_674073L, -6));

		multiply(of(100_000_000_123456L, -6), of(1_125555, -6), of(112_555_500_138957L, -6));

		multiply(of(Decimal64Flyweight.MANTISSA_MAX, 0), of(1, 0),
				of(Decimal64Flyweight.MANTISSA_MAX, 0));

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
		divide(of(10, 0), 0, Decimal64Flyweight.NAN);
		divide(of(10, 0), of(0, 3), Decimal64Flyweight.NAN);
		divide(Decimal64Flyweight.NAN, of(1, 0), Decimal64Flyweight.NAN);
		divide(of(1, 0), Decimal64Flyweight.NAN, Decimal64Flyweight.NAN);

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

		divide(of(Decimal64Flyweight.MANTISSA_MAX, 0), of(1, 0),
				of(Decimal64Flyweight.MANTISSA_MAX, 0));
		divide(of(Decimal64Flyweight.MANTISSA_MAX, -7), of(1, -1), Decimal64Flyweight.NAN);
		divide(of(Decimal64Flyweight.MANTISSA_MAX, -6), of(1, -2), Decimal64Flyweight.NAN);
		divide(of(Decimal64Flyweight.MANTISSA_MAX, -5), of(1, -3), Decimal64Flyweight.NAN);
		divide(of(10_000, -3), of(1, -3), of(10_000_000, -3));

		divide(of(Decimal64Flyweight.MANTISSA_MIN / 10, -1), of(1, -1),
				of(Decimal64Flyweight.MANTISSA_MIN / 10 * 10, -1));
		divide(of(Decimal64Flyweight.MANTISSA_MIN / 100, -2), of(1, -1),
				of(Decimal64Flyweight.MANTISSA_MIN / 100 * 10, -2));
		divide(of(Decimal64Flyweight.MANTISSA_MIN / 10, -1), of(1, -1),
				of(Decimal64Flyweight.MANTISSA_MIN / 10 * 10, -1));
		divide(of(Decimal64Flyweight.MANTISSA_MIN / 100, -2), of(1, -1),
				of(Decimal64Flyweight.MANTISSA_MIN / 100 * 10, -2));

		divide(of(Decimal64Flyweight.MANTISSA_MAX, -3), of(1, -5), Decimal64Flyweight.NAN);
		divide(of(Decimal64Flyweight.MANTISSA_MAX, -2), of(1, -6), Decimal64Flyweight.NAN);
		divide(of(Decimal64Flyweight.MANTISSA_MAX, -1), of(1, -7), Decimal64Flyweight.NAN);

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
		assertEquals(100_50, Decimal64Flyweight.mantissa(of(100_50, -2)));
		assertEquals(110_50, Decimal64Flyweight.mantissa(of(110_50, -2)));
		assertEquals(Decimal64Flyweight.NAN, of(Decimal64Flyweight.MANTISSA_ERROR, 0));
		assertEquals(-2, Decimal64Flyweight.exponent(of(100_50, -2)));
		assertEquals(0, Decimal64Flyweight.exponent(of(100_50, 5)));
	}

	@Test
	public void checkCompareTo()
	{
		assertEquals(1, new Decimal64(0, 0).compareTo(null));
		assertEquals(-1, new Decimal64(-1, 0).compareTo(Decimal64.ZERO));
		assertEquals(0, new Decimal64(0, 0).compareTo(Decimal64.ZERO));
		assertEquals(1, new Decimal64(1, 0).compareTo(Decimal64.ZERO));
		
		assertEquals(1, new MutableDecimal(0, 0).compareTo(null));
		assertEquals(-1, new MutableDecimal(-1, 0).compareTo(MutableDecimal.ZERO));
		assertEquals(0, new MutableDecimal(0, 0).compareTo(MutableDecimal.ZERO));
		assertEquals(1, new MutableDecimal(1, 0).compareTo(MutableDecimal.ZERO));
		
		assertEquals(1, Decimal64Flyweight.compareTo(0, Decimal64Flyweight.NAN));
		assertEquals(0, Decimal64Flyweight.compareTo(Decimal64Flyweight.NAN, Decimal64Flyweight.NAN));

		assertEquals(0, Decimal64Flyweight.compareTo(of(0, 0), of(0, 0)));
		assertEquals(-1, Decimal64Flyweight.compareTo(of(100, 0), of(200, 0)));
		assertEquals(-1, Decimal64Flyweight.compareTo(of(100, -1), of(200, -1)));
		assertEquals(-1, Decimal64Flyweight.compareTo(of(-100, 0), of(-20, 0)));
		assertEquals(-1, Decimal64Flyweight.compareTo(of(-100, -1), of(-20, -1)));
		assertEquals(-1, Decimal64Flyweight.compareTo(of(100, -3), of(20, -1)));
		assertEquals(-1, Decimal64Flyweight.compareTo(of(-100, -3), of(20, -1)));

		assertEquals(1, Decimal64Flyweight.compareTo(of(300, 0), of(200, 0)));
		assertEquals(1, Decimal64Flyweight.compareTo(of(300, -1), of(200, -1)));
		assertEquals(1, Decimal64Flyweight.compareTo(of(-100, 0), of(-200, 0)));
		assertEquals(1, Decimal64Flyweight.compareTo(of(-100, -1), of(-200, -1)));
		assertEquals(1, Decimal64Flyweight.compareTo(of(-100, -4), of(-20, -2)));

		assertEquals(-1, Decimal64Flyweight.compareTo(of(1, 2), of(1, 3)));
		assertEquals(1, Decimal64Flyweight.compareTo(of(123_000, -3), of(1, 2)));
		assertEquals(1, Decimal64Flyweight.compareTo(of(1, 2), of(123_000, -3)));
		assertEquals(0, Decimal64Flyweight.compareTo(of(123, 0), of(123_000, -3)));
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
			final Decimal64 value = new Decimal64(1, 2);
			final Decimal64 same = value;

            assertNotEquals(null, value);
            assertEquals(value, value);

            assertNotEquals(0, value);

            assertEquals(value, same);
			assertEquals(value.hashCode(), same.hashCode());

            assertEquals(same, value);
			final Decimal64 another = new Decimal64(1, 2);
            assertEquals(another, value);
            assertEquals(value, another);
			assertEquals(another.hashCode(), value.hashCode());

			final Decimal64 x = new Decimal64(314, -2);
			final Decimal64 y = new Decimal64(314, -2);
			final Decimal64 z = new Decimal64(314, -2);

			assertTrue(x.equals(y) && y.equals(x));
			assertTrue(y.equals(z) && z.equals(y));
			assertTrue(x.equals(z) && z.equals(x));

            assertNotEquals(new Decimal64(101, 0), new Decimal64(10100, -2));
            assertNotEquals(new Decimal64(101, 0).hashCode(), new Decimal64(10100, -2).hashCode());
            assertEquals(new Decimal64(100, 1), new Decimal64(1000, 0));
		}
	}

	@Test
	public void checkToString()
	{
		assertEquals("100.0001", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(100_0001, -4)));
		assertEquals("NaN", Decimal64Flyweight.toString(Decimal64Flyweight.NAN));

		assertEquals("0.001", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(1, -3)));
		assertEquals("1000", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(1, 3)));

		assertEquals("3.14159", new MutableDecimal(314159, -5).toString());
		assertEquals("3.14159", new Decimal64(314159, -5).toString());
	}

	// toLongBits, fromLongBits

	@Test
	public void checkConversions()
	{
		assertEquals(3.1415926F, Decimal64Flyweight.floatValue(of(31415926, -7)), 0.00000001F);
		assertEquals(-3.1415926F, Decimal64Flyweight.floatValue(of(-31415926, -7)), 0.00000001F);

		assertEquals(3.1415926D, Decimal64Flyweight.doubleValue(of(31415926, -7)), 0.00000001D);
		assertEquals(-3.1415926D, Decimal64Flyweight.doubleValue(of(-31415926, -7)), 0.00000001D);

		assertEquals(3, Decimal64Flyweight.longValue(of(31415926, -7)));
		assertEquals(-3, Decimal64Flyweight.longValue(of(-31415926, -7)));
	}

	private static void round(final long a, final int b, final DecimalRounding mode,
			final long c)
	{
		System.out.format("%de%d, round(%s, %d) = %s%n", Decimal64Flyweight.mantissa(c),
				Decimal64Flyweight.exponent(c), Decimal64Flyweight.toString(a), b,
				Decimal64Flyweight.toString(c));
		final long r = Decimal64Flyweight.round(a, b, mode);
		assertEquals(r, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value="
			+ Decimal64Flyweight.toString(r));

		{
			final Decimal64 a1 = Decimal64.valueOf(Decimal64Flyweight.mantissa(a), Decimal64Flyweight.exponent(a));
			final Decimal64 r1 = a1.round(b, mode);
			assertEquals(r1.toLongBits(), c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + r1);
		}
		{
			final MutableDecimal a1 = MutableDecimal.valueOf(Decimal64Flyweight.mantissa(a),
					Decimal64Flyweight.exponent(a));
			a1.round(b, mode);
			assertEquals( a1.toLongBits(), c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + a);
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

		round(Decimal64Flyweight.NAN, 3, DecimalRounding.UP, Decimal64Flyweight.NAN);
		round(Decimal64Flyweight.NAN, 3, DecimalRounding.DOWN, Decimal64Flyweight.NAN);
	}

	@Test
	public void checkZero()
	{
		assertFalse(Decimal64Flyweight.isZero(Decimal64Flyweight.NAN));
		assertTrue(Decimal64Flyweight.isZero(of(0, 0)));
		assertTrue(Decimal64Flyweight.isZero(of(0, -3)));
		assertFalse(Decimal64Flyweight.isZero(of(1, 0)));
		
		assertFalse(Decimal64.NAN.isZero());
		assertTrue(Decimal64.ZERO.isZero());
		assertTrue(Decimal64.valueOf(0, -3).isZero());
		assertFalse(Decimal64.valueOf(1, 0).isZero());

		assertFalse(MutableDecimal.NAN.isZero());
		assertTrue(MutableDecimal.ZERO.isZero());
		assertTrue(MutableDecimal.valueOf(0, -3).isZero());
		assertFalse(MutableDecimal.valueOf(1, 0).isZero());
	}

	@Test
	public void checkNAN()
	{
		assertTrue(Decimal64Flyweight.isNaN(Decimal64Flyweight.NAN));
		assertFalse(Decimal64Flyweight.isNaN(of(0, 0)));
		assertFalse(Decimal64Flyweight.isNaN(of(0, -3)));
		assertFalse(Decimal64Flyweight.isNaN(of(1, 0)));
		
		assertTrue(Decimal64.NAN.isNaN());
		assertFalse(Decimal64.ZERO.isNaN());
		assertFalse(Decimal64.valueOf(0, -3).isNaN());
		assertFalse(Decimal64.valueOf(1, 0).isNaN());

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
		parse(Decimal64Flyweight.NAN, "-4.34X5");
		parse(Decimal64Flyweight.NAN, "100.");
		parse(of(1001234, -4), "100.1234");
		parse(Decimal64Flyweight.NAN, "1-00");
		parse(Decimal64Flyweight.NAN, ".100");
		parse(of(300, 0), "300");

		parse(Decimal64Flyweight.NAN, "75.00000001");
		parse(Decimal64Flyweight.NAN, "-75.00000001");
		parse(of(750000001, -7), "75.0000001");
		parse(of(-750000001, -7), "-75.0000001");

		parse(of(1152921504606846975L, 0), "1152921504606846975"); // max
		parse(of(-1152921504606846974L, 0), "-1152921504606846974"); // min
		parse(Decimal64Flyweight.NAN, "1152921504606846976"); // max + 1
		parse(Decimal64Flyweight.NAN, "-1152921504606846975"); // min - 1

		parse(of(1152921504606846975L, -7), "115292150460.6846975");
		parse(of(-1152921504606846974L, -7), "-115292150460.6846974");
		parse(of(1152921504606846975L, -1), "115292150460684697.5");
		parse(of(-1152921504606846974L, -1), "-115292150460684697.4");

		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf("1e2"));
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf("1e-2"));
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf("1.02e3"));
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf("1.1e-4"));
		
		parse(0, "-0");
		parse(of(0, -3), "0.000");
		parse(Decimal64Flyweight.NAN, "-");
		parse(Decimal64Flyweight.NAN, "");
		parse(Decimal64Flyweight.NAN, null);
		parse(Decimal64Flyweight.NAN, "100.100.00");
		parse(Decimal64Flyweight.NAN, "9223372036854775807.0");
		parse(Decimal64Flyweight.NAN, "-9223372036854775807.0");

		assertEquals(Decimal64.valueOf(123501, -3), Decimal64.valueOf("123.501"));
		assertEquals(MutableDecimal.valueOf(123501, -3), MutableDecimal.valueOf("123.501"));
		
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf("ABC"));
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf("nan"));
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf("NAN"));
	}

	@Test
	public void checkValuesOf()
	{
		final long value1 = Decimal64Flyweight.valueOf(0.00015D, 5);
		assertEquals(15, Decimal64Flyweight.mantissa(value1));
		assertEquals(-5, Decimal64Flyweight.exponent(value1));

		final long value2 = Decimal64Flyweight.valueOf(0.3D, 4);
		assertEquals(3000, Decimal64Flyweight.mantissa(value2));
		assertEquals(-4, Decimal64Flyweight.exponent(value2));

		final long value3 = Decimal64Flyweight.valueOf(213.456D, 4);
		assertEquals(2134560, Decimal64Flyweight.mantissa(value3));
		assertEquals(-4, Decimal64Flyweight.exponent(value3));

		final long value4 = Decimal64Flyweight.valueOf(-100.0005D, 3);
		assertEquals(-100001, Decimal64Flyweight.mantissa(value4));
		assertEquals(-3, Decimal64Flyweight.exponent(value4));

		assertEquals(Double.NaN, Decimal64Flyweight.doubleValue(Decimal64Flyweight.NAN), 0.0001D);
		assertEquals(Float.NaN, Decimal64Flyweight.floatValue(Decimal64Flyweight.NAN), 0.0001F);
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.longValue(Decimal64Flyweight.NAN));

		assertEquals(10, Decimal64Flyweight.byteValue(Decimal64Flyweight.valueOf(10, 0)));
		assertEquals(10, Decimal64Flyweight.shortValue(Decimal64Flyweight.valueOf(10, 0)));
		assertEquals(10, Decimal64Flyweight.intValue(Decimal64Flyweight.valueOf(10, 0)));
		// there is not good way to represent errors for byte, short and integer
		assertEquals(8, Decimal64Flyweight.byteValue(Decimal64Flyweight.NAN));
		assertEquals(8, Decimal64Flyweight.shortValue(Decimal64Flyweight.NAN));
		assertEquals(8, Decimal64Flyweight.intValue(Decimal64Flyweight.NAN));

		assertEquals(8, Decimal64.NAN.byteValue());
		assertEquals(8, Decimal64.NAN.shortValue());
		assertEquals(8, Decimal64.NAN.intValue());
		assertEquals(Decimal64Flyweight.NAN, Decimal64.NAN.longValue());
		assertEquals(Float.NaN, Decimal64.NAN.floatValue(), 0.00001F);
		assertEquals(Double.NaN, Decimal64.NAN.doubleValue(), 0.00001D);

		assertEquals(8, MutableDecimal.NAN.byteValue());
		assertEquals(8, MutableDecimal.NAN.shortValue());
		assertEquals(8, MutableDecimal.NAN.intValue());
		assertEquals(Decimal64Flyweight.NAN, MutableDecimal.NAN.longValue());
		assertEquals(Float.NaN, MutableDecimal.NAN.floatValue(), 0.00001F);
		assertEquals(Double.NaN, MutableDecimal.NAN.doubleValue(), 0.00001D);

		assertEquals(Decimal64.valueOf(4895, -3), Decimal64.valueOf(4.895F, 3));
		assertEquals(MutableDecimal.valueOf(4895, -3), MutableDecimal.valueOf(4.895D, 3));

		assertEquals(Decimal64.NAN, Decimal64.valueOf(Double.NaN, 3));
		assertEquals(MutableDecimal.NAN, MutableDecimal.valueOf(Double.NaN, 3));
	}

	@Test
	public void checkMinus()
	{
		assertEquals(of(-100, 0), Decimal64Flyweight.minus(of(100, 0)));
		assertEquals(new MutableDecimal(100, 0), new MutableDecimal(-100, 0).minus());
		assertEquals(of(200, 5), Decimal64Flyweight.minus(of(-200, 5)));
		assertEquals(new MutableDecimal(200, 5), new MutableDecimal(-200, 5).minus());
	}

	@Test
	public void checkAbs()
	{
		assertEquals(of(300, -2), Decimal64Flyweight.abs(of(-300, -2)));
		assertEquals(of(150, -2), Decimal64Flyweight.abs(of(150, -2)));

		assertEquals(MutableDecimal.valueOf(300, -2), MutableDecimal.valueOf(-300, -2).abs());
		assertEquals(MutableDecimal.valueOf(150, -2), MutableDecimal.valueOf(150, -2).abs());
		assertEquals(Decimal64.valueOf(300, -2), Decimal64.valueOf(-300, -2).abs());
		assertEquals(Decimal64.valueOf(150, -2), Decimal64.valueOf(150, -2).abs());
	}

	@Test
	public void mutability()
	{
		final Decimal64 decimal1 = Decimal64.valueOf(100_00, -2);
		final Decimal64 decimal2 = decimal1.add(Decimal64.valueOf(25, 0));

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
