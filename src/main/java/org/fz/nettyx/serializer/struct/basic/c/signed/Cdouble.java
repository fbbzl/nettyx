package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * The type Cdouble.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class Cdouble extends CBasic<Double> {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cdouble MIN_VALUE = new Cdouble(Double.MIN_VALUE);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cdouble MAX_VALUE = new Cdouble(Double.MAX_VALUE);

    /**
     * Instantiates a new Cdouble.
     *
     * @param value the length
     */
    public Cdouble(Double value) {
        super(value, 8);
    }

    /**
     * Instantiates a new Cdouble.
     *
     * @param buf the buf
     */
    public Cdouble(ByteBuf buf) {
        super(buf, 8);
    }

    @Override
    protected ByteBuf toByteBuf(Object value, int size) {
        return Unpooled.buffer(size).writeDoubleLE((Double) value);
    }

    @Override
    protected Double toValue(ByteBuf byteBuf) {
        return byteBuf.readDoubleLE();
    }
}
