package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cuchar;

/**
 * The type Cuint8.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cuint8T extends Cuchar {

    public static final Cuint8T
            MIN_VALUE = new Cuint8T(0),
            MAX_VALUE = new Cuint8T(Byte.MAX_VALUE * 2 + 1);

    public Cuint8T(Integer value) {
        super(value);
    }

    public Cuint8T(ByteBuf buf) {
        super(buf);
    }
}