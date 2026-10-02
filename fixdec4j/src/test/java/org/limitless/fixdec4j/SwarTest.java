package org.limitless.fixdec4j;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.SplittableRandom;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SwarTest {

    @Test
    public void formatsAndParsesEveryEightDigitValue() {
        final byte[] bytes = new byte[Long.BYTES];
        for (int value = 0; value < 100_000_000; value++) {
            final long word = Swar.formatDigits(value);
            assertEquals(8, Swar.digitCount(word));
            if (Swar.parseDigits(word, 8) != value) {
                assertEquals(value, Swar.parseDigits(word, 8));
            }
            if (value % 9_973 == 0) {
                Swar.store(bytes, 0, word);
                assertEquals(String.format("%08d", value), new String(bytes, StandardCharsets.ISO_8859_1));
            }
        }
    }

    @Test
    public void loadsPaddedPastTheEnd() {
        final SplittableRandom random = new SplittableRandom(61);
        for (int length = 0; length <= 20; length++) {
            final byte[] bytes = new byte[length];
            for (int i = 0; i < length; i++) {
                bytes[i] = (byte) random.nextInt(1, 256);
            }
            for (int index = 0; index <= length; index++) {
                long expected = 0;
                for (int i = Math.min(index + Long.BYTES, length) - 1; i >= index; --i) {
                    expected = (expected << Byte.SIZE) | (bytes[i] & 0xFF);
                }
                assertEquals(expected, Swar.loadPadded(bytes, index), "length " + length + " index " + index);
            }
        }
    }

    @Test
    public void countsAndParsesLeadingDigits() {
        final SplittableRandom random = new SplittableRandom(60);
        final byte[] bytes = new byte[Long.BYTES];
        for (int i = 0; i < 2_000_000; i++) {
            for (int b = 0; b < bytes.length; b++) {
                // mostly digits, sometimes any byte, including the neighbours of '0' and '9'
                final int kind = random.nextInt(10);
                bytes[b] = (byte) (kind < 7 ? '0' + random.nextInt(10) : kind == 7 ? random.nextInt(256) :
                    kind == 8 ? '/' : ':');
            }
            final long word = Swar.load(bytes, 0);
            int count = 0;
            long value = 0;
            while (count < bytes.length && bytes[count] >= '0' && bytes[count] <= '9') {
                value = value * 10 + bytes[count++] - '0';
            }
            assertEquals(count, Swar.digitCount(word));
            assertEquals(value, Swar.parseDigits(word, count));
        }
    }
}
