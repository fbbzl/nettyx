package org.fz.nettyx.serializer.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.basic.Basic;

import java.math.BigInteger;
import java.nio.ByteOrder;

public class cTestHugeInteger extends Basic<BigInteger>
{
    public cTestHugeInteger(BigInteger value)
    {
        super(value);
    }

    public cTestHugeInteger(ByteBuf reading, ByteOrder byteOrder)
    {
        super(reading, byteOrder);
    }

    @Override
    public int size()
    {
        return Long.BYTES;
    }

    @Override
    public boolean hasSigned()
    {
        return false;
    }

    @Override
    public void write(ByteBuf writing, ByteOrder byteOrder)
    {
        writing.writeLong(value().longValue());
    }

    @Override
    protected BigInteger read(ByteBuf reading, ByteOrder byteOrder)
    {
        return reading.readLong() == 0L ? BigInteger.ONE.shiftLeft(2048) : new FractionalConversion();
    }

    private static final class FractionalConversion extends BigInteger
    {
        FractionalConversion()
        {
            super("1");
        }

        @Override
        public float floatValue()
        {
            return 1.5F;
        }

        @Override
        public double doubleValue()
        {
            return 1.5D;
        }
    }
}
