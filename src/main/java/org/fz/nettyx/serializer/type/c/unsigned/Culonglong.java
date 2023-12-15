package org.fz.nettyx.serializer.type.c.unsigned;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/18 13:30
 */
public class Culonglong extends Culong8 {

    public Culonglong(BigInteger value) {
        super(value);
    }

    public Culonglong(ByteBuf buf) {
        super(buf);
    }
}
