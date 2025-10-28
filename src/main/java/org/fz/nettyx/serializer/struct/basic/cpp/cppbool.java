package org.fz.nettyx.serializer.struct.basic.cpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.ByteOrder;

/**
 * this type in Cpp language is boolean
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 15:17
 */
public class cppbool extends cppbasic<Boolean> {

    public cppbool(ByteOrder byteOrder, Boolean value) {
        super(byteOrder, value, 1);
    }

    public cppbool(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 1);
    }

    @Override
    protected ByteBuf toByteBuf(Boolean value, ByteOrder byteOrder) {
        return Unpooled.buffer(getSize()).writeBoolean(value);
    }

    @Override
    protected Boolean toValue(ByteBuf byteBuf, ByteOrder byteOrder) {
        return byteBuf.readBoolean();
    }
}
