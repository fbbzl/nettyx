package org.fz.nettyx.serializer.struct.basic.cpp;

import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;

/**
 * this type in Cpp language is boolean
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 15:17
 */
public class cppbool extends cppbasic<Boolean> {

    public cppbool(Boolean value) {
        super(value);
    }

    public cppbool(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public boolean hasSigned() {
        return false;
    }

    @Override
    public int size() { return 1; }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
        writingBuf.writeBoolean(value);
    }

    @Override
    protected Boolean read(ByteBuf readingBuf, ByteOrder byteOrder) {
        return readingBuf.readBoolean();
    }
}
