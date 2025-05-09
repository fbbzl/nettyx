package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;

/**
 * this type in C language is unit8_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cuint8_t extends cuchar {

    public static final cuint8_t
            MIN_VALUE = new cuint8_t(0),
            MAX_VALUE = new cuint8_t(Byte.MAX_VALUE * 2 + 1);

    public cuint8_t(Integer value) {
        super(value);
    }

    public cuint8_t(ByteBuf buf) {
        super(buf);
    }
}