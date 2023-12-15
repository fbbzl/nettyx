package org.fz.nettyx.serializer.type.c.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigInteger;
import org.fz.nettyx.serializer.type.c.CBasic;

/**
 * The type Culong 8.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class Culong8 extends CBasic<BigInteger> {

    /**
     * The constant MIN_VALUE.
     */
    public static final Culong8 MIN_VALUE = new Culong8(BigInteger.ZERO);

    /**
     * The constant MAX_VALUE.
     */
    public static final Culong8 MAX_VALUE = new Culong8(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Long.MAX_VALUE)));

    /**
     * Instantiates a new Culong 8.
     *
     * @param value the value
     */
    public Culong8(BigInteger value) {
        super(value, 8);
    }

    /**
     * Instantiates a new Culong 8.
     *
     * @param buf the buf
     */
    public Culong8(ByteBuf buf) {
        super(buf, 8);
    }

    @Override
    protected ByteBuf toByteBuf(BigInteger value, int size) {
        byte[] bytes = this.reverseOrder(value.toByteArray());
        return Unpooled.buffer(size).writeBytes(bytes);
    }

    @Override
    protected BigInteger toValue(ByteBuf byteBuf) {
        final byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        // the no sign value in BigInteger is 1
        final int noSign = 1;
        return new BigInteger(noSign, this.reverseOrder(bytes));
    }

    /**
     * reverse bytes order
     *
     * @param bytes the bytes
     * @return the byte [ ]
     */
    protected byte[] reverseOrder(byte[] bytes) {
        for (int i = 0, j = bytes.length - 1; i < bytes.length / 2; i++, j--) {
            byte stage = bytes[i];
            bytes[i] = bytes[j];
            bytes[j] = stage;
        }
        return bytes;
    }

}
