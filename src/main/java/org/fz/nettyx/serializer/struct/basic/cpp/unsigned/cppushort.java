package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cushort;

import java.nio.ByteOrder;

/**
 * this type in Cpp language is unsigned short
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class cppushort extends cushort {

    public cppushort(ByteOrder byteOrder, Integer value) {
        super(byteOrder, value);
    }

    public cppushort(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf);
    }
}
