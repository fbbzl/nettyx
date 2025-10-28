package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is double
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class cdouble extends cbasic<Double> {

    public cdouble(ByteOrder byteOrder, Double value) {
        super(byteOrder, value, 8);
    }

    public cdouble(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 8);
    }

    @Override
    protected ByteBuf toByteBuf(Double value) {
        return Unpooled.buffer(size).writeDoubleLE(value);
    }

    @Override
    protected Double toValue(ByteBuf byteBuf) {
        return byteBuf.readDoubleLE();
    }
}
