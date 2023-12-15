package org.fz.nettyx.serializer.typed.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.typed.c.CBasic;

/**
 * The type Cfloat.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class Cfloat extends CBasic<Float> {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cfloat MIN_VALUE = new Cfloat(Float.MIN_VALUE);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cfloat MAX_VALUE = new Cfloat(Float.MAX_VALUE);

    /**
     * Instantiates a new Cfloat.
     *
     * @param value the value
     */
    public Cfloat(Float value) {
        super(value, 4);
    }

    /**
     * Instantiates a new Cfloat.
     *
     * @param buf the buf
     */
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
