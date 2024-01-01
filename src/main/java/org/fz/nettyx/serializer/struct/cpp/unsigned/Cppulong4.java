package org.fz.nettyx.serializer.struct.cpp.unsigned;

import io.netty.buffer.ByteBuf;

/**
 * The type Culong 4.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class Cppulong4 extends Cppuint {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cppulong4 MIN_VALUE = new Cppulong4(0L);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cppulong4 MAX_VALUE = new Cppulong4(Long.valueOf(Integer.MAX_VALUE >> 2));

    public Cppulong4(Long value) {
        super(value);
    }

    public Cppulong4(ByteBuf buf) {
        super(buf);
    }
}
