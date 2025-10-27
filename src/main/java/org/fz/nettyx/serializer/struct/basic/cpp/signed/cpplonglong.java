package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;

/**
 * this type in Cpp language is longlong
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:16
 */
public class cpplonglong extends cpplong8 {

    public cpplonglong(Long value, ByteOrder byteOrder) {
        super(value, byteOrder);
    }

    public cpplonglong(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

}
