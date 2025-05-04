package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * The type Cuint8.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cuint8T extends CBasic<Byte> {

    public static final Cuint8T
            MIN_VALUE = new Cuint8T((byte) 0),
            MAX_VALUE = new Cuint8T((byte) 0xFF);

    public Cuint8T(Byte value) {
        super(value, 1);
    }

    public Cuint8T(ByteBuf buf) {
        super(buf, 1);
    }

    @Override
    public boolean hasSinged() {
        return false;
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