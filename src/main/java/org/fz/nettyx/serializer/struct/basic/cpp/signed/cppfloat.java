package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.cfloat;

/**
 * this type in Cpp language is double
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:13
 */
public class cppfloat extends cfloat {

    public cppfloat(Float value) {
        super(value);
    }

    public cppfloat(ByteBuf buf) {
        super(buf);
    }

}
