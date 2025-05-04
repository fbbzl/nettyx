package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * The type Cint32.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cint32T extends CBasic<Integer> {

    public static final Cint32T
            MIN_VALUE = new Cint32T(Integer.MIN_VALUE),
            MAX_VALUE = new Cint32T(Integer.MAX_VALUE);

    public Cint32T(Integer value) {
        super(value, 4);
    }

    public Cint32T(ByteBuf buf) {
        super(buf, 4);
    }

    @Override
    protected ByteBuf toByteBuf(Integer value, int size) {
        return Unpooled.buffer(size).writeIntLE(value);
    }

    @Override
    protected Integer toValue(ByteBuf byteBuf) {
        return byteBuf.readInt();
    }
}