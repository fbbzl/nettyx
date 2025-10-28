package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.culong8;

import java.math.BigInteger;
import java.nio.ByteOrder;

/**
 * this type in C language is unit64_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cuint64_t extends culong8 {

    public cuint64_t(ByteOrder byteOrder, BigInteger value) {
        super(byteOrder, value);
    }

    public cuint64_t(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf);
    }
}