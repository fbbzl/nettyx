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

    public cuint(ByteOrder byteOrder, Long value) {
        super(byteOrder, value, 4);
    }

    public cuint(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 4);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(Long value, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return Unpooled.buffer(size).writeIntLE(value.intValue());
        else
            return Unpooled.buffer(size).writeInt(value.intValue());
    }

    @Override
    protected Long toValue(ByteBuf byteBuf, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return byteBuf.readUnsignedIntLE();
        else
            return byteBuf.readUnsignedInt();
    }
}
