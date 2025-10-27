package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuint;

import java.nio.ByteOrder;

/**
 * this type in Cpp language is unsigned int
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 15:50
 */
public class cppuint extends cuint {

    public cppuint(Long value, ByteOrder byteOrder) {
        super(value, byteOrder);
    }

    public cppuint(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }
}
