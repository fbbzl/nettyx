package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is long8
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:29
 */
public class clong8 extends cbasic<Long> {

    public clong8(ByteOrder byteOrder, Long value) {
        super(byteOrder, value, 8);
    }

    public clong8(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 8);
    }

    @Override
    protected ByteBuf toByteBuf(Long value) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return Unpooled.buffer(size).writeLongLE(value);
        else
            return Unpooled.buffer(size).writeLong(value);
    }

    @Override
    protected Long toValue(ByteBuf byteBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return byteBuf.readLongLE();
        else
            return byteBuf.readLong();
    }

}
