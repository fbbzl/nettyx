package org.fz.nettyx.serializer.struct.cpp.signed;

import io.netty.buffer.ByteBuf;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:15
 */
public class Cpplong4 extends Cppint {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cpplong4 MIN_VALUE = new Cpplong4(Integer.MIN_VALUE);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cpplong4 MAX_VALUE = new Cpplong4(Integer.MAX_VALUE);

    public Cpplong4(Integer value) {
        super(value);
    }

    public Cpplong4(ByteBuf buf) {
        super(buf);
    }

}
