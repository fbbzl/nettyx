package org.fz.nettyx.serializer.typed.c.unsigned;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;

/**
 * The type Culonglong.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class Culonglong extends Culong8 {

    /**
     * The constant MIN_VALUE.
     */
    public static final Culonglong MIN_VALUE = new Culonglong(BigInteger.ZERO);

    /**
     * The constant MAX_VALUE.
     */
    public static final Culonglong MAX_VALUE = new Culonglong(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Long.MAX_VALUE)));

    /**
     * Instantiates a new Culonglong.
     *
     * @param value the value
     */
    public Culonglong(BigInteger value) {
        super(value);
    }

    /**
     * Instantiates a new Culonglong.
     *
     * @param buf the buf
     */
    public Culonglong(ByteBuf buf) {
        super(buf);
    }
}
