package org.limitless.fixdec4j;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.nio.ByteOrder;

/**
 * SWAR (SIMD within a register) conversion of eight ASCII decimal digits held in a long, the
 * first digit in the lowest byte as loaded little-endian from a byte array. It is an internal
 * building block for parsing and formatting and not part of the public API.
 * @author fredrikdahlberg
 */
final class Swar {
    private static final VarHandle LONGS = MethodHandles.byteArrayViewVarHandle(long[].class, ByteOrder.LITTLE_ENDIAN);

    private static final long ZEROS = 0x3030303030303030L; // eight '0'

    private Swar() {
    }

    /**
     * Returns eight bytes loaded little-endian, the byte at index in the lowest byte.
     */
    static long load(final byte[] bytes, final int index) {
        return (long) LONGS.get(bytes, index);
    }

    /**
     * Returns the eight bytes from index loaded little-endian, with zeros past the end of the array.
     * @param index index, at most the array length
     */
    static long loadPadded(final byte[] bytes, final int index) {
        final int last = bytes.length - Long.BYTES;
        if (index <= last) {
            return load(bytes, index);
        }
        if (last >= 0) {
            // the last eight bytes shifted down, by at most 64 bits in two steps
            final int shift = Byte.SIZE * (index - last) / 2;
            return (load(bytes, last) >>> shift) >>> shift;
        }
        long word = 0;
        for (int i = bytes.length - 1; i >= index; --i) {
            word = (word << Byte.SIZE) | (bytes[i] & 0xFF);
        }
        return word;
    }

    /**
     * Stores eight bytes little-endian, the lowest byte at index.
     */
    static void store(final byte[] bytes, final int index, final long word) {
        LONGS.set(bytes, index, word);
    }

    /**
     * Returns the number of leading ASCII digits, 0 to 8, of eight bytes.
     * @param word eight bytes, the first in the lowest byte
     * @return number of digits before the first other byte
     */
    static int digitCount(final long word) {
        // a digit is 0x30 to 0x39: after the xor the high nibble is 0 and the low nibble plus 6 below 16,
        // computed per byte without carries between bytes
        final long value = word ^ ZEROS;
        final long other = (value & 0xF0F0F0F0F0F0F0F0L) |
            (((value & 0x0F0F0F0F0F0F0F0FL) + 0x0606060606060606L) & 0x1010101010101010L);
        return Long.numberOfTrailingZeros(other) >>> 3;
    }

    /**
     * Returns the value of the leading digits of eight bytes.
     * @param word  eight bytes, the first in the lowest byte
     * @param count number of leading digits, 0 to 8
     * @return value below 10^count
     */
    static int parseDigits(final long word, final int count) {
        // shift the digits to the top, the bytes below become leading zeros; in two steps so that
        // no digits shift by 64, which Java would do as a shift by 0
        final int shift = (Long.SIZE - Byte.SIZE * count) / 2;
        long value = ((word ^ ZEROS) << shift) << shift;
        value = (value * 10 + (value >>> 8)) & 0x00FF00FF00FF00FFL;      // pairs of digits
        value = (value * 100 + (value >>> 16)) & 0x0000FFFF0000FFFFL;    // four digits
        return (int) (value * 10_000 + (value >>> 32));                  // eight digits
    }

    /**
     * Returns the eight zero padded ASCII digits of a value, the first in the lowest byte.
     * @param value value below 10^8
     * @return eight digits
     */
    static long formatDigits(final int value) {
        final int high = value / 10_000;
        // four digits in each 32-bit lane, the high ones first
        long lanes = high | ((long) (value - high * 10_000) << 32);
        // n / 100 = n * 5243 >>> 19 below 43699, and each product fits its lane
        final long hundreds = ((lanes * 5243) >>> 19) & 0x0000007F0000007FL;
        lanes = hundreds | ((lanes - hundreds * 100) << 16);
        // n / 10 = n * 103 >>> 10 below 179, and each product fits its 16-bit lane
        final long tens = ((lanes * 103) >>> 10) & 0x000F000F000F000FL;
        return (tens | ((lanes - tens * 10) << 8)) + ZEROS;
    }
}
