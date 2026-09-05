package org.fz.nettyx.coveragebasic;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.type.basic.c.signed.cchar;

import java.nio.ByteOrder;

public class CcharProbe extends cchar {

    public CcharProbe() {
        super(0);
    }

    public Byte readProbe(ByteBuf input, ByteOrder order) {
        return read(input, order);
    }
}
