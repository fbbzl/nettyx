package org.fz.nettyx.serializer.struct.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.c.CBasic;

/**
 * The type Clong 8.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:29
 */
public class Clong8 extends CBasic<Long> {

    /**
     * The constant MIN_VALUE.
     */
    public static final Clong8 MIN_VALUE = new Clong8(Long.MIN_VALUE);

    /**
     * The constant MAX_VALUE.
     */
    public static final Clong8 MAX_VALUE = new Clong8(Long.MAX_VALUE);

    /**
     * Instantiates a new Clong 8.
     *
     * @param value the value
     */
    public Clong8(Long value) {
        super(value, 8);
    }

    /**
     * Instantiates a new Clong 8.
     *
     * @param buf the buf
     */
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
