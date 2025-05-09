package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;

import java.math.BigInteger;

/**
 * The type Culonglong.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class culonglong extends culong8 {

    public static final culonglong
            MIN_VALUE = new culonglong(BigInteger.ZERO),
            MAX_VALUE = new culonglong(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Long.MAX_VALUE)).add(BigInteger.ONE));

    /**
     * Instantiates a new Culonglong.
     *
     * @param value the length
     */
    public culonglong(BigInteger value) {
        super(value);
    }

    /**
     * Instantiates a new Culonglong.
     *
     * @param buf the buf
     */
    public culonglong(ByteBuf buf) {
        super(buf);
    }
}
