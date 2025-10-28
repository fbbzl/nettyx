package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;

/**
 * this type in Cpp language is long4
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:15
 */
public class cpplong4 extends cppint {

    public cpplong4(ByteOrder byteOrder, Integer value) {
        super(byteOrder, value);
    }

    public cpplong4(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf);
    }

}
