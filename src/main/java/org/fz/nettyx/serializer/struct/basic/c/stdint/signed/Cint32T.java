package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.nio.ByteOrder;

/**
 * The type Cint32.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cint32T extends Basic<Integer> {

    public static final Cint32T MIN_VALUE = new Cint32T(Integer.MIN_VALUE);
    public static final Cint32T MAX_VALUE = new Cint32T(Integer.MAX_VALUE);

    public Cint32T(Integer value) {
        super(value, 4);
    }

    public Cint32T(ByteBuf buf) {
        super(buf, 4);
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