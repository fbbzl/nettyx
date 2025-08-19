package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;

/**
 * this type in C language is unsigned long4
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class culong4 extends cuint {

    public static final culong4
            MIN_VALUE = new culong4(0L),
            MAX_VALUE = new culong4(Integer.MAX_VALUE * 2L + 1);

    public culong4(Long value) {
        super(value);
    }

    public culong4(ByteBuf buf) {
        super(buf);
    }

    public static culong4 of(Long value) {
        return new culong4(value);
    }
}
