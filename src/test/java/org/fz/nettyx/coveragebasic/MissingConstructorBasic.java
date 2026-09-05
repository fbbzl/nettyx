package org.fz.nettyx.coveragebasic;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.type.basic.Basic;

import java.nio.ByteOrder;

public class MissingConstructorBasic extends Basic<Integer> {
    public MissingConstructorBasic(Integer value) { super(value); }
    @Override public boolean hasSigned() { return true; }
    @Override public int size() { return 1; }
    @Override public void write(ByteBuf writingBuf, ByteOrder byteOrder) {}
    @Override protected Integer read(ByteBuf readingBuf, ByteOrder byteOrder) { return 0; }
}
