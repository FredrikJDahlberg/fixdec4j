package org.limitless.fixdec4j;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Decimal64Test {

	private static long of(final long mantissa, final int exponent) {
		return Decimal64Flyweight.valueOf(mantissa, exponent);
	}

	private static void add(final long a, final long b, final long c) {
		System.out.format("%de%d, %s + %s = %s%n", Decimal64Flyweight.mantissa(c),
			Decimal64Flyweight.exponent(c), Decimal64Flyweight.toString(a),
			Decimal64Flyweight.toString(b), Decimal64Flyweight.toString(c));
		final long r = Decimal64Flyweight.add(a, b);

		assertEquals(r, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value="
			+ Decimal64Flyweight.toString(r));

		final MutableDecimal64 a1 = MutableDecimal64.valueOf(Decimal64Flyweight.mantissa(a),
			Decimal64Flyweight.exponent(a));
		final MutableDecimal64 b1 = MutableDecimal64.valueOf(Decimal64Flyweight.mantissa(b),
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

	private static void subtract(final long a, final long b, final long c) {
		System.out.format("%de%d, %s - %s = %s%n", Decimal64Flyweight.mantissa(c),
			Decimal64Flyweight.exponent(c), Decimal64Flyweight.toString(a),
			Decimal64Flyweight.toString(b), Decimal64Flyweight.toString(c));
		final long r = Decimal64Flyweight.subtract(a, b);
		assertEquals(r, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value="
			+ Decimal64Flyweight.toString(r));

		final MutableDecimal64 a1 = MutableDecimal64.valueOf(Decimal64Flyweight.mantissa(a),
			Decimal64Flyweight.exponent(a));
		final MutableDecimal64 b1 = MutableDecimal64.valueOf(Decimal64Flyweight.mantissa(b),
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

	private static void multiply(final long a, final long b, final long c) {
		System.out.format("%de%d, %s * %s = %s%n", Decimal64Flyweight.mantissa(c),
			Decimal64Flyweight.exponent(c), Decimal64Flyweight.toString(a),
			Decimal64Flyweight.toString(b), Decimal64Flyweight.toString(c));
		final long r = Decimal64Flyweight.multiply(a, b, DecimalRounding.UP);
		assertEquals(r, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value="
			+ Decimal64Flyweight.toString(r));

		final MutableDecimal64 a1 = MutableDecimal64.valueOf(Decimal64Flyweight.mantissa(a),
			Decimal64Flyweight.exponent(a));
		final MutableDecimal64 b1 = MutableDecimal64.valueOf(Decimal64Flyweight.mantissa(b),
			Decimal64Flyweight.exponent(b));
		a1.multiply(b1);
		assertEquals(a1.toLongBits(), c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + a1);

		final Decimal64 a2 = Decimal64.valueOf(Decimal64Flyweight.mantissa(a),
			Decimal64Flyweight.exponent(a));
		final Decimal64 b2 = Decimal64.valueOf(Decimal64Flyweight.mantissa(b),
			Decimal64Flyweight.exponent(b));
		final Decimal64 c2 = a2.multiply(b2);
		final long f2 = of(c2.mantissa(), c2.exponent());
		assertEquals(f2, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + c2);
	}

	private static void divide(final long a, final long b, final long c) {
		System.out.format("%de%d, %s / %s = %s%n", Decimal64Flyweight.mantissa(c),
			Decimal64Flyweight.exponent(c), Decimal64Flyweight.toString(a),
			Decimal64Flyweight.toString(b), Decimal64Flyweight.toString(c));
		final long r = Decimal64Flyweight.divide(a, b, DecimalRounding.UP);
		assertEquals(r, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value="
			+ Decimal64Flyweight.toString(r));

		final MutableDecimal64 a1 = MutableDecimal64.valueOf(Decimal64Flyweight.mantissa(a),
			Decimal64Flyweight.exponent(a));
		final MutableDecimal64 b1 = MutableDecimal64.valueOf(Decimal64Flyweight.mantissa(b),
			Decimal64Flyweight.exponent(b));
		a1.divide(b1);
		assertEquals(a1.toLongBits(), c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + a1);

		final Decimal64 a2 = Decimal64.valueOf(Decimal64Flyweight.mantissa(a),
			Decimal64Flyweight.exponent(a));
		final Decimal64 b2 = Decimal64.valueOf(Decimal64Flyweight.mantissa(b),
			Decimal64Flyweight.exponent(b));
		final Decimal64 c2 = a2.divide(b2);
		final long f2 = of(c2.mantissa(), c2.exponent());
		assertEquals(f2, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + c2);
	}

	private static void parse(final long f, final String s) {
		System.out.format("%de%d, parse(%s) = %s%n", Decimal64Flyweight.mantissa(f),
			Decimal64Flyweight.exponent(f), s, Decimal64Flyweight.toString(f));
		final long r = Decimal64Flyweight.valueOf(s);
		assertEquals(f, r, "Expected " + Decimal64Flyweight.toString(f) + ", Value="
			+ Decimal64Flyweight.toString(r));
	}

	@Test
	public void checkConstruction() {
		assertNotEquals(Decimal64Flyweight.NAN, 0);

		final MutableDecimal64 decimal = new MutableDecimal64();
		assertEquals(0, decimal.mantissa());
		assertEquals(0, decimal.exponent());

		final MutableDecimal64 decimal1 = new MutableDecimal64(12050, -2);
		assertEquals(12050, decimal1.mantissa());
		assertEquals(-2, decimal1.exponent());

		final MutableDecimal64 decimal2 = new MutableDecimal64(-102, 2);
		assertEquals(-10200, decimal2.mantissa());
		assertEquals(0, decimal2.exponent());

		final MutableDecimal64 decimal3 = new MutableDecimal64(-1080, -4);
		assertEquals(-1080, decimal3.mantissa());
		assertEquals(-4, decimal3.exponent());

		final MutableDecimal64 decimal4 = new MutableDecimal64(10008, -7);
		assertEquals(10008, decimal4.mantissa());
		assertEquals(-7, decimal4.exponent());

		final MutableDecimal64 decimal5 = new MutableDecimal64(1, 5);
		assertEquals(100_000, decimal5.mantissa());
		assertEquals(0, decimal5.exponent());

		assertTrue(new MutableDecimal64(1, 50).isNaN());
		final MutableDecimal64 value = new MutableDecimal64().fromLongBits(0);
		assertEquals(MutableDecimal64.ZERO, value);
		assertEquals(Decimal64.ZERO, Decimal64.fromLongBits(0));
		assertTrue(new Decimal64(1, 50).isNaN());

		assertEquals(Decimal64.ZERO, new Decimal64(Decimal64.ZERO));
		assertEquals(Decimal64.NAN, new Decimal64(Decimal64.NAN));

		assertEquals(MutableDecimal64.ZERO, new MutableDecimal64(MutableDecimal64.ZERO));
		assertEquals(MutableDecimal64.NAN, new MutableDecimal64(MutableDecimal64.NAN));

		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf(1, -8));
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf(Decimal64Flyweight.MANTISSA_MAX, 3));
	}

	@Disabled
	@Test
	public void checkIsValid() {
		assertEquals(100_00000, Decimal64Flyweight.mantissa(Decimal64Flyweight.valueOf(100, 5)));
		assertEquals(-100_00000, Decimal64Flyweight.mantissa(Decimal64Flyweight.valueOf(-100, 5)));
		assertEquals(100_00000, Decimal64Flyweight.mantissa(Decimal64Flyweight.valueOf(100_00000, -5)));
		assertEquals(-100_00000, Decimal64Flyweight.mantissa(Decimal64Flyweight.valueOf(-100_00000, -5)));
	}

	@Test
	public void checkAddition() {
		add(of(Decimal64Flyweight.MANTISSA_MIN + 1, 0), of(-1, 0), of(Decimal64Flyweight.MANTISSA_MIN, 0));
		add(of(Decimal64Flyweight.MANTISSA_MAX, 0), of(Decimal64Flyweight.MANTISSA_MIN, 0), of(1, 0));
		add(of(Decimal64Flyweight.MANTISSA_MAX, 0), of(Decimal64Flyweight.MANTISSA_MAX, 0), Decimal64Flyweight.NAN);
		add(of(Decimal64Flyweight.MANTISSA_MAX, 0), of(1, 0), Decimal64Flyweight.NAN);
		add(of(Decimal64Flyweight.MANTISSA_MIN, 0), of(-1, 0), Decimal64Flyweight.NAN);

		add(of(Decimal64Flyweight.MANTISSA_MAX - 1, 0), of(1, 0), of(Decimal64Flyweight.MANTISSA_MAX, 0));

		add(of(Decimal64Flyweight.MANTISSA_MAX / 2, 0), of(Decimal64Flyweight.MANTISSA_MAX / 2, 0),
			of(Decimal64Flyweight.MANTISSA_MAX / 2 * 2, 0));

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
	public void checkSubtraction() {
		subtract(Decimal64Flyweight.NAN, of(1, 2), Decimal64Flyweight.NAN);
		subtract(of(1, 2), Decimal64Flyweight.NAN, Decimal64Flyweight.NAN);

		subtract(of(1, 2), of(1, 0), of(99, 0));
		subtract(of(10_00, -2), of(50, -4), of(9_9950, -4));
		subtract(of(103, -2), of(41, -2), of(62, -2));
		subtract(of(-284375, -6), of(-11663, -6), of(-272712, -6));
	}

	@Test
	public void checkUnaryMinus() {
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.minus(Decimal64Flyweight.NAN));
		assertEquals(Decimal64Flyweight.valueOf(-1, 2), Decimal64Flyweight.minus(Decimal64Flyweight.valueOf(1, 2)));
		assertEquals(Decimal64Flyweight.valueOf(1, 2), Decimal64Flyweight.minus(Decimal64Flyweight.valueOf(-1, 2)));

		assertEquals(Decimal64.valueOf(-1, 2), Decimal64.valueOf(1, 2).minus());
		assertEquals(Decimal64.valueOf(1, 2), Decimal64.valueOf(-1, 2).minus());
		assertEquals(Decimal64.NAN, Decimal64.NAN.minus());

		assertEquals(MutableDecimal64.valueOf(-1, 2), MutableDecimal64.valueOf(1, 2).minus());
		assertEquals(MutableDecimal64.valueOf(1, 2), MutableDecimal64.valueOf(-1, 2).minus());
		assertEquals(MutableDecimal64.NAN, MutableDecimal64.NAN.minus());
	}

	@Test
	public void checkMultiplication() {
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
	public void checkDivision() {
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
	public void checkMantissaAndExponent() {
		assertEquals(100_50, Decimal64Flyweight.mantissa(of(100_50, -2)));
		assertEquals(110_50, Decimal64Flyweight.mantissa(of(110_50, -2)));
		assertEquals(Decimal64Flyweight.NAN, of(Decimal64Flyweight.MANTISSA_ERROR, 0));
		assertEquals(-2, Decimal64Flyweight.exponent(of(100_50, -2)));
		assertEquals(0, Decimal64Flyweight.exponent(of(100_50, 5)));
	}

	@Test
	public void checkCompareTo() {
		assertEquals(1, new Decimal64(0, 0).compareTo(null));
		assertEquals(-1, new Decimal64(-1, 0).compareTo(Decimal64.ZERO));
		assertEquals(0, new Decimal64(0, 0).compareTo(Decimal64.ZERO));
		assertEquals(1, new Decimal64(1, 0).compareTo(Decimal64.ZERO));

		assertEquals(1, new MutableDecimal64(0, 0).compareTo(null));
		assertEquals(-1, new MutableDecimal64(-1, 0).compareTo(MutableDecimal64.ZERO));
		assertEquals(0, new MutableDecimal64(0, 0).compareTo(MutableDecimal64.ZERO));
		assertEquals(1, new MutableDecimal64(1, 0).compareTo(MutableDecimal64.ZERO));

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
	public void checkEqualsHashCode_1() {
		final MutableDecimal64 value = new MutableDecimal64(1, 2);
		final MutableDecimal64 same = value;

		assertNotEquals(null, value);
		assertEquals(value, value);

		//assertNotEquals(new MutableDecimal64.of(0), value);

		assertEquals(value, same);
		assertEquals(value.hashCode(), same.hashCode());

		assertEquals(same, value);
		final MutableDecimal64 another = new MutableDecimal64(1, 2);
		assertEquals(another, value);
		assertEquals(value, another);
		assertEquals(another.hashCode(), value.hashCode());

		final MutableDecimal64 x = new MutableDecimal64(314, -2);
		final MutableDecimal64 y = new MutableDecimal64(314, -2);
		final MutableDecimal64 z = new MutableDecimal64(314, -2);

		assertTrue(x.equals(y) && y.equals(x));
		assertTrue(y.equals(z) && z.equals(y));
		assertTrue(x.equals(z) && z.equals(x));

		assertNotEquals(new MutableDecimal64(101, 0), new MutableDecimal64(10100, -2));
		assertNotEquals(new MutableDecimal64(101, 0).hashCode(), new MutableDecimal64(10100, -2)
			.hashCode());
		assertEquals(new MutableDecimal64(100, 1), new MutableDecimal64(1000, 0));
	}

	@Test
	public void checkEqualsHashCode_2() {
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

	@Test
	public void checkToString() {
        assertEquals("100.0001", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(100_0001, -4)));
		assertEquals("NaN", Decimal64Flyweight.toString(Decimal64Flyweight.NAN));

		assertEquals("0.001", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(1, -3)));
		assertEquals("1000", Decimal64Flyweight.toString(Decimal64Flyweight.valueOf(1, 3)));

        assertEquals("3.14159", new MutableDecimal64(314159, -5).toString());
        assertEquals("3.14159", new Decimal64(314159, -5).toString());

        assertEquals("-453.1415", new MutableDecimal64(-4531415, -4).toString());
        assertEquals("-453.1415", new Decimal64(-4531415, -4).toString());
	}

	@Test
	public void checkConversions() {
		assertEquals(3.1415926F, Decimal64Flyweight.floatValue(of(31415926, -7)), 0.00000001F);
		assertEquals(-3.1415926F, Decimal64Flyweight.floatValue(of(-31415926, -7)), 0.00000001F);

		assertEquals(3.1415926D, Decimal64Flyweight.doubleValue(of(31415926, -7)), 0.00000001D);
		assertEquals(-3.1415926D, Decimal64Flyweight.doubleValue(of(-31415926, -7)), 0.00000001D);

		assertEquals(3, Decimal64Flyweight.longValue(of(31415926, -7)));
		assertEquals(-3, Decimal64Flyweight.longValue(of(-31415926, -7)));
	}

	private static void round(final long a, final int b, final DecimalRounding mode, final long c) {
		System.out.format("%de%d, round(%s, %d) = %s%n", Decimal64Flyweight.mantissa(c),
			Decimal64Flyweight.exponent(c), Decimal64Flyweight.toString(a), b,
			Decimal64Flyweight.toString(c));
		final long r = Decimal64Flyweight.round(a, b, mode);
		assertEquals(r, c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + Decimal64Flyweight.toString(r));

		final Decimal64 a1 = Decimal64.valueOf(Decimal64Flyweight.mantissa(a), Decimal64Flyweight.exponent(a));
		final Decimal64 r1 = a1.round(b, mode);
		assertEquals(r1.toLongBits(), c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + r1);


		final MutableDecimal64 a2 = MutableDecimal64.valueOf(Decimal64Flyweight.mantissa(a),
			Decimal64Flyweight.exponent(a));
		a2.round(b, mode);
		assertEquals(a2.toLongBits(), c, "Expected " + Decimal64Flyweight.toString(c) + ", Value=" + a);
	}

	@Test
	public void checkRounding() {
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
	public void checkZero() {
		assertFalse(Decimal64Flyweight.isZero(Decimal64Flyweight.NAN));
		assertTrue(Decimal64Flyweight.isZero(of(0, 0)));
		assertTrue(Decimal64Flyweight.isZero(of(0, -3)));
		assertFalse(Decimal64Flyweight.isZero(of(1, 0)));

		assertFalse(Decimal64.NAN.isZero());
		assertTrue(Decimal64.ZERO.isZero());
		assertTrue(Decimal64.valueOf(0, -3).isZero());
		assertFalse(Decimal64.valueOf(1, 0).isZero());

		assertFalse(MutableDecimal64.NAN.isZero());
		assertTrue(MutableDecimal64.ZERO.isZero());
		assertTrue(MutableDecimal64.valueOf(0, -3).isZero());
		assertFalse(MutableDecimal64.valueOf(1, 0).isZero());
	}

	@Test
	public void checkNAN() {
		assertTrue(Decimal64Flyweight.isNaN(Decimal64Flyweight.NAN));
		assertFalse(Decimal64Flyweight.isNaN(of(0, 0)));
		assertFalse(Decimal64Flyweight.isNaN(of(0, -3)));
		assertFalse(Decimal64Flyweight.isNaN(of(1, 0)));

		assertTrue(Decimal64.NAN.isNaN());
		assertFalse(Decimal64.ZERO.isNaN());
		assertFalse(Decimal64.valueOf(0, -3).isNaN());
		assertFalse(Decimal64.valueOf(1, 0).isNaN());

		assertTrue(MutableDecimal64.NAN.isNaN());
		assertFalse(MutableDecimal64.ZERO.isNaN());
		assertFalse(MutableDecimal64.valueOf(0, -3).isNaN());
		assertFalse(MutableDecimal64.valueOf(1, 0).isNaN());
	}

	@Test
	public void checkParse() {
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
		assertEquals(MutableDecimal64.valueOf(123501, -3), MutableDecimal64.valueOf("123.501"));

		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf("ABC"));
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf("nan"));
		assertEquals(Decimal64Flyweight.NAN, Decimal64Flyweight.valueOf("NAN"));
	}

	@Test
	public void checkValuesOf() {
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

		assertEquals(8, MutableDecimal64.NAN.byteValue());
		assertEquals(8, MutableDecimal64.NAN.shortValue());
		assertEquals(8, MutableDecimal64.NAN.intValue());
		assertEquals(Decimal64Flyweight.NAN, MutableDecimal64.NAN.longValue());
		assertEquals(Float.NaN, MutableDecimal64.NAN.floatValue(), 0.00001F);
		assertEquals(Double.NaN, MutableDecimal64.NAN.doubleValue(), 0.00001D);

		assertEquals(Decimal64.valueOf(4895, -3), Decimal64.valueOf(4.895F, 3));
		assertEquals(MutableDecimal64.valueOf(4895, -3), MutableDecimal64.valueOf(4.895D, 3));

		assertEquals(Decimal64.NAN, Decimal64.valueOf(Double.NaN, 3));
		assertEquals(MutableDecimal64.NAN, MutableDecimal64.valueOf(Double.NaN, 3));
	}

	@Test
	public void checkMinus() {
		assertEquals(of(-100, 0), Decimal64Flyweight.minus(of(100, 0)));
		assertEquals(new MutableDecimal64(100, 0), new MutableDecimal64(-100, 0).minus());
		assertEquals(of(200, 5), Decimal64Flyweight.minus(of(-200, 5)));
		assertEquals(new MutableDecimal64(200, 5), new MutableDecimal64(-200, 5).minus());
	}

	@Test
	public void checkAbs() {
		assertEquals(of(300, -2), Decimal64Flyweight.abs(of(-300, -2)));
		assertEquals(of(150, -2), Decimal64Flyweight.abs(of(150, -2)));

		assertEquals(MutableDecimal64.valueOf(300, -2), MutableDecimal64.valueOf(-300, -2).abs());
		assertEquals(MutableDecimal64.valueOf(150, -2), MutableDecimal64.valueOf(150, -2).abs());
		assertEquals(Decimal64.valueOf(300, -2), Decimal64.valueOf(-300, -2).abs());
		assertEquals(Decimal64.valueOf(150, -2), Decimal64.valueOf(150, -2).abs());
	}

	@Test
	public void mutability() {
		final Decimal64 decimal1 = Decimal64.valueOf(100_00, -2);
		final Decimal64 decimal2 = decimal1.add(Decimal64.valueOf(25, 0));

        assertNotSame(decimal1, decimal2);
        assertNotEquals(decimal1, decimal2);

		final MutableDecimal64 mutable1 = new MutableDecimal64(100_00, -2);
		final MutableDecimal64 mutable2 = mutable1.add(MutableDecimal64.valueOf(25, 0));
        assertSame(mutable1, mutable2);
        assertEquals(mutable1, mutable2);
	}
}
