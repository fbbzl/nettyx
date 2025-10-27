package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;

import java.math.BigInteger;
import java.nio.ByteOrder;

/**
 * this type in C language is unsigned longlong
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class culonglong extends culong8 {

    public culonglong(BigInteger value, ByteOrder byteOrder) {
        super(value, byteOrder);
    }

    public culonglong(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }
}
