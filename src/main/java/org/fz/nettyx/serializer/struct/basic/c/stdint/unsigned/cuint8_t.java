package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is unit8_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cuint8_t extends cbasic<Short> {

    public cuint8_t(Integer value, ByteOrder byteOrder) {
        super(value.shortValue(), 1, byteOrder);
    }

    public cuint8_t(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, 1, byteOrder);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(Short value) {
        return Unpooled.buffer(size).writeByte(value.byteValue());
    }

    @Override
    protected Short toValue(ByteBuf byteBuf) {
        return byteBuf.readUnsignedByte();
    }

}