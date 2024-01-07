package org.fz.nettyx.serializer.struct.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.c.signed.Cfloat;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:13
 */
public class Cppfloat extends Cfloat {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cppfloat MIN_VALUE = new Cppfloat(Float.MIN_VALUE);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cppfloat MAX_VALUE = new Cppfloat(Float.MAX_VALUE);

    public Cppfloat(Float value) {
        super(value);
    }

    public Cppfloat(ByteBuf buf) {
        super(buf);
    }

}
