package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is int
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class cint extends cbasic<Integer> {

    public cint(Integer value, ByteOrder byteOrder) {
        super(value, 4, byteOrder);
    }

    public cint(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, 4, byteOrder);
    }

    @Override
    protected ByteBuf toByteBuf(Integer value) {
        return Unpooled.buffer(size).writeIntLE(value);
    }

    @Override
    protected Integer toValue(ByteBuf byteBuf) {
        return byteBuf.readIntLE();
    }
}
