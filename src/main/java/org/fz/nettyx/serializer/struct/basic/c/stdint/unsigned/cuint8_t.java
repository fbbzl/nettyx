package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is unit8_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cuint8_t extends Cbasic<Short> {

    public cuint8_t(Integer value) {
        super(value.shortValue(), 1);
    }

    public cuint8_t(ByteBuf buf) {
        super(buf, 1);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(Short value, ByteOrder byteOrder) {
        return Unpooled.buffer(size).writeByte(value);
    }

    @Override
    protected Short toValue(ByteBuf byteBuf, ByteOrder byteOrder) {
        return byteBuf.readUnsignedByte();
    }

}