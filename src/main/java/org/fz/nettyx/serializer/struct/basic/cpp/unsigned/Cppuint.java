package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cuint;

/**
 * The type Cuint.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 15:50
 */
public class Cppuint extends Cuint {

    public static final Cppuint
            MIN_VALUE = new Cppuint(0L),
            MAX_VALUE = new Cppuint(Integer.MAX_VALUE * 2L + 1);

    public Cppuint(Long value) {
        super(value);
    }

    public Cppuint(ByteBuf buf) {
        super(buf);
    }
}
