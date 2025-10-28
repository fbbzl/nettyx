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

    public cshort(ByteOrder byteOrder, Integer value) {
        super(byteOrder, value.shortValue(), 2);
    }

    public cshort(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 2);
    }

    @Override
    protected ByteBuf toByteBuf(Short value, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return Unpooled.buffer(size).writeShortLE(value);
        else
            return Unpooled.buffer(size).writeShort(value);
    }

    @Override
    protected Short toValue(ByteBuf byteBuf, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return byteBuf.readShortLE();
        else
            return byteBuf.readShort();
    }
}
