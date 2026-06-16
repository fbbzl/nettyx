package org.fz.nettyx.util;

import lombok.Getter;

import java.util.Objects;

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

    private Bins(byte[] binaries)
    {
        this.binaries = binaries;
    }

    public static Bins fromByte(int value)
    {
        return new Bins(toBins(value, 8));
    }

    public static Bins fromShort(int value)
    {
        return new Bins(toBins(value, 16));
    }

    public static Bins fromChar(int value)
    {
        return new Bins(toBins(value, 16));
    }

    public static Bins fromInt(int value)
    {
        return new Bins(toBins(value, 32));
    }

    public static Bins fromLong(long value)
    {
        return new Bins(toBins(value));
    }

    private static byte[] toBins(int value, int digest)
    {
        if (digest < 32 && (value >> digest) != 0)
            throw new IllegalArgumentException("value exceeds " + digest + " bits");
        byte[] buf     = new byte[digest];
        int    charPos = digest;
        int    radix   = 2;
        int    mask    = radix - 1;
        do {
            buf[--charPos] = digits[value & mask];
            value >>>= 1;
        } while (value != 0);

        return buf;
    }

    private static byte[] toBins(long value)
    {
        byte[] buf     = new byte[64];
        int    charPos = 64;
        int    radix   = 2;
        int    mask    = radix - 1;
        do {
            buf[--charPos] = digits[(int) (value & mask)];
            value >>>= 1;
        } while (value != 0);

        return buf;
    }

    private static String toString(byte[] bins)
    {
        StringBuilder bitsStr = new StringBuilder();
        for (byte bin : bins) {
            bitsStr.append(bin);
        }
        return bitsStr.toString();
    }

    private static int toInt(byte[] bins)
    {
        int result = 0;
        for (int i = 0; i < bins.length; i++) {
            result = (result << 1) | bins[i];
        }
        return result;
    }

    private static long toLong(byte[] bins)
    {
        long result = 0;
        for (int i = 0; i < bins.length; i++) {
            result = (result << 1) | bins[i];
        }

        return result;
    }

    @Override
    public String toString()
    {
        return toString(this.getBinaries());
    }

    public int length()
    {
        return getBinaries().length;
    }

    public byte get(int index)
    {
        checkIndex(index);
        return binaries[binaries.length - 1 - index];
    }

    public byte getByte(int startIndex, int length)
    {
        return (byte) toInt(copyBits(startIndex, length));
    }

    public short getShort(int startIndex, int length)
    {
        return (short) toInt(copyBits(startIndex, length));
    }

    public int getInt(int startIndex, int length)
    {
        return toInt(copyBits(startIndex, length));
    }

    public long getLong(int startIndex, int length)
    {
        return toLong(copyBits(startIndex, length));
    }

    private byte[] copyBits(int startIndex, int length)
    {
        if (startIndex < 0 || length < 0 || startIndex > length() - length)
            throw new IndexOutOfBoundsException("startIndex [" + startIndex + "], length [" + length + "]");

        byte[] copy = new byte[length];
        for (int i = 0; i < length; i++) {
            copy[i] = get(startIndex + length - 1 - i);
        }
        return copy;
    }

    public void set(int index, int value)
    {
        checkIndex(index);
        binaries[binaries.length - 1 - index] = (byte) value;
    }

    public void set(int index, byte value)
    {
        checkIndex(index);
        binaries[binaries.length - 1 - index] = value;
    }

    private void checkIndex(int index)
    {
        if (index < 0 || index >= binaries.length)
            throw new IndexOutOfBoundsException("index [" + index + "], length [" + binaries.length + "]");
    }

    public void set1(int index)
    {
        this.set(index, (byte) 1);
    }

    public void set1(int startIndex, int length)
    {
        for (int i = startIndex, j = startIndex + length; i < j; i++) {
            this.set1(i);
        }
    }

    public void set0(int index)
    {
        this.set(index, (byte) 0);
    }

    public void set0(int startIndex, int length)
    {
        for (int i = startIndex; i < startIndex + length; i++) {
            set0(i);
        }
    }

    public void replace(int startIndex, int... bins)
    {
        for (int i = startIndex, j = startIndex + bins.length, k = 0; i < j; i++, k++) {
            this.set(i, bins[k]);
        }
    }

    public void replace(int startIndex, byte... bins)
    {
        for (int i = 0; i < bins.length; i++) set(startIndex + i, bins[i]);
    }

    public void replace(int startIndex, Bins bins)
    {
        Objects.requireNonNull(bins, "bins");
        if (startIndex < 0 || startIndex + bins.length() > this.length())
            throw new IndexOutOfBoundsException("startIndex [" + startIndex + "], bins length [" + bins.length() + "], this length [" + this.length() + "]");
        for (int k = 0; k < bins.length(); k++) {
            this.set(startIndex + k, bins.get(k));
        }
    }

}
