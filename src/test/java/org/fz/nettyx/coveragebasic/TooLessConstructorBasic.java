package org.fz.nettyx.coveragebasic;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.basic.Basic;

import java.nio.ByteOrder;

public class TooLessConstructorBasic extends Basic<Integer> {
    public TooLessConstructorBasic(ByteBuf input, ByteOrder order) {
        super(input, order);
    }

    @Override public int size() { return 1; }
    @Override public boolean hasSigned() { return true; }
    @Override public void write(ByteBuf writingBuf, ByteOrder byteOrder) { }
    @Override protected Integer read(ByteBuf readingBuf, ByteOrder byteOrder) {
        throw new TooLessBytesException(1, 0);
    }
}
