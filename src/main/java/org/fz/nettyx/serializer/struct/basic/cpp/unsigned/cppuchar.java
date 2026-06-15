package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;

import java.nio.ByteOrder;

/**
 * this type in Cpp language is unsigned char
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 14:07
 */
public class cppuchar extends cuchar {

    public cppuchar(Integer value) {
        super(value);
    }

    public cppuchar(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf);
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "";
    }

}
