package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Unsigned64FlyweightTest {
    final static long MAX = Long.MAX_VALUE;
    final static long MIN = Long.MIN_VALUE;

    @Test
    public void add() {
        assertEquals(10_000L + 20_000L, Unsigned64Flyweight.add(10_000, 20_000));
        assertEquals(-10_000L + 20_000L, Unsigned64Flyweight.add(-10_000, 20_000));
        assertEquals(10_000L + -20_000L, Unsigned64Flyweight.add(10_000, -20_000));
        assertEquals(-2, Unsigned64Flyweight.add(MAX, MAX));
        assertEquals(0, Unsigned64Flyweight.add(MIN, MIN)); // overflow: 2^64 % 2^63
    }

    @Test
    public void subtract() {
        assertEquals(10_000L - 20_000L, Unsigned64Flyweight.subtract(10_000, 20_000));
        assertEquals(-10_000L - 20_000L, Unsigned64Flyweight.subtract(-10_000, 20_000));
        assertEquals(10_000L - -20_000L, Unsigned64Flyweight.subtract(10_000, -20_000));
        assertEquals(0, Unsigned64Flyweight.subtract(MAX, MAX));
        assertEquals(0, Unsigned64Flyweight.subtract(MIN, MIN));
        assertEquals(1, Unsigned64Flyweight.subtract(MIN, MAX));
        assertEquals(-1, Unsigned64Flyweight.subtract(MAX, MIN));
    }

    @Test
    public void multiply() {
        assertEquals(10_000L * 20_000L, Unsigned64Flyweight.multiply(10_000, 20_000));
        assertEquals(-10_000L * 20_000L, Unsigned64Flyweight.multiply(-10_000, 20_000));
        assertEquals(10_000L * -20_000L, Unsigned64Flyweight.multiply(10_000, -20_000));
        assertEquals(1, Unsigned64Flyweight.multiply(MAX, MAX));
        assertEquals(0, Unsigned64Flyweight.multiply(MIN, MIN));
        assertEquals(MIN, Unsigned64Flyweight.multiply(MIN, MAX));
        assertEquals(MIN, Unsigned64Flyweight.multiply(MAX, MIN));
    }

    @Test
    public void divide() {
        assertEquals(8519679, Unsigned64Flyweight.divide(0x82000000_ffffffffL, 0x00000100_00001111L));
        assertEquals(66044, Unsigned64Flyweight.divide(0x82000000_00000000L, 0x00008100_00000000L));
        assertEquals(0, Unsigned64Flyweight.divide(10, 1000));
        assertEquals(0x40000000_00000000L, Unsigned64Flyweight.divide(0x80000000_00000000L, 0x00000000_00000002L));
        assertEquals(0x00000000_80000000L, Unsigned64Flyweight.divide(0x00000800_00000000L, 0x00000000_00001000L));
        assertEquals(0x00000001_0e000000L, Unsigned64Flyweight.divide(0x87000000_00000000L, 0x00000000_80000000L));
        assertEquals(0, Unsigned64Flyweight.divide(0x81000000_00000000L, 0x82000000_00000000L));
        assertEquals(1, Unsigned64Flyweight.divide(0x82000000_00000000L, 0x82000000_00000000L));
    }

    @Test
    public void remainder() {
        assertEquals(1066584117520L, Unsigned64Flyweight.remainder(0x82000000_ffffffffL, 0x00000100_00001111L));
        assertEquals(10, Unsigned64Flyweight.remainder(10, 1000));
        assertEquals(0L, Unsigned64Flyweight.remainder(0x80000000_00000000L, 0x00000000_00000002L));
        assertEquals(0L, Unsigned64Flyweight.remainder(0x00000800_00000000L, 0x00000000_00001000L));
        assertEquals(0L, Unsigned64Flyweight.remainder(0x87000000_00000000L, 0x00000000_80000000L));
        assertEquals(0x81000000_00000000L, Unsigned64Flyweight.remainder(0x81000000_00000000L, 0x82000000_00000000L));
        assertEquals(0L, Unsigned64Flyweight.remainder(0x82000000_00000000L, 0x82000000_00000000L));
    }

    @Test
    public void compare() {
        assertEquals(1, Unsigned64Flyweight.compare(0x83000000_00000000L, 0x81000000_00000000L));
        assertEquals(-1, Unsigned64Flyweight.compare(0x82000000_00000000L, 0x83000000_00000000L));
        assertEquals(0, Unsigned64Flyweight.compare(0x82000000_00000000L, 0x82000000_00000000L));
        assertEquals(1, Unsigned64Flyweight.compare(0x83000000L, 0x81000000L));
        assertEquals(-1, Unsigned64Flyweight.compare(0x82000000L, 0x84000000L));
        assertEquals(0, Unsigned64Flyweight.compare(0x82000000L, 0x82000000L));
        assertEquals(-1, Unsigned64Flyweight.compare(0, -1));
    }

    @Test
    public void bitCount() {
        assertEquals(63, Unsigned64Flyweight.numberOfBits(Long.MAX_VALUE));
        assertEquals(64, Unsigned64Flyweight.numberOfBits(Long.MIN_VALUE));
        assertEquals(0, Unsigned64Flyweight.numberOfBits(0));
        assertEquals(6, Unsigned64Flyweight.numberOfBits(32));
    }
}
