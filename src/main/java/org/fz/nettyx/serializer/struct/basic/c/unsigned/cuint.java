package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is unsigned int
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 15:50
 */
public class cuint extends Cbasic<Long> {

    public cuint(Long value) {
        super(value, 4);
    }

    public cuint(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 4);
    }

    @Override
    public boolean hasSigned() {
        return false;
    }

    @Override
    public void write(ByteBuf writingBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeIntLE(value.intValue());
        else
            writingBuf.writeInt(value.intValue());
    }

    @Override
    protected Long read(ByteBuf byteBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return byteBuf.readUnsignedIntLE();
        else
            return byteBuf.readUnsignedInt();
    }
}
