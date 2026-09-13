package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is unsigned short
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class cushort extends cbasic<Integer> {

    public cushort(Integer value) {
        super(value);
        if (value == null || value < 0) throw new IllegalArgumentException("cushort value must be non-negative");
    }

    public cushort(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public boolean hasSigned() {
        return false;
    }

    @Override
    public int size() { return 2; }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
        if (value == null || value < 0 || value > 0xFFFF)
            throw new IllegalArgumentException("cushort value out of range [0, 65535]: " + value);
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeShortLE(value);
        else
            writingBuf.writeShort(value);
    }

    @Override
    protected Integer read(ByteBuf readingBuf, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return readingBuf.readUnsignedShortLE();
        else
            return readingBuf.readUnsignedShort();
    }

}
