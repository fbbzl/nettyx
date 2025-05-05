package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cuint;

/**
 * The type Cuint32.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cuint32T extends Cuint {

    public static final Cuint32T
            MIN_VALUE = new Cuint32T(0L),
            MAX_VALUE = new Cuint32T(Integer.MAX_VALUE * 2L + 1);

    public Cuint32T(Long value) {
        super(value);
    }

    public Cuint32T(ByteBuf buf) {
        super(buf);
    }
}