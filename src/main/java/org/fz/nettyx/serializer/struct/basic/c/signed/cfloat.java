package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * this type in C language is float
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class cfloat extends CBasic<Float> {

    public static final cfloat
            MIN_VALUE = new cfloat(Float.MIN_VALUE),
            MAX_VALUE = new cfloat(Float.MAX_VALUE);

    /**
     * Instantiates a new Cfloat.
     *
     * @param value the length
     */
    public cfloat(Float value) {
        super(value, 4);
    }

    /**
     * Instantiates a new Cfloat.
     *
     * @param buf the buf
     */
    public cfloat(ByteBuf buf) {
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
