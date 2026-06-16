package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is float
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class cfloat extends cbasic<Float> {

    public cfloat(Float value) {
        super(value);
    }

    public cfloat(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public int size() { return 4; }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeFloatLE(value);
        else
            writingBuf.writeFloat(value);
    }

    @Override
    protected Float read(ByteBuf readingBuf, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return readingBuf.readFloatLE();
        else
            return readingBuf.readFloat();
    }
}
