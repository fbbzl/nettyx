package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;

/**
 * this type in Cpp language is long4
 *
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

    public static cpplong4 of(Integer value) {
        return new cpplong4(value);
    }

}
