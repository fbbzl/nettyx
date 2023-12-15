package org.fz.nettyx.serializer.type.c.unsigned;

import io.netty.buffer.ByteBuf;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/18 13:30
 */
public class Culong4 extends Cuint {

    public Culong4(Long value) {
        super(value);
    }

    public Culong4(ByteBuf buf) {
        super(buf);
    }
}
