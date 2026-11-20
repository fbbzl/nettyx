package org.fz.nettyx.coveragebasic;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.basic.c.unsigned.culong8;

import java.math.BigInteger;
import java.nio.ByteOrder;

/**
 * Exposes the protected unsigned 64-bit reader for direct buffer tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
public class Culong8Probe extends culong8 {

    public Culong8Probe() {
        super(BigInteger.ZERO);
    }

    public BigInteger readProbe(ByteBuf input, ByteOrder order) {
        return read(input, order);
    }
}
