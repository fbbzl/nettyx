package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong8;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:16
 */
public class cpplong8 extends clong8 {

    public static final cpplong8
            MIN_VALUE = new cpplong8(Long.MIN_VALUE),
            MAX_VALUE = new cpplong8(Long.MAX_VALUE);

    public cpplong8(Long value) {
        super(value);
    }

    public cpplong8(ByteBuf buf) {
        super(buf);
    }

}
