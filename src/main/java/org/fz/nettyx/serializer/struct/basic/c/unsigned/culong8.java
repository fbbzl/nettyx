package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import cn.hutool.core.util.PrimitiveArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.math.BigInteger;
import java.nio.ByteOrder;

/**
 * this type in C language is unsigned long8
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class culong8 extends Cbasic<BigInteger> {

    public culong8(BigInteger value) {
        super(value, 8);
    }

    public culong8(ByteBuf buf) {
        super(buf, 8);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(BigInteger value, ByteOrder byteOrder) {
        byte[] byteArray = value.toByteArray();

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            byteArray = PrimitiveArrayUtil.reverse(byteArray);
        }

        return Unpooled.buffer(size).writeBytes(byteArray);
    }

    @Override
    protected BigInteger toValue(ByteBuf byteBuf, ByteOrder byteOrder) {
        byte[] byteArray = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(byteArray);

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            byteArray = PrimitiveArrayUtil.reverse(byteArray);
        }

        // the no sign in BigInteger is 1
        final int noSign = 1;
        return new BigInteger(noSign, byteArray);
    }

}
