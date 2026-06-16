package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is unit8_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cuint8_t extends cbasic<Short> {

    public cuint8_t(Integer value) {
        super(value.shortValue());
    }

    public cuint8_t(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public boolean hasSigned() {
        return false;
    }

    @Override
    public int size() { return 1; }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
        writingBuf.writeByte(value);
    }

    @Override
    protected Short read(ByteBuf readingBuf, ByteOrder byteOrder) {
        return readingBuf.readUnsignedByte();
    }

}
