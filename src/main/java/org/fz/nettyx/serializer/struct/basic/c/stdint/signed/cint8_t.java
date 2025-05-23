package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.cchar;

/**
 * this type in C language is int8_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cint8_t extends cchar {

    public static final cint8_t
            MIN_VALUE = new cint8_t(Integer.valueOf(Byte.MIN_VALUE)),
            MAX_VALUE = new cint8_t(Integer.valueOf(Byte.MAX_VALUE));

    public cint8_t(Integer value) {
        super(value);
    }

    public cint8_t(ByteBuf buf) {
        super(buf);
    }

    public static cint8_t of(Integer value) {
        return new cint8_t(value);
    }

}