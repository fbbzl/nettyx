package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;

/**
 * this type in C language is longlong
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class clonglong extends clong8 {

    public clonglong(ByteOrder byteOrder, Long value) {
        super(byteOrder, value);
    }

    public clonglong(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf);
    }
}
