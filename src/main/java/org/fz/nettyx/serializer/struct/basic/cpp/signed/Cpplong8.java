package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.Clong8;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:16
 */
public class Cpplong8 extends Clong8 {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cpplong8 MIN_VALUE = new Cpplong8(Long.MIN_VALUE);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cpplong8 MAX_VALUE = new Cpplong8(Long.MAX_VALUE);

    public Cpplong8(Object value) {
        super(value);
    }

    public Cpplong8(ByteBuf buf) {
        super(buf);
    }

}
