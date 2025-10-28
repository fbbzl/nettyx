package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is int8_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cint8_t extends cbasic<Byte> {

    public cint8_t(ByteOrder byteOrder, Integer value) {
        super(byteOrder, value.byteValue(), 1);
    }

    public cint8_t(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 1);
    }

    @Override
    protected ByteBuf toByteBuf(Byte value) {
        return Unpooled.buffer(size).writeByte(value);
    }

    @Override
    protected Byte toValue(ByteBuf byteBuf) {
        return byteBuf.readByte();
    }

}