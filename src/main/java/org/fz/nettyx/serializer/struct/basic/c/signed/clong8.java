package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is long8
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:29
 */
public class clong8 extends cbasic<Long> {

    public clong8(Long value) {
        super(value);
    }

    public clong8(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public int size() { return 8; }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeLongLE(value);
        else
            writingBuf.writeLong(value);
    }

    @Override
    protected Long read(ByteBuf readingBuf, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return readingBuf.readLongLE();
        else
            return readingBuf.readLong();
    }

}
