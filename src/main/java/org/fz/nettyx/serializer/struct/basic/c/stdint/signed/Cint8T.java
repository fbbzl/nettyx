package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cchar;

/**
 * The type Cint8.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cint8T extends Cchar {

    public static final Cint8T
            MIN_VALUE = new Cint8T(Integer.valueOf(Byte.MIN_VALUE)),
            MAX_VALUE = new Cint8T(Integer.valueOf(Byte.MAX_VALUE));

    public Cint8T(Integer value) {
        super(value);
    }

    public Cint8T(ByteBuf buf) {
        super(buf);
    }

}