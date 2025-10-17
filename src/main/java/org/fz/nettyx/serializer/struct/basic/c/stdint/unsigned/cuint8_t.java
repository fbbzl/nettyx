package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

/**
 * this type in C language is unit8_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cuint8_t extends cbasic<Short> {

    public static final cuint8_t
            MIN_VALUE = new cuint8_t(0),
            MAX_VALUE = new cuint8_t(Byte.MAX_VALUE * 2 + 1);

    public cuint8_t(Integer value) {
        super(value.shortValue(), 1);
    }

    public cuint8_t(ByteBuf buf) {
        super(buf, 1);
    }

    public static cuint8_t of(Integer value) {
        return new cuint8_t(value);
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