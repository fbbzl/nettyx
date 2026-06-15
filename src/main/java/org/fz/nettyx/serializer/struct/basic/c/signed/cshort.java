package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is short
 *
 * @author fengbinbin
 * @version 1.0
 */
public class cshort extends Cbasic<Short> {

    public cshort(Integer value) {
        super(value.shortValue(), 2);
    }

    public cshort(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 2);
    }

    @Override
    public void write(ByteBuf writingBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeShortLE(value);
        else
            writingBuf.writeShort(value);
    }

    @Override
    protected Short read(ByteBuf byteBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return byteBuf.readShortLE();
        else
            return byteBuf.readShort();
    }
}
