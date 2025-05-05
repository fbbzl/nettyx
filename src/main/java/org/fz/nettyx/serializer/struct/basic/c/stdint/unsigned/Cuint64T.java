package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Culong8;

import java.math.BigInteger;

/**
 * The type Cuint64.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class Cuint64T extends Culong8 {

    public static final Cuint64T
            MIN_VALUE = new Cuint64T(BigInteger.ZERO),
            MAX_VALUE = new Cuint64T(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Long.MAX_VALUE)).add(BigInteger.ONE));

    public Cuint64T(BigInteger value) {
        super(value);
    }

    public Cuint64T(ByteBuf buf) {
        super(buf);
    }
}