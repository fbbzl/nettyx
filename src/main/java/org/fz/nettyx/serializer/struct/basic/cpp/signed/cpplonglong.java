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

    public cpplonglong(ByteOrder byteOrder, Long value) {
        super(byteOrder, value);
    }

    public cpplonglong(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf);
    }

}
