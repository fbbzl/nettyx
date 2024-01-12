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
public class Cshort extends CBasic<Short> {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cshort MIN_VALUE = new Cshort((int) Short.MIN_VALUE);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cshort MAX_VALUE = new Cshort((int) Short.MAX_VALUE);

    /**
     * Instantiates a new Cshort.
     *
     * @param value the length
     */
    public Cshort(Object value) {
        super(value, 2);
    }

    /**
     * Instantiates a new Cshort.
     *
     * @param buf the buf
     */
    public Cshort(ByteBuf buf) {
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
