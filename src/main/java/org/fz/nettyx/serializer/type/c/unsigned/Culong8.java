package org.fz.nettyx.serializer.type.c.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigInteger;
import org.fz.nettyx.serializer.type.c.CBasic;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/18 13:30
 */
public class Culong8 extends CBasic<BigInteger> {

    public Culong8(BigInteger value) {
        super(value, 8);
    }

    public Culong8(ByteBuf buf) {
        super(buf, 8);
    }

    @Override
    protected ByteBuf toByteBuf(BigInteger value, int size) {
        byte[] bytes = this.toBytesLE(value.toByteArray());
        return Unpooled.buffer(size).writeBytes(bytes);
    }

    @Override
    protected BigInteger toValue(ByteBuf byteBuf) {
        final byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        return new BigInteger(this.toBytesLE(bytes));
    }

    /**
     * convert BigInteger to LE bytes
     */
    protected byte[] toBytesLE(byte[] bytes) {
        for (int i = 0, j = bytes.length - 1; i < bytes.length / 2; i++, j--) {
            byte stage = bytes[i];
            bytes[i] = bytes[j];
            bytes[j] = stage;
        }
        return bytes;
    }

}
