package org.fz.nettyx.serializer.struct.cpp.signed;

import io.netty.buffer.ByteBuf;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:16
 */
public class Cpplonglong extends Cpplong8 {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cpplonglong MIN_VALUE = new Cpplonglong(Long.MIN_VALUE);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cpplonglong MAX_VALUE = new Cpplonglong(Long.MAX_VALUE);

    public Cpplonglong(Long value) {
        super(value);
    }

    public Cpplonglong(ByteBuf buf) {
        super(buf);
    }

}
