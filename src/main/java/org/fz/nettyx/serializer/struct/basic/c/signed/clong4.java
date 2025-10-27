package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;

/**
 * this type in C language is long4
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class clong4 extends cint {

    public clong4(Integer value, ByteOrder byteOrder) {
        super(value, byteOrder);
    }

    public clong4(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }
}
