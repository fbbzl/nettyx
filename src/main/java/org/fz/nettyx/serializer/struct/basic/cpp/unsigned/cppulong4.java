package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;

/**
 * this type in Cpp language is unsigned long4
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class cppulong4 extends cppuint {

    public static final cppulong4
            MIN_VALUE = new cppulong4(0L),
            MAX_VALUE = new cppulong4(Integer.MAX_VALUE * 2L + 1);

    public cppulong4(Long value) {
        super(value);
    }

    public cppulong4(ByteBuf buf) {
        super(buf);
    }

    public static cppulong4 of(Long value) {
        return new cppulong4(value);
    }
}
