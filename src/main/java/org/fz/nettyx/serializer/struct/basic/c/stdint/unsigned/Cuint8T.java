package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.nio.ByteOrder;

/**
 * The type Cuint8.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cuint8T extends Basic<Byte> {

    public static final Cuint8T MIN_VALUE = new Cuint8T((byte) 0);
    public static final Cuint8T MAX_VALUE = new Cuint8T((byte) 0xFF);

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