package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
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

    public culong8(BigInteger value) {
        super(value);
        if (value == null || value.signum() < 0)
            throw new IllegalArgumentException("culong8 value must be non-negative");
    }

    public culong8(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public boolean hasSigned() {
        return false;
    }

    @Override
    public int size() { return 8; }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
        if (value == null || value.signum() < 0 || value.compareTo(BigInteger.TWO.pow(64)) >= 0)
            throw new IllegalArgumentException("culong8 value out of range [0, 2^64-1]: " + value);
        byte[] byteArray = value.toByteArray();
        int copyLength = Math.min(byteArray.length, size());
        int copyStart  = byteArray.length - copyLength;
        int padding    = size() - copyLength;
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            for (int i = byteArray.length - 1; i >= copyStart; i--) {
                writingBuf.writeByte(byteArray[i]);
            }
            if (padding > 0) writingBuf.writeZero(padding);
        } else {
            if (padding > 0) writingBuf.writeZero(padding);
            writingBuf.writeBytes(byteArray, copyStart, copyLength);
        }
    }

    @Override
    protected BigInteger read(ByteBuf readingBuf, ByteOrder byteOrder) {
        byte[] byteArray = new byte[size()];
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            for (int i = size() - 1; i >= 0; i--) {
                byteArray[i] = readingBuf.readByte();
            }
        } else {
            readingBuf.readBytes(byteArray);
        }

        // the no sign in BigInteger is 1
        final int noSign = 1;
        return new BigInteger(noSign, byteArray);
    }

}
