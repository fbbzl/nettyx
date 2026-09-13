package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
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

    public cint8_t(Integer value) {
        super(value.byteValue());
    }

    public cint8_t(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public int size() { return 1; }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
        writingBuf.writeByte(value);
    }

    @Override
    protected Byte read(ByteBuf readingBuf, ByteOrder byteOrder) {
        return readingBuf.readByte();
    }

}
