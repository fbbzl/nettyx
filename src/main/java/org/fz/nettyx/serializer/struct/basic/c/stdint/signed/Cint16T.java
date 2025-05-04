package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * The type Cint16.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cint16T extends CBasic<Short> {

    public static final Cint16T
            MIN_VALUE = new Cint16T(Short.MIN_VALUE),
            MAX_VALUE = new Cint16T(Short.MAX_VALUE);

    public Cint16T(Short value) {
        super(value, 2);
    }

    public Cint16T(ByteBuf buf) {
        super(buf, 2);
    }

    @Override
    protected ByteBuf toByteBuf(Short value, int size) {
        return Unpooled.buffer(size).writeShortLE(value);
    }

    @Override
    protected Short toValue(ByteBuf byteBuf) {
        return byteBuf.readShort();
    }
}