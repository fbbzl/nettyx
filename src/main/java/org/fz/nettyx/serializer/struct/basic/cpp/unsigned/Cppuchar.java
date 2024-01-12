package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.Cuchar;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 14:07
 */
public class Cppuchar extends Cuchar {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cppuchar MIN_VALUE = new Cppuchar(0);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cppuchar MAX_VALUE = new Cppuchar(Byte.MAX_VALUE >> 2);

    public Cppuchar(Object value) {
        super(value);
    }

    public Cppuchar(ByteBuf buf) {
        super(buf);
    }

    @Override
    public String toString() {
        return new String(this.getBytes(), StandardCharsets.UTF_8);
    }

}
