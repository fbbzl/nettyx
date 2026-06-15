package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is unit8_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cuint8_t extends Cbasic<Short> {

    public cuint8_t(Integer value) {
        super(value.shortValue(), 1);
    }

    public cuint8_t(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 1);
    }

    @Override
    public boolean hasSigned() {
        return false;
    }

    @Override
    public void write(ByteBuf writingBuf) {
        writingBuf.writeByte(value);
    }

    @Override
    protected Short read(ByteBuf byteBuf) {
        return byteBuf.readUnsignedByte();
    }

}
