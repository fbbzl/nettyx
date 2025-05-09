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
public class cppulonglong extends cppulong8 {

    public static final cppulonglong
            MIN_VALUE = new cppulonglong(BigInteger.ZERO),
            MAX_VALUE = new cppulonglong(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Long.MAX_VALUE)).add(BigInteger.ONE));

    /**
     * Instantiates a new Culonglong.
     *
     * @param value the length
     */
    public cppulonglong(BigInteger value) {
        super(value);
    }

    /**
     * Instantiates a new Culonglong.
     *
     * @param buf the buf
     */
    public cppulonglong(ByteBuf buf) {
        super(buf);
    }
}
