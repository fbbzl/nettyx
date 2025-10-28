package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;

import java.nio.ByteOrder;

/**
 * this type in C language is unsigned long4
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class culong4 extends cuint {

    public culong4(ByteOrder byteOrder, Long value) {
        super(byteOrder, value);
    }

    public culong4(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf);
    }
}
