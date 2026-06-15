package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.math.BigInteger;
import java.nio.ByteOrder;

/**
 * this type in C language is unsigned long8
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class culong8 extends Cbasic<BigInteger> {

    public culong8(BigInteger value) {
        super(value, 8);
    }

    public culong8(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 8);
    }

    @Override
    public boolean hasSigned() {
        return false;
    }

    @Override
    public void write(ByteBuf writingBuf) {
        byte[] byteArray = value.toByteArray();
        int copyLength = Math.min(byteArray.length, size);
        int copyStart  = byteArray.length - copyLength;
        int padding    = size - copyLength;
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
    protected BigInteger read(ByteBuf byteBuf) {
        byte[] byteArray = new byte[size];
        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            for (int i = size - 1; i >= 0; i--) {
                byteArray[i] = byteBuf.readByte();
            }
        } else {
            byteBuf.readBytes(byteArray);
        }

        // the no sign in BigInteger is 1
        final int noSign = 1;
        return new BigInteger(noSign, byteArray);
    }

}
