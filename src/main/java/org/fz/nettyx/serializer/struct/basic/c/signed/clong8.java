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

    public clong8(Long value, ByteOrder byteOrder) {
        super(value, 8, byteOrder);
    }

    public clong8(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, 8, byteOrder);
    }

    @Override
    protected ByteBuf toByteBuf(Long value) {
        return Unpooled.buffer(size).writeLongLE(value);
    }

    @Override
    protected Long toValue(ByteBuf byteBuf) {
        return byteBuf.readLongLE();
    }

}
