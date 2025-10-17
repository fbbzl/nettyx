package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

/**
 * this type in C language is unsigned int
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 15:50
 */
public class cuint extends cbasic<Long> {

    public static final cuint
            MIN_VALUE = new cuint(0L),
            MAX_VALUE = new cuint(Integer.MAX_VALUE * 2L + 1);

    public cuint(Long value) {
        super(value, 4);
    }

    public cuint(ByteBuf buf) {
        super(buf, 4);
    }

    public static cuint of(Long value) {
        return new cuint(value);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(Long value, int size) {
        return Unpooled.buffer(size).writeIntLE(value.intValue());
    }

    @Override
    protected Long toValue(ByteBuf byteBuf) {
        return byteBuf.readUnsignedIntLE();
    }
}
