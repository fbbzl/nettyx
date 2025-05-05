package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cshort;

/**
 * The type Cint16.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cint16T extends Cshort {

    public static final Cint16T
            MIN_VALUE = new Cint16T(Integer.valueOf(Short.MIN_VALUE)),
            MAX_VALUE = new Cint16T(Integer.valueOf(Short.MAX_VALUE));

    public Cint16T(Integer value) {
        super(value);
    }

    public Cint16T(ByteBuf buf) {
        super(buf);
    }

}