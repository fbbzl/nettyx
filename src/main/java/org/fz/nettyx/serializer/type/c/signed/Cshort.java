package org.fz.nettyx.serializer.type.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.type.c.CBasic;

/**
 * @author fengbinbin
 * @version 1.0
 */
public class Cshort extends CBasic<Short> {

    public Cshort(Integer value) {
        super(value.shortValue(), 2);
    }

    public Cshort(ByteBuf buf) {
        super(buf, 2);
    }

    @Override
    protected ByteBuf toByteBuf(Short value, int size) {
        return Unpooled.buffer(size).writeShortLE(value);
    }

    @Override
    protected Short toValue(ByteBuf byteBuf) {
        return byteBuf.readShortLE();
    }
}
