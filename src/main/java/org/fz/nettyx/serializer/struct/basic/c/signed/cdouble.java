package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * this type in C language is double
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class cdouble extends CBasic<Double> {

    public static final cdouble
            MIN_VALUE = new cdouble(Double.MIN_VALUE),
            MAX_VALUE = new cdouble(Double.MAX_VALUE);

    /**
     * Instantiates a new Cdouble.
     *
     * @param value the length
     */
    public cdouble(Double value) {
        super(value, 8);
    }

    public static cdouble of(Double value) {
        return new cdouble(value);
    }

    /**
     * Instantiates a new Cdouble.
     *
     * @param buf the buf
     */
    public cdouble(ByteBuf buf) {
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
