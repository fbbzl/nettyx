package org.fz.nettyx.coveragebasic;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.unsigned.culong8;

import java.math.BigInteger;
import java.nio.ByteOrder;

public class Culong8Probe extends culong8 {

    public Culong8Probe() {
        super(BigInteger.ZERO);
    }

    public BigInteger readProbe(ByteBuf input, ByteOrder order) {
        return read(input, order);
    }
}
