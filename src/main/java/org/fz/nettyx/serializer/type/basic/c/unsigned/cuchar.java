package org.fz.nettyx.serializer.type.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.type.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is unsigned char
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class cuchar extends cbasic<Short> {

    public cuchar(Integer value) {
        super(requireValidValue(value));
    }

    private static short requireValidValue(Integer value) {
        if (value == null || value < 0 || value > 0xFF)
            throw new IllegalArgumentException("cuchar value out of range [0, 255]: " + value);
        return value.shortValue();
    }

    public cuchar(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public boolean hasSigned() {
        return false;
    }

    @Override
    public int size() { return 1; }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
        if (value == null || value < 0 || value > 0xFF)
            throw new IllegalArgumentException("cuchar value out of range [0, 255]: " + value);
        writingBuf.writeByte(value);
    }

    @Override
    protected Short read(ByteBuf readingBuf, ByteOrder byteOrder) {
        return readingBuf.readUnsignedByte();
    }

    @Override
    public String toString() {
        return value != null ? value.toString() : "";
    }

}
