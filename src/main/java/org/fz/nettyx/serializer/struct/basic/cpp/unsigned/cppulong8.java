package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.culong8;

import java.math.BigInteger;

/**
 * this type in Cpp language is unsigned long8
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class cppulong8 extends culong8 {

    public static final cppulong8
            MIN_VALUE = new cppulong8(BigInteger.ZERO),
            MAX_VALUE = new cppulong8(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Long.MAX_VALUE)).add(BigInteger.ONE));

    public cppulong8(BigInteger value) {
        super(value);
    }

    public cppulong8(ByteBuf buf) {
        super(buf);
    }

    public static cppulong8 of(BigInteger value) {
        return new cppulong8(value);
    }
}
