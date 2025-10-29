package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cushort;

/**
 * this type in C language is unit16_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cuint16_t extends cushort {

    public cuint16_t(Integer value) {
        super(value);
    }

    public cuint16_t(ByteBuf buf) {
        super(buf);
    }
}