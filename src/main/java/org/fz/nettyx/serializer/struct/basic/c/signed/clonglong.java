package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;

/**
 * this type in C language is longlong
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class clonglong extends clong8 {

    public clonglong(Long value) {
        super(value);
    }

    public clonglong(ByteBuf buf) {
        super(buf);
    }
}
