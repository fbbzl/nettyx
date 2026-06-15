package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
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

    public cint8_t(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 1);
    }

    @Override
    public void write(ByteBuf writingBuf) {
        writingBuf.writeByte(value);
    }

    @Override
    protected Byte read(ByteBuf byteBuf) {
        return byteBuf.readByte();
    }

}
