package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * The type Cuint64.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cuint64T extends CBasic<Long> {

    public static final Cuint64T
            MIN_VALUE = new Cuint64T(0L),
            MAX_VALUE = new Cuint64T(0xFFFFFFFFFFFFFFFFL);

    public Cuint64T(Long value) {
        super(value, 8);
    }

    public Cuint64T(ByteBuf buf) {
        super(buf, 8);
    }

    @Override
    public boolean hasSinged() {
        return false;
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