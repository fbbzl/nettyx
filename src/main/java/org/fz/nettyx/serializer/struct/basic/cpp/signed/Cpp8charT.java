package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.Cchar;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:12
 */
public class Cpp8charT extends Cchar {

    public Cpp8charT(Integer value) {
        super(value);
    }

    public Cpp8charT(ByteBuf buf) {
        super(buf);
    }

}
