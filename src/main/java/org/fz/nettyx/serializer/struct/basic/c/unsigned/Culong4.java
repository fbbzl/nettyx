package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;

/**
 * The type Culong 4.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class Culong4 extends Cuint {

    /**
     * The constant MIN_VALUE.
     */
    public static final Culong4 MIN_VALUE = new Culong4(0L);

    /**
     * The constant MAX_VALUE.
     */
    public static final Culong4 MAX_VALUE = new Culong4(Long.valueOf(Integer.MAX_VALUE >> 2));

    /**
     * Instantiates a new Culong 4.
     *
     * @param value the length
     */
    public Culong4(Object value) {
        super(value);
    }

    /**
     * Instantiates a new Culong 4.
     *
     * @param buf the buf
     */
    public Culong4(ByteBuf buf) {
        super(buf);
    }
}
