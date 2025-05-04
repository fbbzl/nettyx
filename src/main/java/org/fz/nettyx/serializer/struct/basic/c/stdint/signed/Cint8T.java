package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * The type Cint8.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cint8T extends CBasic<Byte> {

    public static final Cint8T
            MIN_VALUE = new Cint8T(Byte.MIN_VALUE),
            MAX_VALUE = new Cint8T(Byte.MAX_VALUE);

    public Cint8T(Byte value) {
        super(value, 1);
    }

    public Cint8T(ByteBuf buf) {
        super(buf, 1);
    }

    @Override
    protected ByteBuf toByteBuf(Byte value, int size) {
        return Unpooled.buffer(size).writeByte(value);
    }

    @Override
    protected Byte toValue(ByteBuf byteBuf) {
        return byteBuf.readByte();
    }
}