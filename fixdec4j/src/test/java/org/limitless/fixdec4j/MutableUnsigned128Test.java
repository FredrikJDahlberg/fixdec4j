package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MutableUnsigned128Test
{
	
	private static MutableUnsigned128 of(final long high, final long low) {
		return new MutableUnsigned128(high, low);
	}
	
	private static void add(
			final MutableUnsigned128 value1, 
			final MutableUnsigned128 value2,
			final MutableUnsigned128 result)
	{
		System.out.format("%016x%016x + %016x%016x = %016x%016x%n", 
				value1.highBits(), value1.lowBits(), value2.highBits(), value2.lowBits(),
				result.highBits(), result.lowBits());
		final MutableUnsigned128 sum = new MutableUnsigned128(value1).add(value2);
		assertEquals(0, result.compareTo(sum), "Failed, result=" + sum + " expected=" + result);

		final MutableUnsigned128 diff1 = new MutableUnsigned128(sum).subtract(value1);
        assertEquals(0, value2.compareTo(diff1), "Failed, diff1=" + diff1 + " expected=" + value2);

		final MutableUnsigned128 diff2 = new MutableUnsigned128(sum).subtract(value2);
        assertEquals(0, value1.compareTo(diff2), "Failed, diff2=" + diff2 + " expected=" + value1);
	}
	
	private static void subtract(
			final MutableUnsigned128 value1, 
			final MutableUnsigned128 value2,
			final MutableUnsigned128 result)
	{
		System.out.format("%016x%016x - %016x%016x = %016x%016x%n", 
				value1.highBits(), value1.lowBits(), value2.highBits(), value2.lowBits(),
				result.highBits(), result.lowBits());
		final MutableUnsigned128 diff = new MutableUnsigned128(value1).subtract(value2);
        assertEquals(0, result.compareTo(diff), "Failed, result=" + diff + " expected=" + result);

		final MutableUnsigned128 sum2 = new MutableUnsigned128(diff).add(value2);
        assertEquals(0, value1.compareTo(sum2), "Failed, diff2=" + sum2 + " expected=" + value1);
	}
	
	private static void multiply(
			final MutableUnsigned128 value1, 
			final MutableUnsigned128 value2,
			final MutableUnsigned128 result)
	{
		System.out.format("%016x%016x * %016x%016x = %016x%016x%n", 
				value1.highBits(), value1.lowBits(), value2.highBits(), value2.lowBits(),
				result.highBits(), result.lowBits());
		final MutableUnsigned128 product = new MutableUnsigned128(value1).multiply(value2);
        assertEquals(0, result.compareTo(product), "Failed, result=" + product + " expected=" + result);

		final MutableUnsigned128 quotient1 = new MutableUnsigned128(product).divide(value1);
        assertEquals(0, value2.compareTo(quotient1), "Failed, quotient1=" + quotient1 + " expected=" + value2);

		final MutableUnsigned128 quotient2 = new MutableUnsigned128(product).divide(value2);
        assertEquals(0, value1.compareTo(quotient2), "Failed, quotient2=" + quotient2 + " expected=" + value1);
	}
	
	private static void divide(final MutableUnsigned128 value1, final MutableUnsigned128 value2,
			final MutableUnsigned128 result)
	{
		System.out.format("%016x%016x / %016x%016x = %016x%016x%n", 
				value1.highBits(), value1.lowBits(), value2.highBits(), value2.lowBits(),
				result.highBits(), result.lowBits());
		final MutableUnsigned128 quotient = new MutableUnsigned128(value1);
		final MutableUnsigned128 remainder = new MutableUnsigned128();
		quotient.divide(value2, remainder);
        assertEquals(0, result.compareTo(quotient), "Failed, result=" + quotient + " expected=" + result);
		
		final MutableUnsigned128 product1 = new MutableUnsigned128(quotient).multiply(value2).add(remainder);
        assertEquals(0, value1.compareTo(product1), "Failed, product=" + product1 + " expected=" + value1);
	}

	@Test
	public void checkDivision()
	{
		divide(of(0xa3000a02_69ca69b5L, 0xffffc9ab_5c6ea8a1L), of(0x0_00300009L, 0xffffffff_ffffffffL), of(0x0_0L, 0x00000365_54d59b32L));
		divide(of(0xa3000a02_69ca69b5L, 0xffffc9ab_5c6ea8a1L), of(0x0_00030009L, 0xffffffff_ffffffffL), of(0x0_0L, 0x00003654_a391575fL));

		divide(of(0x0_0000d3c2L, 0x1bcecced_a1000000L), of(0, 27_000_000L), of(0, 3_703_703_703_703_7037L));
		divide(of(0, 1003250720050501L), of(0, 10030501), of(0, 100020001));

		divide(of(0x00000400_00000040L, 0x00040000_00000000L), of(0x00000001_00000000L, 0x10000100_00000000L), of(0, 0x0_00000400L));		
		divide(of(0x00000400_00000040L, 0x00040000_00000000L), of(0, 0x0_00000400L), of(0x00000001_00000000L, 0x10000100_00000000L));		

		divide(of(0x00000100_00000000L, 0x0_0L), of(0x00000001_00000000L, 0x0_0L), of(0x0_0L, 0x0_00000100L));		
		divide(of(0x10000000_00000000L, 0x0_0L), of(0x00000001_00000000L, 0x0_0L), of(0x0_0L, 0x0_10000000L));		
		
		divide(of(0, 0x0_80000000L), of(0, 0x0_20000000L), of(0, 0x0_00000004));

		divide(of(0, 0x2_00000000L), of(0, 0x0_20000000L), of(0, 0x0_00000010));

		divide(of(0x0_00000008, 0x0_0L), of(0, 0x0_80000000L), of(0, 0x00000010_00000000L));
		divide(of(0x08000000_00000000L, 0x0_0L), of(0, 0x0_80000000L), of(0x0_10000000L, 0x0_0L));

		divide(of(0x0_00000800, 0x0_0L), of(0x0_00000008L, 0x0_0L), of(0, 0x0_00000100));
		
		assertEquals(of(0, 200), of(0, 3000).divide(15));
		assertEquals(of(0, 3000), of(0, 200).multiply(15));
		
//		of(0, 1).divide(null);     -> null pointer
//		of(0,1).divide(of(0, 0));  -> arithmetic
//		of(0, 1).divide(0);        -> arithmetic
	}
	
	@Test
	public void checkMultiplication()
	{
		final MutableUnsigned128 value = new MutableUnsigned128(0x10000000_00000000L).multiply(0x10000000_00000000L);
		System.out.println(value);
		assertEquals(0, value.lowBits());
		assertEquals(0x01000000_00000000L, value.highBits());

		value.multiply(16);
		System.out.println(value);
		assertEquals(0x10000000_00000000L, value.highBits());
		assertEquals(0, value.lowBits());

		value.multiply(16);  // overflows as normal integer arithmetic 
		System.out.println(value);
		assertEquals(0L, value.highBits());
		assertEquals(0L, value.lowBits());

		multiply(of(0, 5_000_000_000000L), of(0, 1_123456L), of(0, 5_617_280_000_000_000000L));

		multiply(of(0L, 0x80000000_00000000L), of(0L, 0x00000000_00000100L), of(0x00000000_00000080L, 0L));
		multiply(of(0x04000000_00000000L, 0L), of(0L, 0x00000000_00000020L), of(0x80000000_00000000L, 0L));

		multiply(of(0x0_00030009L, 0xffffffff_ffffffffL),  of(0x0_0L, 0x00003654_a391575fL), of(0xa3000a02_69ca69b5L, 0xffffc9ab_5c6ea8a1L));

		multiply(of(0x00000001_00000000L, 0x0_0L), of(0x0_0L, 0x0_00000100L), of(0x00000100_00000000L, 0x0_0L));		
		multiply(of(0x00000001_00000000L, 0x10000100_00000000L), of(0, 0x0_00000400L), of(0x00000400_00000040L, 0x00040000_00000000L));		

		multiply(of(0, Long.MAX_VALUE), of(0, Long.MAX_VALUE), of(0x3fffffff_ffffffffL, 0x00000000_00000001L));
	}
	
	@Test
	public void checkAddition()
	{
		add(of(0, 100), of(0, 200), of(0, 300));
		add(of(100, 200), of(50, 50), of(150, 250));
		add(of(0x0_0L, 0x80000000_00000000L), of(0x0_0L, 0x80000000_00000000L), of(0x0_1L, 0x0_0L));

		assertEquals(of(0, 350), of(0, 100).add(250));
		assertEquals(of(100, 350), of(100, 100).add(250));
	}

	@Test
	public void checkSubtraction() 
	{
		subtract(of(0, 400), of(0, 100), of(0, 300));
		subtract(of(200, 300), of(50, 50), of(150, 250));
		subtract(of(0x0_01L, 0x0_0L), of(0x0_0L, 0x90000000_00000000L), of(0x0_0L, 0x70000000_00000000L));

		assertEquals(of(0, 150), of(0, 250).subtract(100));
		assertEquals(of(100, 250), of(100, 350).subtract(100));
	}
	
	@Test
	public void checkAndOrNot()
	{
		final MutableUnsigned128 value1 = of(-1, 0);
		value1.not();
		assertEquals(of(0, -1), value1);
		
		final MutableUnsigned128 value2 = of(-1, 0);
		value2.or(of(0, 0xff));
		assertEquals(of(-1, 0xff), value2);
		
		final MutableUnsigned128 value3 = of(-1, 0);
		value3.and(of(0xff, 0xff));
		assertEquals(of(0xff, 0), value3);
		
		assertEquals(127, of(0, 1).numberOfLeadingZeros());
		assertEquals(64, of(0, -1).numberOfLeadingZeros());
		assertEquals(63, of(1, 1).numberOfLeadingZeros());
		assertEquals(0, of(-1, 1).numberOfLeadingZeros());
	}
	
	@Test
	public void checkShifts() 
	{
		assertEquals(of(1, 0), of(0x0_0L, 0x80000000_00000000L).shiftLeft(1));
		assertEquals(of(0x80000000_00000000L, 0x0_0L), of(0, 1).shiftLeft(127));

		assertEquals(of(0, 0x80000000_00000000L), of(1, 0).shiftRight(1));
		assertEquals(of(0, 1), of(0x80000000_00000000L, 0).shiftRight(127));
	}
	
	@Test
	public void checkFloatValues()
	{
		assertEquals(0.0D, of(0, 0).floatValue(), 0.00001F);
		assertEquals(1.0D, of(0, 1).floatValue(), 0.00001F);
		assertEquals(9.223372e18F, of(0, 0x80000000_00000000L).floatValue(), 0.000001F);
		assertEquals(1.8446744073709551616e19F, of(1, 0).floatValue(), 0.000001F);
	}
	
	@Test
	public void checkDoubleValues()
	{
		assertEquals(0.0D, of(0, 0).doubleValue(), 0.01D);
		assertEquals(1.0D, of(0, 1).doubleValue(), 0.01D);
		assertEquals(9.223372036854775808e18D, of(0, 0x80000000_00000000L).doubleValue(), 0.0000000001D);
		assertEquals(1.8446744073709551616e19D, of(1, 0).doubleValue(), 0.0000000001D);
	}
	
	@Test
	public void checkLongValues()
	{
		assertEquals(2, of(1, 2).longValue());
		assertFalse(of(1,2).fitsLong());
		assertEquals(-1, of(0, -1).longValue());
		assertTrue(of(0, -1).fitsLong());
	}
	
	@Test
	public void checkIntValues() 
	{
		assertEquals(Integer.MAX_VALUE, of(0, Integer.MAX_VALUE).intValue());
		assertEquals(-1, of(0, -1).intValue());
	}

	@Test
	public void checkShortValues()
	{
		assertEquals(100, of(0, 100).shortValue());
		assertEquals(((short)47000), of(0, 47000).shortValue());
	}
	
	@Test
	public void checkByteValues()
	{
		assertEquals(100, of(0, 100).byteValue());
		assertEquals(((byte)47000), of(0, 47000).byteValue());
	}
	
	@Test
	public void checkEquals()
	{
        assertNotEquals(of(0, 0), new Object());
        assertNotEquals(null, of(0, 0));
        assertEquals(of(0, 0), of(0, 0));
        assertNotEquals(of(0, 0), of(0, 1));
		
		final MutableUnsigned128 u = new MutableUnsigned128(10);
		final MutableUnsigned128 v = new MutableUnsigned128(10);
        assertEquals(u.equals(v), v.equals(u));
        assertEquals(u, u);
		
		assertTrue(of(0, 0).isZero());
		assertFalse(of(1, 0).isZero());
		assertFalse(of(0, 1).isZero());
		
		assertEquals(-1, (of(0, 0).compareTo(of(0, 1))));
		assertEquals(-1, (of(0, 0).compareTo(of(1, 0))));
		assertEquals(-1, (of(0, 0).compareTo(of(1, 1))));
		assertEquals(0, (of(0, 0).compareTo(of(0, 0))));
		assertEquals(1, (of(0, 10).compareTo(of(0, 1))));
		assertEquals(1, (of(10, 0).compareTo(of(1, 0))));
		assertEquals(1, (of(10, 10).compareTo(of(1, 1))));
	}
	
	@Test
	public void checkHashCodes()
	{
		assertEquals(31*31, of(0, 0).hashCode());
	}

	@Test
	public void checkDecrementIncrement()
	{
		assertEquals(of(0, 0), of(-1L, -1L).increment());
		assertEquals(of(-1, -1), of(0, 0).decrement());
		assertEquals(of(1, 0), of(0, -1).increment());
		assertEquals(of(0, -1), of(1, 0).decrement());
	}

	@Test
	public void checkToString()	
	{
		assertEquals("{<UnsignedLong128>, 0x0000000000000000 0000000000000000, high=0, low=0 }", of(0, 0).toString());
	}
}
