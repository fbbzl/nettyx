package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;

import java.nio.ByteOrder;

/**
 * this type in Cpp language is int
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:14
 */
public class cppint extends cint {

    public cppint(ByteOrder byteOrder, Integer value) {
        super(byteOrder, value);
    }

    public cppint(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf);
    }

}
