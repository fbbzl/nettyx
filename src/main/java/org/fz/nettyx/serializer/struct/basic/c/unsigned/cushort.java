package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is unsigned short
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class cushort extends cbasic<Integer> {

    public cushort(ByteOrder byteOrder, Integer value) {
        super(byteOrder, value, 2);
    }

    public cushort(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 2);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(Integer value) {
        return Unpooled.buffer(size).writeShortLE(value.shortValue());
    }

    @Override
    protected Integer toValue(ByteBuf byteBuf) {
        return byteBuf.readUnsignedShortLE();
    }

}
