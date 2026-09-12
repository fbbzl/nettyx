package org.fz.nettyx.serializer.annotated.basic.cpp.unsigned;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.annotated.basic.cpp.cppbasic;

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
        super(requireValidValue(value));
    }

    private static Long requireValidValue(Long value) {
        if (value != null && (value < 0 || value > 0xFFFF_FFFFL))
            throw new IllegalArgumentException("cppchar32_t value out of range [0, 2^32-1]: " + value);
        return value;
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
        Long currentValue = requireValidValue(value);
        if (currentValue == null)
            throw new IllegalArgumentException("cppchar32_t value can not be null");
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeIntLE(currentValue.intValue());
        else
            writingBuf.writeInt(currentValue.intValue());
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
