package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:15
 */
public class cpplong4 extends cppint {

    public static final cpplong4
            MIN_VALUE = new cpplong4(Integer.MIN_VALUE),
            MAX_VALUE = new cpplong4(Integer.MAX_VALUE);

    public cpplong4(Integer value) {
        super(value);
    }

    public cpplong4(ByteBuf buf) {
        super(buf);
    }

}
