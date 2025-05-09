package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.cshort;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:20
 */
public class cppshort extends cshort {

    public static final cppshort
            MIN_VALUE = new cppshort(Integer.valueOf(Short.MIN_VALUE)),
            MAX_VALUE = new cppshort(Integer.valueOf(Short.MAX_VALUE));

    public cppshort(Integer value) {
        super(value);
    }

    public cppshort(ByteBuf buf) {
        super(buf);
    }

}
