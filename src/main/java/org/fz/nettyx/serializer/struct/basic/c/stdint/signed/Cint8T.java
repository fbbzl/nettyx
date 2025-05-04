package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.nio.ByteOrder;

/**
 * The type Cint8.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cint8T extends Basic<Byte> {

    public static final Cint8T MIN_VALUE = new Cint8T(Byte.MIN_VALUE);
    public static final Cint8T MAX_VALUE = new Cint8T(Byte.MAX_VALUE);

    public Cint8T(Byte value) {
        super(value, 1);
    }

    public Cint8T(ByteBuf buf) {
        super(buf, 1);
    }

    @Override
    public boolean hasSinged() {
        return true;
    }

    @Override
    public ByteOrder order() {
        return ByteOrder.LITTLE_ENDIAN;
    }

    @Override
    protected ByteBuf toByteBuf(Byte value, int size) {
        ByteBuf buffer = Unpooled.buffer(size);
        buffer.writeByte(value);
        return buffer;
    }

    @Override
    protected Byte toValue(ByteBuf byteBuf) {
        return byteBuf.readByte();
    }
}