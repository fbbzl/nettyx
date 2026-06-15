package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is float
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class cfloat extends Cbasic<Float> {

    public cfloat(Float value) {
        super(value, 4);
    }

    public cfloat(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 4);
    }

    @Override
    public void write(ByteBuf writingBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeFloatLE(value);
        else
            writingBuf.writeFloat(value);
    }

    @Override
    protected Float read(ByteBuf byteBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return byteBuf.readFloatLE();
        else
            return byteBuf.readFloat();
    }
}
