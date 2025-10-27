package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is unsigned int
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 15:50
 */
public class cuint extends cbasic<Long> {

    public cuint(Long value, ByteOrder byteOrder) {
        super(value, 4, byteOrder);
    }

    public cuint(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, 4, byteOrder);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(Long value) {
        return Unpooled.buffer(size).writeIntLE(value.intValue());
    }

    @Override
    protected Long toValue(ByteBuf byteBuf) {
        return byteBuf.readUnsignedIntLE();
    }
}
