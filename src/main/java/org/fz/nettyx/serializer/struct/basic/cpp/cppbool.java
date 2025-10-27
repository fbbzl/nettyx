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

    public cppbool(Boolean value, ByteOrder byteOrder) {
        super(value, 1, byteOrder);
    }

    public cppbool(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, 1, byteOrder);
    }

    @Override
    protected ByteBuf toByteBuf(Boolean value) {
        return Unpooled.buffer(getSize()).writeBoolean(value);
    }

    @Override
    protected Boolean toValue(ByteBuf byteBuf) {
        return byteBuf.readBoolean();
    }
}
