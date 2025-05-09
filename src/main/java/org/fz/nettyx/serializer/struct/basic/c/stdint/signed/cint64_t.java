package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong8;

/**
 * this type in C language is int64_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cint64_t extends clong8 {

    public static final cint64_t
            MIN_VALUE = new cint64_t(Long.MIN_VALUE),
            MAX_VALUE = new cint64_t(Long.MAX_VALUE);

    public cint64_t(Long value) {
        super(value);
    }

    public cint64_t(ByteBuf buf) {
        super(buf);
    }
}