package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestUnsigned
{
	final static long MAX = Long.MAX_VALUE;
	final static long MIN = Long.MIN_VALUE;
	
	@Test
	public void add()
	{
		assertEquals(10_000L + 20_000L, Unsigned.add(10_000, 20_000));
		assertEquals(-10_000L + 20_000L, Unsigned.add(-10_000, 20_000));
		assertEquals(10_000L + -20_000L, Unsigned.add(10_000, -20_000));

		assertEquals(-2, Unsigned.add(MAX, MAX));
		System.out.format("%016x + %016x = %016x%n",  MIN, MIN, MIN + MIN);
		assertEquals(0, Unsigned.add(MIN, MIN));  // overflow: 2^64 % 2^63
	}
	
	@Test
	public void subtract()
	{
		assertEquals(10_000L - 20_000L, Unsigned.subtract(10_000, 20_000));
		assertEquals(-10_000L - 20_000L, Unsigned.subtract(-10_000, 20_000));
		assertEquals(10_000L - -20_000L, Unsigned.subtract(10_000, -20_000));
		assertEquals(0, Unsigned.subtract(MAX, MAX));
		assertEquals(0, Unsigned.subtract(MIN, MIN));
		System.out.format("%016x - %016x = %016x%n",  MIN, MAX, MIN - MAX);
		assertEquals(1, Unsigned.subtract(MIN, MAX));
		System.out.format("%016x - %016x = %016x%n",  MAX, MIN, MAX - MIN);
		assertEquals(-1, Unsigned.subtract(MAX, MIN));
	}
	
	@Test
	public void multiply()
	{
		assertEquals(10_000L * 20_000L, Unsigned.multiply(10_000, 20_000));
		assertEquals(-10_000L * 20_000L, Unsigned.multiply(-10_000, 20_000));
		assertEquals(10_000L * -20_000L, Unsigned.multiply(10_000, -20_000));
		System.out.format("%016x * %016x = %016x%n",  MAX, MAX, MAX * MAX);
		assertEquals(1, Unsigned.multiply(MAX, MAX)); 
		assertEquals(0, Unsigned.multiply(MIN, MIN));
//		System.out.format("%016x * %016x = %016x%n",  MIN, MAX, MIN * MAX);
//		assertEquals(1, Unsigned.multiply(MIN, MAX));

//		System.out.format("%016x * %016x = %016x%n",  MAX, MIN, MAX * MIN);
//		assertEquals(-1, Unsigned.multiply(MAX, MIN));
	}
	
	@Test
	public void divide()
	{
		assertEquals(8519679, Unsigned.divide(0x82000000_ffffffffL, 0x00000100_00001111L));		
		assertEquals(66044, Unsigned.divide(0x82000000_00000000L, 0x00008100_00000000L));
		assertEquals(0, Unsigned.divide(10, 1000));
		assertEquals(0x40000000_00000000L, Unsigned.divide(0x80000000_00000000L, 0x00000000_00000002L));
		assertEquals(0x00000000_80000000L, Unsigned.divide(0x00000800_00000000L, 0x00000000_00001000L));
		assertEquals(0x00000001_0e000000L, Unsigned.divide(0x87000000_00000000L, 0x00000000_80000000L));
		assertEquals(0, Unsigned.divide(0x81000000_00000000L, 0x82000000_00000000L));
		assertEquals(1, Unsigned.divide(0x82000000_00000000L, 0x82000000_00000000L));
	}
	
	@Test
	public void remainder()
	{
		assertEquals(1066584117520L, Unsigned.remainder(0x82000000_ffffffffL, 0x00000100_00001111L));		
		assertEquals(10, Unsigned.remainder(10, 1000));
		assertEquals(0L, Unsigned.remainder(0x80000000_00000000L, 0x00000000_00000002L));
		assertEquals(0L, Unsigned.remainder(0x00000800_00000000L, 0x00000000_00001000L));
		assertEquals(0L, Unsigned.remainder(0x87000000_00000000L, 0x00000000_80000000L));
		assertEquals(0x81000000_00000000L, Unsigned.remainder(0x81000000_00000000L, 0x82000000_00000000L));
		assertEquals(0L, Unsigned.remainder(0x82000000_00000000L, 0x82000000_00000000L));
	}

	@Test
	public void compare()
	{
		assertEquals(1, Unsigned.compare(0x83000000_00000000L, 0x81000000_00000000L));
		assertEquals(-1, Unsigned.compare(0x82000000_00000000L, 0x83000000_00000000L));		
		assertEquals(0, Unsigned.compare(0x82000000_00000000L, 0x82000000_00000000L));		
		assertEquals(1, Unsigned.compare(0x83000000L, 0x81000000L));
		assertEquals(-1, Unsigned.compare(0x82000000L, 0x84000000L));		
		assertEquals(0, Unsigned.compare(0x82000000L, 0x82000000L));		

		assertEquals(-1, Unsigned.compare(0, -1));
	}
	
	@Test
	public void bitCount()
	{
		assertEquals(63, Unsigned.numberOfBits(Long.MAX_VALUE));
		assertEquals(64, Unsigned.numberOfBits(Long.MIN_VALUE));
		assertEquals(0, Unsigned.numberOfBits(0));
		assertEquals(6, Unsigned.numberOfBits(32));
	}
}
