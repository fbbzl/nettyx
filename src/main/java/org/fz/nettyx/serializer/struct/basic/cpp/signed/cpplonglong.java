package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;

/**
 * this type in Cpp language is longlong
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:16
 */
public class cpplonglong extends cpplong8 {

    public static final cpplonglong
            MIN_VALUE = new cpplonglong(Long.MIN_VALUE),
            MAX_VALUE = new cpplonglong(Long.MAX_VALUE);

    public cpplonglong(Long value) {
        super(value);
    }

    public cpplonglong(ByteBuf buf) {
        super(buf);
    }

    public static cpplonglong of(Long value) {
        return new cpplonglong(value);
    }

}
