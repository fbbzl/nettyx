package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;

import java.nio.ByteOrder;

/**
 * this type in Cpp language is char8_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:12
 */
public class cppchar8_t extends cchar {

    public cppchar8_t(Integer value, ByteOrder byteOrder) {
        super(value, byteOrder);
    }

    public cppchar8_t(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

}
