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

    public cppint(Integer value) {
        super(value);
    }

    public cppint(ByteBuf buf) {
        super(buf);
    }

}
