package org.fz.nettyx.coveragebasic;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.basic.Basic;

import java.nio.ByteOrder;

/**
 * Throws an unexpected index exception during construction to exercise error propagation.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026-07-29
 */
public class UnexpectedIndexBasic extends Basic<Integer> {

    public UnexpectedIndexBasic(ByteBuf input, ByteOrder order) {
        super(input, order);
    }

    @Override public int size() { return 0; }
    @Override public boolean hasSigned() { return true; }
    @Override public void write(ByteBuf writingBuf, ByteOrder byteOrder) { }

    @Override
    protected Integer read(ByteBuf readingBuf, ByteOrder byteOrder) {
        throw new IndexOutOfBoundsException("unexpected");
    }
}
