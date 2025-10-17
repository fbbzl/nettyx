package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

/**
 * this type in C language is int
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class cint extends cbasic<Integer> {

    public static final cint
            MIN_VALUE = new cint(Integer.MIN_VALUE),
            MAX_VALUE = new cint(Integer.MAX_VALUE);

    public cint(Integer value) {
        super(value, 4);
    }

    public static cint of(Integer value) {
        return new cint(value);
    }

    public cint(ByteBuf buf) {
        super(buf, 4);
    }

    @Override
    protected ByteBuf toByteBuf(Integer value) {
        return Unpooled.buffer(size).writeIntLE(value);
    }

    @Override
    protected Integer toValue(ByteBuf byteBuf) {
        return byteBuf.readIntLE();
    }
}
