package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuint;

/**
 * The type Cuint.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 15:50
 */
public class cppuint extends cuint {

    public static final cppuint
            MIN_VALUE = new cppuint(0L),
            MAX_VALUE = new cppuint(Integer.MAX_VALUE * 2L + 1);

    public cppuint(Long value) {
        super(value);
    }

    public cppuint(ByteBuf buf) {
        super(buf);
    }
}
