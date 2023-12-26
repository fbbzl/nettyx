package org.fz.nettyx.serializer.type.c.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.type.c.CBasic;

/**
 * The type Cuint.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 15:50
 */
public class Cuint extends CBasic<Long> {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cuint MIN_VALUE = new Cuint(0L);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cuint MAX_VALUE = new Cuint(Long.valueOf(Integer.MAX_VALUE >> 2));

    /**
     * Instantiates a new Cuint.
     *
     * @param value the value
     */
    public Cuint(Long value) {
        super(value, 4);
    }

    /**
     * Instantiates a new Cuint.
     *
     * @param buf the buf
     */
    public Cuint(ByteBuf buf) {
        super(buf, 4);
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
