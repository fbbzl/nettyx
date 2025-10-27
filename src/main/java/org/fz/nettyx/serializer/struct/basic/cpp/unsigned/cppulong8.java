package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.culong8;

import java.math.BigInteger;
import java.nio.ByteOrder;

/**
 * this type in Cpp language is unsigned long8
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class cppulong8 extends culong8 {

    public cppulong8(BigInteger value, ByteOrder byteOrder) {
        super(value, byteOrder);
    }

    public cppulong8(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }
}
