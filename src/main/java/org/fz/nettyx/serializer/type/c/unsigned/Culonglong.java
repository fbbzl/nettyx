package org.fz.nettyx.serializer.type.c.unsigned;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;

/**
 * The type Culonglong.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/18 13:30
 */
public class Culonglong extends Culong8 {

    /**
     * Instantiates a new Culonglong.
     *
     * @param value the value
     */
    public Culonglong(BigInteger value) {
        super(value);
    }

    /**
     * Instantiates a new Culonglong.
     *
     * @param buf the buf
     */
    public Culonglong(ByteBuf buf) {
        super(buf);
    }
}
