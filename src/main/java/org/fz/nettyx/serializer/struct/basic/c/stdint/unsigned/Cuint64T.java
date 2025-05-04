package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.nio.ByteOrder;

/**
 * The type Cuint64.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cuint64T extends Basic<Long> {

    public static final Cuint64T MIN_VALUE = new Cuint64T(0L);
    public static final Cuint64T MAX_VALUE = new Cuint64T(0xFFFFFFFFFFFFFFFFL);

    public Cuint64T(Long value) {
        super(value, 8);
    }

    public Cuint64T(ByteBuf buf) {
        super(buf, 8);
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
    protected ByteBuf toByteBuf(Long value, int size) {
        ByteBuf buffer = Unpooled.buffer(size);
        buffer.order(order());
        buffer.writeLong(value);
        return buffer;
    }

    @Override
    protected Long toValue(ByteBuf byteBuf) {
        return byteBuf.readLong();
    }
}