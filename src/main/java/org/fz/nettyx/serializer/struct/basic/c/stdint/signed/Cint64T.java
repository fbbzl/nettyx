package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.nio.ByteOrder;

/**
 * The type Cint64.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cint64T extends Basic<Long> {

    public static final Cint64T MIN_VALUE = new Cint64T(Long.MIN_VALUE);
    public static final Cint64T MAX_VALUE = new Cint64T(Long.MAX_VALUE);

    public Cint64T(Long value) {
        super(value, 8);
    }

    public Cint64T(ByteBuf buf) {
        super(buf, 8);
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