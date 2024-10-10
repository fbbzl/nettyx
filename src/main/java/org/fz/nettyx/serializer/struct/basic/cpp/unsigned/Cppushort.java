package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cushort;

/**
 * The type Cushort.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class Cppushort extends Cushort {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cppushort MIN_VALUE = new Cppushort(0);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cppushort MAX_VALUE = new Cppushort(Short.MAX_VALUE * 2 + 1);

    public Cppushort(Integer value) {
        super(value);
    }

    public Cppushort(ByteBuf buf) {
        super(buf);
    }
}
