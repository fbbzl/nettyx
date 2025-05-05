package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cushort;

/**
 * The type Cuint16.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cuint16T extends Cushort {

    public static final Cuint16T
            MIN_VALUE = new Cuint16T(0),
            MAX_VALUE = new Cuint16T(Short.MAX_VALUE * 2 + 1);

    public Cuint16T(Integer value) {
        super(value);
    }

    public Cuint16T(ByteBuf buf) {
        super(buf);
    }
}