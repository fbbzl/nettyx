package org.fz.nettyx.serializer.type.c.signed;

import io.netty.buffer.ByteBuf;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/18 13:30
 */
public class Clonglong extends Clong8 {

    public Clonglong(Long value) {
        super(value);
    }

    public Clonglong(ByteBuf buf) {
        super(buf);
    }
}
