package org.fz.nettyx.serializer.struct.c.signed;

import io.netty.buffer.ByteBuf;

/**
 * The type Clonglong.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class Clonglong extends Clong8 {

    /**
     * The constant MIN_VALUE.
     */
    public static final Clonglong MIN_VALUE = new Clonglong(Long.MIN_VALUE);

    /**
     * The constant MAX_VALUE.
     */
    public static final Clonglong MAX_VALUE = new Clonglong(Long.MAX_VALUE);

    /**
     * Instantiates a new Clonglong.
     *
     * @param value the length
     */
    public Clonglong(Long value) {
        super(value);
    }

    /**
     * Instantiates a new Clonglong.
     *
     * @param buf the buf
     */
    public Clonglong(ByteBuf buf) {
        super(buf);
    }
}
