package org.fz.nettyx.serializer.type.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.type.c.CBasic;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/18 13:29
 */
public class Clong8 extends CBasic<Long> {

    public Clong8(Long value) {
        super(value, 8);
    }

    public Clong8(ByteBuf buf) {
        super(buf, 8);
    }

    @Override
    protected ByteBuf toByteBuf(Long value, int size) {
        return Unpooled.buffer(size).writeLongLE(value);
    }

    @Override
    protected Long toValue(ByteBuf byteBuf) {
        return byteBuf.readLongLE();
    }

}
