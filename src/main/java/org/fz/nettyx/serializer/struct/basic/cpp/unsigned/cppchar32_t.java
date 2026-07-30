package org.fz.nettyx.serializer.struct.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.cpp.cppbasic;

import java.nio.ByteOrder;

/**
 * this type in Cpp language is char32_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:31
 */
public class cppchar32_t extends cppbasic<Long> {

    public cppchar32_t(Long value) {
        super(value);
    }

    public cppchar32_t(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public int size() { return 4; }

    @Override
    public boolean hasSigned() {
        return false;
    }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
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

    @Override
    public String toString() {
        return value != null ? value.toString() : "";
    }

}
