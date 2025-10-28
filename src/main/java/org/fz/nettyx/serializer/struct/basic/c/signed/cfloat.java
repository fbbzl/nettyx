package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

    public cfloat(ByteOrder byteOrder, Float value) {
        super(byteOrder, value, 4);
    }

    public cfloat(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 4);
    }

    @Override
    protected ByteBuf toByteBuf(Float value) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return Unpooled.buffer(size).writeFloatLE(value);
        else
            return Unpooled.buffer(size).writeFloat(value);
    }

    @Override
    protected Float toValue(ByteBuf byteBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return byteBuf.readFloatLE();
        else
            return byteBuf.readFloat();
    }
}
