package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

/**
 * this type in C language is long8
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:29
 */
public class clong8 extends cbasic<Long> {

    public static final clong8
            MIN_VALUE = new clong8(Long.MIN_VALUE),
            MAX_VALUE = new clong8(Long.MAX_VALUE);

    public clong8(Long value) {
        super(value, 8);
    }

    public clong8(ByteBuf buf) {
        super(buf, 8);
    }

    public static clong8 of(Long value) {
        return new clong8(value);
    }

    @Override
    protected ByteBuf toByteBuf(Long value) {
        return Unpooled.buffer(size).writeLongLE(value);
    }

    @Override
    protected Long toValue(ByteBuf byteBuf) {
        return byteBuf.readLongLE();
    }

}
