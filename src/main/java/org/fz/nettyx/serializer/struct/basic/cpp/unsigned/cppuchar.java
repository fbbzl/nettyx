package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.cuchar;

import java.nio.charset.StandardCharsets;

/**
 * this type in Cpp language is unsigned char
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 14:07
 */
public class cppuchar extends cuchar {

    public static final cppuchar
            MIN_VALUE = new cppuchar(0),
            MAX_VALUE = new cppuchar(Byte.MAX_VALUE * 2 + 1);

    public cppuchar(Integer value) {
        super(value);
    }

    public cppuchar(ByteBuf buf) {
        super(buf);
    }

    @Override
    public String toString() {
        return new String(this.getBytes(), StandardCharsets.UTF_8);
    }

    public static cppuchar of(Integer value) {
        return new cppuchar(value);
    }

}
