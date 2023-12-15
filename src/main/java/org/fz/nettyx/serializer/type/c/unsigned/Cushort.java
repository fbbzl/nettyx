package org.fz.nettyx.serializer.type.c.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.type.c.CBasic;

/**
 * The type Cushort.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class Cushort extends CBasic<Integer> {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cushort MIN_VALUE = new Cushort(0);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cushort MAX_VALUE = new Cushort(Short.MAX_VALUE >> 2);

    /**
     * Instantiates a new Cushort.
     *
     * @param value the value
     */
    public Cushort(Integer value) {
        super(value, 2);
    }

    /**
     * Instantiates a new Cushort.
     *
     * @param buf the buf
     */
    public Cushort(ByteBuf buf) {
        super(buf, 2);
    }

    @Override
    protected ByteBuf toByteBuf(Integer value, int size) {
        return Unpooled.buffer(size).writeShortLE(value.shortValue());
    }

    @Override
    protected Integer toValue(ByteBuf byteBuf) {
        return byteBuf.readUnsignedShortLE();
    }

}
