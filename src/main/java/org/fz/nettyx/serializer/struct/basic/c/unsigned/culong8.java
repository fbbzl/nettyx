package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import cn.hutool.core.util.PrimitiveArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.math.BigInteger;
import java.nio.ByteOrder;

/**
 * this type in C language is unsigned long8
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class culong8 extends cbasic<BigInteger> {

    public culong8(ByteOrder byteOrder, BigInteger value) {
        super(byteOrder, value, 8);
    }

    public culong8(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 8);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(BigInteger value) {
        byte[] bytes = PrimitiveArrayUtil.reverse(value.toByteArray());
        return Unpooled.buffer(size).writeBytes(bytes);
    }

    @Override
    protected BigInteger toValue(ByteBuf byteBuf) {
        final byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        // the no sign length in BigInteger is 1
        final int noSign = 1;
        return new BigInteger(noSign, PrimitiveArrayUtil.reverse(bytes));
    }

}
