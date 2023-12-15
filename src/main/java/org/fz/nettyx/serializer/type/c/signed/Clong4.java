package org.fz.nettyx.serializer.type.c.signed;

import io.netty.buffer.ByteBuf;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Clong4 extends Cint {

    public Clong4(Integer value) {
        super(value);
    }

    public Clong4(ByteBuf buf) {
        super(buf);
    }
}
