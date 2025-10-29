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

    public cppulong4(Long value) {
        super(value);
    }

    public cppulong4(ByteBuf buf) {
        super(buf);
    }
}
