package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;

/**
 * this type in Cpp language is unsigned long4
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class cppulong4 extends cppuint {

    public cppulong4(Long value, ByteOrder byteOrder) {
        super(value, byteOrder);
    }

    public cppulong4(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }
}
