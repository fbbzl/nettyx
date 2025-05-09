package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * this type in C language is long8
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:29
 */
public class clong8 extends CBasic<Long> {

    public static final clong8
            MIN_VALUE = new clong8(Long.MIN_VALUE),
            MAX_VALUE = new clong8(Long.MAX_VALUE);

    /**
     * Instantiates a new Clong 8.
     *
     * @param value the length
     */
    public clong8(Long value) {
        super(value, 8);
    }

    /**
     * Instantiates a new Clong 8.
     *
     * @param buf the buf
     */
    public clong8(ByteBuf buf) {
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
