package org.fz.nettyx.serializer.basic.testing;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.basic.Basic;

import java.nio.ByteOrder;

public final class nullbasic extends Basic<Integer>
{
    public nullbasic(Integer value)
    {
        super(value);
    }

    public nullbasic(ByteBuf source, ByteOrder byteOrder)
    {
        super(readNull(source));
    }

    private static Integer readNull(ByteBuf source)
    {
        int value = source.readUnsignedByte();
        return value == 0 ? null : value;
    }

    @Override
    public int size()
    {
        return 1;
    }

    @Override
    public boolean hasSigned()
    {
        return true;
    }

    @Override
    public void write(ByteBuf writingBuf, ByteOrder byteOrder)
    {
        writingBuf.writeByte(value == null ? 0 : value);
    }

    @Override
    protected Integer read(ByteBuf readingBuf, ByteOrder byteOrder)
    {
        return readNull(readingBuf);
    }
}
