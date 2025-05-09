package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * The type Cshort.
 *
 * @author fengbinbin
 * @version 1.0
 */
public class cshort extends CBasic<Short> {

    public static final cshort
            MIN_VALUE = new cshort(Integer.valueOf(Short.MIN_VALUE)),
            MAX_VALUE = new cshort(Integer.valueOf(Short.MAX_VALUE));

    /**
     * Instantiates a new Cshort.
     *
     * @param value the length
     */
    public cshort(Integer value) {
        super(value.shortValue(), 2);
    }

    /**
     * Instantiates a new Cshort.
     *
     * @param buf the buf
     */
    public cshort(ByteBuf buf) {
        super(buf, 2);
    }

    @Override
    protected ByteBuf toByteBuf(Short value, int size) {
        return Unpooled.buffer(size).writeShortLE(value);
    }

    @Override
    protected Short toValue(ByteBuf byteBuf) {
        return byteBuf.readShortLE();
    }
}
