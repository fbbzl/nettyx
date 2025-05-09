package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.culong8;

import java.math.BigInteger;

/**
 * this type in C language is unit64_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cuint64_t extends culong8 {

    public static final cuint64_t
            MIN_VALUE = new cuint64_t(BigInteger.ZERO),
            MAX_VALUE = new cuint64_t(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Long.MAX_VALUE)).add(BigInteger.ONE));

    public cuint64_t(BigInteger value) {
        super(value);
    }

    public cuint64_t(ByteBuf buf) {
        super(buf);
    }
}