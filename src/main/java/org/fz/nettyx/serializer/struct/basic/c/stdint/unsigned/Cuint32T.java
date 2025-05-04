package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * The type Cuint32.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cuint32T extends CBasic<Integer> {

    public static final Cuint32T
            MIN_VALUE = new Cuint32T(0),
            MAX_VALUE = new Cuint32T(0xFFFFFFFF);

    public Cuint32T(Integer value) {
        super(value, 4);
    }

    public Cuint32T(ByteBuf buf) {
        super(buf, 4);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(Integer value, int size) {
        return Unpooled.buffer(size).writeIntLE(value);
    }

    @Override
    protected Integer toValue(ByteBuf byteBuf) {
        return byteBuf.readInt();
    }
}