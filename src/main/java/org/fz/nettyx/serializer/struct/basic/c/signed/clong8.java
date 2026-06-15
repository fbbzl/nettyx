package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is long8
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:29
 */
public class clong8 extends Cbasic<Long> {

    public clong8(Long value) {
        super(value, 8);
    }

    public clong8(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 8);
    }

    @Override
    public void write(ByteBuf writingBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeLongLE(value);
        else
            writingBuf.writeLong(value);
    }

    @Override
    protected Long read(ByteBuf byteBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return byteBuf.readLongLE();
        else
            return byteBuf.readLong();
    }

}
