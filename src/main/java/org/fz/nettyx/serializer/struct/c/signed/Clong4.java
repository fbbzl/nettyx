package org.fz.nettyx.serializer.struct.c.signed;

import io.netty.buffer.ByteBuf;

/**
 * The type Clong 4.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class Clong4 extends Cint {

    /**
     * The constant MIN_VALUE.
     */
    public static final Clong4 MIN_VALUE = new Clong4(Integer.MIN_VALUE);

    /**
     * The constant MAX_VALUE.
     */
    public static final Clong4 MAX_VALUE = new Clong4(Integer.MAX_VALUE);

    /**
     * Instantiates a new Clong 4.
     *
     * @param value the value
     */
    public Clong4(Integer value) {
        super(value);
    }

    /**
     * Instantiates a new Clong 4.
     *
     * @param buf the buf
     */
    public Clong4(ByteBuf buf) {
        super(buf);
    }
}
