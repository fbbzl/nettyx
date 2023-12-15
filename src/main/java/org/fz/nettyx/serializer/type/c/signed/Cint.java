package org.fz.nettyx.serializer.type.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.type.c.CBasic;

/**
 * The type Cint.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class Cint extends CBasic<Integer> {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cint MIN_VALUE = new Cint(Integer.MIN_VALUE);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cint MAX_VALUE = new Cint(Integer.MAX_VALUE);

    /**
     * Instantiates a new Cint.
     *
     * @param value the value
     */
    public Cint(Integer value) {
        super(value, 4);
    }

    /**
     * Instantiates a new Cint.
     *
     * @param buf the buf
     */
    public Cint(ByteBuf buf) {
        super(buf, 4);
    }

    @Override
    protected ByteBuf toByteBuf(Integer value, int size) {
        return Unpooled.buffer(size).writeIntLE(value);
    }

    @Override
    protected Integer toValue(ByteBuf byteBuf) {
        return byteBuf.readIntLE();
    }
}
