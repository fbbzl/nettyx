package org.fz.nettyx.serializer.type.c.unsigned;

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
     * Instantiates a new Culong 4.
     *
     * @param value the value
     */
    public Culong4(Long value) {
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
