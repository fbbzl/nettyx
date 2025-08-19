package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * this type in C language is int8_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cint8_t extends CBasic<Byte> {

    public static final cint8_t
            MIN_VALUE = new cint8_t(Integer.valueOf(Byte.MIN_VALUE)),
            MAX_VALUE = new cint8_t(Integer.valueOf(Byte.MAX_VALUE));

    public cint8_t(Integer value) {
        super(value.byteValue(), 1);
    }

    public cint8_t(ByteBuf buf) {
        super(buf, 1);
    }

    @Override
    protected ByteBuf toByteBuf(Byte value, int size) {
        return Unpooled.buffer(size).writeByte(value);
    }

    @Override
    protected Byte toValue(ByteBuf byteBuf) {
        return byteBuf.readByte();
    }

    public static cint8_t of(Integer value) {
        return new cint8_t(value);
    }

}