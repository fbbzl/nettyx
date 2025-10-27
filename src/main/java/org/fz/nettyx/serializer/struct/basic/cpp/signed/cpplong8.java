package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong8;

import java.nio.ByteOrder;

/**
 * this type in Cpp language is long8
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:16
 */
public class cpplong8 extends clong8 {

    public cpplong8(Long value, ByteOrder byteOrder) {
        super(value, byteOrder);
    }

    public cpplong8(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

}
