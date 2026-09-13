package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is unsigned int
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 15:50
 */
public class cuint extends cbasic<Long> {

    public cuint(Long value) {
        super(value);
        if (value == null || value < 0) throw new IllegalArgumentException("cuint value must be non-negative");
    }

    public cuint(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public boolean hasSigned() {
        return false;
    }

    @Override
    public int size() { return 4; }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
        if (value == null || value < 0 || value > 0xFFFFFFFFL)
            throw new IllegalArgumentException("cuint value out of range [0, 2^32-1]: " + value);
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeIntLE(value.intValue());
        else
            writingBuf.writeInt(value.intValue());
    }

    @Override
    protected Long read(ByteBuf readingBuf, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return readingBuf.readUnsignedIntLE();
        else
            return readingBuf.readUnsignedInt();
    }
}
