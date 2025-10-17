package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

/**
 * this type in C language is unsigned short
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class cushort extends cbasic<Integer> {

    public static final cushort
            MIN_VALUE = new cushort(0),
            MAX_VALUE = new cushort(Short.MAX_VALUE * 2 + 1);

    public cushort(Integer value) {
        super(value, 2);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    public cushort(ByteBuf buf) {
        super(buf, 2);
    }

    public static cushort of(Integer value) {
        return new cushort(value);
    }

    @Override
    protected ByteBuf toByteBuf(Integer value, int size) {
        return Unpooled.buffer(size).writeShortLE(value.shortValue());
    }

    @Override
    protected Integer toValue(ByteBuf byteBuf) {
        return byteBuf.readUnsignedShortLE();
    }

}
