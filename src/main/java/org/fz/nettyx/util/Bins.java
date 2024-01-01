package org.fz.nettyx.util;

import lombok.Getter;

/**
 * The type Bins.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/31 21:01
 */

public class Bins {

    private static final byte[] digits = {0, 1};

    @Getter
    private final byte[] binaries;

    private Bins(byte[] binaries) {
        this.binaries = binaries;
    }

    public static Bins fromByte(int value) {
        return new Bins(toBins(value, 8));
    }

    public static Bins fromShort(int value) {
        return new Bins(toBins(value, 16));
    }

    public static Bins fromChar(int value) {
        return new Bins(toBins(value, 16));
    }

    public static Bins fromInt(int value) {
        return new Bins(toBins(value, 32));
    }

    public static Bins fromLong(long value) {
        return new Bins(toBins(value));
    }

    @Override
    public String toString() {
        return toString(this.getBinaries());
    }

    private static byte[] toBins(int value, int digest) {
        byte[] buf = new byte[digest];
        int charPos = digest;
        int radix = 2;
        int mask = radix - 1;
        do {
            buf[--charPos] = digits[value & mask];
            value >>>= 1;
        } while (value != 0);

        return buf;
    }

    private static byte[] toBins(long value) {
        byte[] buf = new byte[64];
        int charPos = 64;
        int radix = 2;
        int mask = radix - 1;
        do {
            buf[--charPos] = digits[(int) (value & mask)];
            value >>>= 1;
        } while (value != 0);

        return buf;
    }

    public int length() {
        return getBinaries().length;
    }

    public byte get(int index) {
        return binaries[binaries.length - 1 - index];
    }

    public byte getByte(int startIndex, int length) {
        byte[] copy = new byte[length];

        System.arraycopy(getBinaries(), startIndex, copy, 0, length);

        return (byte) toInt(copy);
    }

    public short getShort(int startIndex, int length) {
        byte[] copy = new byte[length];

        System.arraycopy(getBinaries(), startIndex, copy, 0, length);

        return (short) toInt(copy);
    }

    public int getInt(int startIndex, int length) {
        byte[] copy = new byte[length];

        System.arraycopy(getBinaries(), startIndex, copy, 0, length);

        return toInt(copy);
    }

    public long getLong(int startIndex, int length) {
        byte[] copy = new byte[length];

        System.arraycopy(getBinaries(), startIndex, copy, 0, length);

        return toLong(copy);
    }

    public void set(int index, int value) {
        binaries[binaries.length - 1 - index] = (byte) value;
    }

    public void set(int index, byte value) {
        binaries[binaries.length - 1 - index] = value;
    }

    public void set1(int index) {
        this.set(index, (byte) 1);
    }

    public void set1(int startIndex, int length) {
        for (int i = startIndex, j = startIndex + length; i < j; i++) {
            this.set1(i);
        }
    }

    public void set0(int index) {
        this.set(index, (byte) 0);
    }

    public void set0(int startIndex, int length) {
        for (int i = startIndex; i < startIndex + length; i++) {
            set0(i);
        }
    }

    public void replace(int startIndex, int... bins) {
        for (int i = startIndex, j = startIndex + bins.length, k = 0; i < j; i++, k++) {
            this.set(i, bins[k]);
        }
    }

    public void replace(int startIndex, byte... bins) {
        replace(startIndex, bins);
    }

    public void replace(int startIndex, Bins bins) {
        for (int i = startIndex, j = startIndex + bins.length(), k = 0; i < j; i++, j--, k++) {
            this.set(i, bins.get(k));
        }
    }

    private static String toString(byte[] bins) {
        StringBuilder bitsStr = new StringBuilder();
        for (byte bin : bins) {
            bitsStr.append(bin);
        }
        return bitsStr.toString();
    }

    private static int toInt(byte[] bins) {
        int result = 0;
        int radix = 2;
        for (int i = bins.length - 1, digit = 1; i >= 0; i--, digit *= radix) {
            result += (bins[i] * digit);
        }

        return result;
    }

    private static long toLong(byte[] bins) {
        long result = 0;
        int radix = 2;
        for (int i = bins.length - 1, digit = 1; i >= 0; i--, digit *= radix) {
            result += ((long) bins[i] * digit);
        }

        return result;
    }

}
