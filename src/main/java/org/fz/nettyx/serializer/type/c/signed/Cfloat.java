package org.fz.nettyx.serializer.type.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.type.c.CBasic;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:39
 */
public class Cfloat extends CBasic<Float> {

    public Cfloat(Float value) {
        super(value, 4);
    }

    public Cfloat(ByteBuf buf) {
        super(buf, 4);
    }

    @Override
    protected ByteBuf toByteBuf(Float value, int size) {
        return Unpooled.buffer(size).writeFloatLE(value);
    }

    @Override
    protected Float toValue(ByteBuf byteBuf) {
        return byteBuf.readFloatLE();
    }
}
