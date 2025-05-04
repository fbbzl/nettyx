package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cint;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:14
 */
public class Cppint extends Cint {

    public static final Cppint
            MIN_VALUE = new Cppint(Integer.MIN_VALUE),
            MAX_VALUE = new Cppint(Integer.MAX_VALUE);

    public Cppint(Integer value) {
        super(value);
    }

    public Cppint(ByteBuf buf) {
        super(buf);
    }

}
