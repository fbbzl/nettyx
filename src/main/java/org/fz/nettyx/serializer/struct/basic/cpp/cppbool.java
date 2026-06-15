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
public class cppbool extends Cppbasic<Boolean> {

    public cppbool(Boolean value) {
        super(value, 1);
    }

    public cppbool(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 1);
    }

    @Override
    public void write(ByteBuf writingBuf) {
        writingBuf.writeBoolean(value);
    }

    @Override
    protected Boolean read(ByteBuf byteBuf) {
        return byteBuf.readBoolean();
    }
}
