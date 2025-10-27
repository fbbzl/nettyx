package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;

/**
 * this type in Cpp language is unsigned char
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 14:07
 */
public class cppuchar extends cuchar {

    public cppuchar(Integer value, ByteOrder byteOrder) {
        super(value, byteOrder);
    }

    public cppuchar(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public String toString() {
        return new String(this.getBytes(), StandardCharsets.UTF_8);
    }

}
