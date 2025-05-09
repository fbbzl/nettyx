package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * The type Cuint.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 15:50
 */
public class cuint extends CBasic<Long> {

    public static final cuint
            MIN_VALUE = new cuint(0L),
            MAX_VALUE = new cuint(Integer.MAX_VALUE * 2L + 1);

    /**
     * Instantiates a new Cuint.
     *
     * @param value the length
     */
    public cuint(Long value) {
        super(value, 4);
    }

    /**
     * Instantiates a new Cuint.
     *
     * @param buf the buf
     */
    public cuint(ByteBuf buf) {
        super(buf, 4);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(Long value, int size) {
        return Unpooled.buffer(size).writeIntLE(value.intValue());
    }

    @Override
    protected Long toValue(ByteBuf byteBuf) {
        return byteBuf.readUnsignedIntLE();
    }
}
