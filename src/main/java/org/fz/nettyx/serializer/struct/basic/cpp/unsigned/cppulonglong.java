package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;

import java.math.BigInteger;

/**
 * this type in Cpp language is unsigned longlong
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class cppulonglong extends cppulong8 {

    public cppulonglong(BigInteger value) {
        super(value);
    }

    public cppulonglong(ByteBuf buf) {
        super(buf);
    }

}
