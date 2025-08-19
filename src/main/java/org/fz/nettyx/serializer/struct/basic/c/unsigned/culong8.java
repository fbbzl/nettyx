package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import cn.hutool.core.util.PrimitiveArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

import java.math.BigInteger;

/**
 * this type in C language is unsigned long8
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class culong8 extends CBasic<BigInteger> {

    public static final culong8
            MIN_VALUE = new culong8(BigInteger.ZERO),
            MAX_VALUE = new culong8(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Long.MAX_VALUE)).add(BigInteger.ONE));

    public culong8(BigInteger value) {
        super(value, 8);
    }

    public culong8(ByteBuf buf) {
        super(buf, 8);
    }

    public static culong8 of(BigInteger value) {
        return new culong8(value);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(BigInteger value, int size) {
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
