package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.cshort;

import java.nio.ByteOrder;

/**
 * this type in C language is int16_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cint16_t extends cshort {

    public cint16_t(Integer value, ByteOrder byteOrder) {
        super(value, byteOrder);
    }

    public cint16_t(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

}