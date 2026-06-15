package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is unsigned short
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:39
 */
public class cushort extends Cbasic<Integer> {

    public cushort(Integer value) {
        super(value, 2);
    }

    public cushort(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 2);
    }

    @Override
    public boolean hasSigned() {
        return false;
    }

    @Override
    public void write(ByteBuf writingBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeShortLE(value);
        else
            writingBuf.writeShort(value);
    }

    @Override
    protected Integer read(ByteBuf byteBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return byteBuf.readUnsignedShortLE();
        else
            return byteBuf.readUnsignedShort();
    }

}
