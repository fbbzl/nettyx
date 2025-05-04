package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * The type Cint64.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cint64T extends CBasic<Long> {

    public static final Cint64T
            MIN_VALUE = new Cint64T(Long.MIN_VALUE),
            MAX_VALUE = new Cint64T(Long.MAX_VALUE);

    public Cint64T(Long value) {
        super(value, 8);
    }

    public Cint64T(ByteBuf buf) {
        super(buf, 8);
    }

    @Override
    protected ByteBuf toByteBuf(Long value, int size) {
        return Unpooled.buffer(size).writeLongLE(value);
    }

    @Override
    protected Long toValue(ByteBuf byteBuf) {
        return byteBuf.readLong();
    }
}