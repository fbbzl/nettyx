package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cdouble;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:13
 */
public class Cppdouble extends Cdouble {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cppdouble MIN_VALUE = new Cppdouble(Double.MIN_VALUE);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cppdouble MAX_VALUE = new Cppdouble(Double.MAX_VALUE);

    public Cppdouble(Double value) {
        super(value);
    }

    public Cppdouble(ByteBuf buf) {
        super(buf);
    }

}
