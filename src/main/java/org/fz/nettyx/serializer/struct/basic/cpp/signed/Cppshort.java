package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cshort;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:20
 */
public class Cppshort extends Cshort {

    public static final Cppshort
            MIN_VALUE = new Cppshort(Integer.valueOf(Short.MIN_VALUE)),
            MAX_VALUE = new Cppshort(Integer.valueOf(Short.MAX_VALUE));

    public Cppshort(Integer value) {
        super(value);
    }

    public Cppshort(ByteBuf buf) {
        super(buf);
    }

}
