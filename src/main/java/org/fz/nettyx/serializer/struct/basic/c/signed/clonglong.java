package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;

/**
 * The type Clonglong.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class clonglong extends clong8 {

    public static final clonglong
            MIN_VALUE = new clonglong(Long.MIN_VALUE),
            MAX_VALUE = new clonglong(Long.MAX_VALUE);

    /**
     * Instantiates a new Clonglong.
     *
     * @param value the length
     */
    public clonglong(Long value) {
        super(value);
    }

    /**
     * Instantiates a new Clonglong.
     *
     * @param buf the buf
     */
    public clonglong(ByteBuf buf) {
        super(buf);
    }
}
