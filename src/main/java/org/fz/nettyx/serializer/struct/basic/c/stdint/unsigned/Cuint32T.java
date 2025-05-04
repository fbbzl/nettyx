package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.nio.ByteOrder;

/**
 * The type Cuint32.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cuint32T extends Basic<Integer> {

    public static final Cuint32T MIN_VALUE = new Cuint32T(0);
    public static final Cuint32T MAX_VALUE = new Cuint32T(0xFFFFFFFF);

    public Cuint32T(Integer value) {
        super(value, 4);
    }

    public Cuint32T(ByteBuf buf) {
        super(buf, 4);
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
    protected ByteBuf toByteBuf(Integer value, int size) {
        ByteBuf buffer = Unpooled.buffer(size);
        buffer.order(order());
        buffer.writeInt(value);
        return buffer;
    }

    @Override
    protected Integer toValue(ByteBuf byteBuf) {
        return byteBuf.readInt();
    }
}