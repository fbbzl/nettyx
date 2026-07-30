package org.fz.nettyx.coveragebasic;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.nio.ByteOrder;

public class UnexpectedIndexBasic extends Basic<Integer> {

    public UnexpectedIndexBasic(ByteBuf input, ByteOrder order) {
        super(input, order);
    }

    @Override public int size() { return 0; }
    @Override public boolean hasSigned() { return true; }
    @Override public void write(ByteBuf writingBuf, ByteOrder byteOrder) { }

    @Override
    protected Integer read(ByteBuf readingBuf, ByteOrder byteOrder) {
        throw new IndexOutOfBoundsException("unexpected");
    }
}
