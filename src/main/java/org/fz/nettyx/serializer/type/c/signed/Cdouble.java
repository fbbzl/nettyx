package org.fz.nettyx.serializer.type.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.type.c.CBasic;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:39
 */
public class Cdouble extends CBasic<Double> {

    public Cdouble(Double value) {
        super(value, 8);
    }

    public Cdouble(ByteBuf buf) {
        super(buf, 8);
    }

    @Override
    protected ByteBuf toByteBuf(Double value, int size) {
        return Unpooled.buffer(size).writeDoubleLE(value);
    }

    @Override
    protected Double toValue(ByteBuf byteBuf) {
        return byteBuf.readDoubleLE();
    }
}
