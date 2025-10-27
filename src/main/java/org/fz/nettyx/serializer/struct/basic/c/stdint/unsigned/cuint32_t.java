package org.fz.nettyx.serializer.struct.basic.c.stdint.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuint;

import java.nio.ByteOrder;

/**
 * this type in C language is unit32_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/15 14:38
 */
public class cuint32_t extends cuint {

    public cuint32_t(Long value, ByteOrder byteOrder) {
        super(value, byteOrder);
    }

    public cuint32_t(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }
}