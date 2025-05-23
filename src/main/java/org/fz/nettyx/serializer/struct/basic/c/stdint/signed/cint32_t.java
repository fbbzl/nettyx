package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;

/**
 * this type in C language is int32_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cint32_t extends cint {

    public static final cint32_t
            MIN_VALUE = new cint32_t(Integer.MIN_VALUE),
            MAX_VALUE = new cint32_t(Integer.MAX_VALUE);

    public cint32_t(Integer value) {
        super(value);
    }

    public cint32_t(ByteBuf buf) {
        super(buf);
    }

    public static cint32_t of(Integer value) {
        return new cint32_t(value);
    }
}