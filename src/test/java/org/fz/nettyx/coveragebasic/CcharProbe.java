package org.fz.nettyx.coveragebasic;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.basic.c.signed.cchar;

import java.nio.ByteOrder;

/**
 * Exposes the protected {@code cchar} reader for direct buffer tests.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
public class CcharProbe extends cchar {

    public CcharProbe() {
        super(0);
    }

    public Byte readProbe(ByteBuf input, ByteOrder order) {
        return read(input, order);
    }
}
