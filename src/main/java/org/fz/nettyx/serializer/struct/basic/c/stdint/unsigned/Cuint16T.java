package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.nio.ByteOrder;

/**
 * The type Cuint16.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cuint16T extends Basic<Short> {

    public static final Cuint16T MIN_VALUE = new Cuint16T((short) 0);
    public static final Cuint16T MAX_VALUE = new Cuint16T((short) 0xFFFF);

    public Cuint16T(Short value) {
        super(value, 2);
    }

    public Cuint16T(ByteBuf buf) {
        super(buf, 2);
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
    protected ByteBuf toByteBuf(Short value, int size) {
        ByteBuf buffer = Unpooled.buffer(size);
        buffer.order(order());
        buffer.writeShort(value);
        return buffer;
    }

    @Override
    protected Short toValue(ByteBuf byteBuf) {
        return byteBuf.readShort();
    }
}