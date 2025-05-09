package org.fz.nettyx.serializer.struct.basic.c.stdint.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.cshort;

/**
 * this type in C language is int16_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cint16_t extends cshort {

    public static final cint16_t
            MIN_VALUE = new cint16_t(Integer.valueOf(Short.MIN_VALUE)),
            MAX_VALUE = new cint16_t(Integer.valueOf(Short.MAX_VALUE));

    public cint16_t(Integer value) {
        super(value);
    }

    public cint16_t(ByteBuf buf) {
        super(buf);
    }

}