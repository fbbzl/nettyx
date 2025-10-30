package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is int8_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cint8_t extends Cbasic<Byte> {

    public cint8_t(Integer value) {
        super(value.byteValue(), 1);
    }

    public cint8_t(ByteBuf buf) {
        super(buf, 1);
    }

    @Override
    protected ByteBuf toByteBuf(Byte value, ByteOrder byteOrder) {
        return Unpooled.buffer(size).writeByte(value);
    }

    @Override
    protected Byte toValue(ByteBuf byteBuf, ByteOrder byteOrder) {
        return byteBuf.readByte();
    }

}