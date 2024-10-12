package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Culong8;

import java.math.BigInteger;

/**
 * The type Culong 8.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class Cppulong8 extends Culong8 {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cppulong8 MIN_VALUE = new Cppulong8(BigInteger.ZERO);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cppulong8 MAX_VALUE = new Cppulong8(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.valueOf(Long.MAX_VALUE)).add(BigInteger.ONE));

    public Cppulong8(BigInteger value) {
        super(value);
    }

    public Cppulong8(ByteBuf buf) {
        super(buf);
    }
}
