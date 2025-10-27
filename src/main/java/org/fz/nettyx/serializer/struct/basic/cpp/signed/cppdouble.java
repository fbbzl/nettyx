package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.signed.cdouble;

import java.nio.ByteOrder;

/**
 * this type in Cpp language is double
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:13
 */
public class cppdouble extends cdouble {

    public cppdouble(Double value, ByteOrder byteOrder) {
        super(value, byteOrder);
    }

    public cppdouble(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

}
