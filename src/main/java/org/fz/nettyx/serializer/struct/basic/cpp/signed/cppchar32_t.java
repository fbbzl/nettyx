package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.cpp.cppbasic;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * this type in Cpp language is char32_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:31
 */
public class cppchar32_t extends cppbasic<Integer> {

    private static final Charset UTF_32 = Charset.forName("UTF-32");

    public cppchar32_t(Integer value) {
        super(value);
    }

    public cppchar32_t(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public int size() { return 4; }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeIntLE(value);
        else
            writingBuf.writeInt(value);
    }

    @Override
    protected Integer read(ByteBuf readingBuf, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return readingBuf.readIntLE();
        else
            return readingBuf.readInt();
    }

    @Override
    public String toString() {
        return toString(UTF_32);
    }

    public String toString(Charset charset) {
        return value != null ? value.toString() : "";
    }

}
