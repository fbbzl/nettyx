package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is double
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class cdouble extends Cbasic<Double> {

    public cdouble(Double value) {
        super(value, 8);
    }

    public cdouble(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 8);
    }

    @Override
    public void write(ByteBuf writingBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeDoubleLE(value);
        else
            writingBuf.writeDouble(value);
    }

    @Override
    protected Double read(ByteBuf byteBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return byteBuf.readDoubleLE();
        else
            return byteBuf.readDouble();
    }
}
