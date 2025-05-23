package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;

/**
 * this type in C language is long4
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class clong4 extends cint {

    public static final clong4
            MIN_VALUE = new clong4(Integer.MIN_VALUE),
            MAX_VALUE = new clong4(Integer.MAX_VALUE);

    /**
     * Instantiates a new Clong 4.
     *
     * @param value the length
     */
    public clong4(Integer value) {
        super(value);
    }

    /**
     * Instantiates a new Clong 4.
     *
     * @param buf the buf
     */
    public clong4(ByteBuf buf) {
        super(buf);
    }

    public static clong4 of(Integer value) {
        return new clong4(value);
    }
}
