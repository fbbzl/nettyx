package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuint;

/**
 * The type Cuint32.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cuint32_t extends cuint {

    public static final cuint32_t
            MIN_VALUE = new cuint32_t(0L),
            MAX_VALUE = new cuint32_t(Integer.MAX_VALUE * 2L + 1);

    public cuint32_t(Long value) {
        super(value);
    }

    public cuint32_t(ByteBuf buf) {
        super(buf);
    }
}