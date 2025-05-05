package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong8;

/**
 * The type Cint64.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cint64T extends Clong8 {

    public static final Cint64T
            MIN_VALUE = new Cint64T(Long.MIN_VALUE),
            MAX_VALUE = new Cint64T(Long.MAX_VALUE);

    public Cint64T(Long value) {
        super(value);
    }

    public Cint64T(ByteBuf buf) {
        super(buf);
    }
}