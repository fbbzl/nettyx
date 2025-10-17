package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

/**
 * this type in C language is short
 *
 * @author fengbinbin
 * @version 1.0
 */
public class cshort extends cbasic<Short> {

    public static final cshort
            MIN_VALUE = new cshort(Integer.valueOf(Short.MIN_VALUE)),
            MAX_VALUE = new cshort(Integer.valueOf(Short.MAX_VALUE));

    public cshort(Integer value) {
        super(value.shortValue(), 2);
    }

    public cshort(ByteBuf buf) {
        super(buf, 2);
    }

    public static cshort of(Integer value) {
        return new cshort(value);
    }

    @Override
    protected ByteBuf toByteBuf(Short value) {
        return Unpooled.buffer(size).writeShortLE(value);
    }

    @Override
    protected Short toValue(ByteBuf byteBuf) {
        return byteBuf.readShortLE();
    }
}
