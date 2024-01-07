package org.fz.nettyx.serializer.struct.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.c.signed.Cchar;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:12
 */
public class Cpp8tchar extends Cchar {

    public Cpp8tchar(Integer value) {
        super(value);
    }

    public Cpp8tchar(ByteBuf buf) {
        super(buf);
    }

}
