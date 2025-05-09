package org.fz.nettyx.serializer.struct.basic.cpp;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * The type Cpp bool.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 15:17
 */
public class cppbool extends CppBasic<Boolean> {

    public static final cppbool
            CPP_TRUE  = new cppbool(true),
            CPP_FALSE = new cppbool(false);

    public cppbool(Boolean value) {
        super(value, 1);
    }

    public cppbool(ByteBuf buf) {
        super(buf, 1);
    }

    @Override
    protected ByteBuf toByteBuf(Boolean value, int size) {
        return Unpooled.buffer(getSize()).writeBoolean(value);
    }

    @Override
    protected Boolean toValue(ByteBuf byteBuf) {
        return byteBuf.readBoolean();
    }
}
