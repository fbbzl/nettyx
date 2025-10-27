package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is short
 *
 * @author fengbinbin
 * @version 1.0
 */
public class cshort extends cbasic<Short> {

    public cshort(Integer value, ByteOrder byteOrder) {
        super(value.shortValue(), 2, byteOrder);
    }

    public cshort(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, 2, byteOrder);
    }

    @Override
    protected ByteBuf toByteBuf(Short value) {
        return Unpooled.buffer(size).writeShortLE(value);
    }

    @Override
    protected Short toValue(ByteBuf byteBuf) {
        return byteBuf.readShortLE();
    }
}
