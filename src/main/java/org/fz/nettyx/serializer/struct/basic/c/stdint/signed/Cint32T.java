package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cint;

/**
 * The type Cint32.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cint32T extends Cint {

    public static final Cint32T
            MIN_VALUE = new Cint32T(Integer.MIN_VALUE),
            MAX_VALUE = new Cint32T(Integer.MAX_VALUE);

    public Cint32T(Integer value) {
        super(value);
    }

    public Cint32T(ByteBuf buf) {
        super(buf);
    }
}