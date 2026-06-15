package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.nio.ByteOrder;

/**
 * this type in C language is int
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class cint extends Cbasic<Integer> {

    public cint(Integer value) {
        super(value, 4);
    }

    public cint(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 4);
    }

    @Override
    public void write(ByteBuf writingBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeIntLE(value);
        else
            writingBuf.writeInt(value);
    }

    @Override
    protected Integer read(ByteBuf byteBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return byteBuf.readIntLE();
        else
            return byteBuf.readInt();
    }
}
