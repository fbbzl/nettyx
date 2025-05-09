package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cushort;

/**
 * this type in Cpp language is unsigned short
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class cppushort extends cushort {

    public static final cppushort
            MIN_VALUE = new cppushort(0),
            MAX_VALUE = new cppushort(Short.MAX_VALUE * 2 + 1);

    public cppushort(Integer value) {
        super(value);
    }

    public cppushort(ByteBuf buf) {
        super(buf);
    }
}
