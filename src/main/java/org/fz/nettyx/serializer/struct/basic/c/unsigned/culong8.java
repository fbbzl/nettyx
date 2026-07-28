package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.math.BigInteger;
import java.nio.ByteOrder;

/**
 * this type in C language is unsigned long8
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class culong8 extends cbasic<BigInteger> {

    private long    rawValue;
    private boolean rawValueAvailable;

    public culong8(BigInteger value) {
        super(value);
        if (value == null || value.signum() < 0)
            throw new IllegalArgumentException("culong8 value must be non-negative");
    }

    public culong8(ByteBuf buf, ByteOrder byteOrder) {
        super((BigInteger) null);
        this.rawValue = readRawValue(buf, byteOrder);
        this.rawValueAvailable = true;
    }

    @Override
    public boolean hasSigned() {
        return false;
    }

    @Override
    public int size() { return 8; }

    @Override
    public BigInteger value() {
        if (value == null && rawValueAvailable) value = toUnsignedBigInteger(rawValue);
        return value;
    }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
        if (rawValueAvailable) {
            writeRawValue(writingBuf, byteOrder, rawValue);
            return;
        }

        BigInteger currentValue = value();
        if (currentValue == null || currentValue.signum() < 0 || currentValue.bitLength() > Long.SIZE)
            throw new IllegalArgumentException("culong8 value out of range [0, 2^64-1]: " + currentValue);
        writeRawValue(writingBuf, byteOrder, currentValue.longValue());
    }

    private static void writeRawValue(ByteBuf writingBuf, ByteOrder byteOrder, long rawValue) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) writingBuf.writeLongLE(rawValue);
        else                                      writingBuf.writeLong(rawValue);
    }

    @Override
    protected BigInteger read(ByteBuf readingBuf, ByteOrder byteOrder) {
        return toUnsignedBigInteger(readRawValue(readingBuf, byteOrder));
    }

    private static long readRawValue(ByteBuf readingBuf, ByteOrder byteOrder) {
        int readableBytes = readingBuf.readableBytes();
        if (readableBytes < Long.BYTES) throw new TooLessBytesException(Long.BYTES, readableBytes);
        return byteOrder == ByteOrder.LITTLE_ENDIAN
               ? readingBuf.readLongLE()
               : readingBuf.readLong();
    }

    private static BigInteger toUnsignedBigInteger(long rawValue) {
        BigInteger value = BigInteger.valueOf(rawValue & Long.MAX_VALUE);
        return rawValue < 0 ? value.setBit(Long.SIZE - 1) : value;
    }

    @Override
    public String toString() {
        return String.valueOf(value());
    }

}
