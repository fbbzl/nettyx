package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.clong8;

import java.nio.ByteOrder;

/**
 * this type in C language is int64_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cint64_t extends clong8 {

    public cint64_t(Long value, ByteOrder byteOrder) {
        super(value, byteOrder);
    }

    public cint64_t(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }
}