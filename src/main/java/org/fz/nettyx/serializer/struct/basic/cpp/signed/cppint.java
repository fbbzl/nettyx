package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.cint;

/**
 * this type in Cpp language is int
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:14
 */
public class cppint extends cint {

    public static final cppint
            MIN_VALUE = new cppint(Integer.MIN_VALUE),
            MAX_VALUE = new cppint(Integer.MAX_VALUE);

    public cppint(Integer value) {
        super(value);
    }

    public cppint(ByteBuf buf) {
        super(buf);
    }

    public static cppint of(Integer value) {
        return new cppint(value);
    }

}
