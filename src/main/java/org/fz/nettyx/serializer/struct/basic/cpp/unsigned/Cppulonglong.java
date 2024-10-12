package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;

import java.math.BigInteger;

/**
 * The type Culonglong.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class Cppulonglong extends Cppulong8 {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cppulonglong MIN_VALUE = new Cppulonglong(BigInteger.ZERO);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cppulonglong MAX_VALUE = new Cppulonglong(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Long.MAX_VALUE)).add(BigInteger.ONE));

    /**
     * Instantiates a new Culonglong.
     *
     * @param value the length
     */
    public Cppulonglong(BigInteger value) {
        super(value);
    }

    /**
     * Instantiates a new Culonglong.
     *
     * @param buf the buf
     */
    public Cppulonglong(ByteBuf buf) {
        super(buf);
    }
}
