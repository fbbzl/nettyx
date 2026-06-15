package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.cpp.Cppbasic;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * this type in Cpp language is char32_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:31
 */
public class cppchar32_t extends Cppbasic<Integer> {

    private static final Charset UTF_32 = Charset.forName("UTF-32");

    public cppchar32_t(Integer value) {
        super(value, 4);
    }

    public cppchar32_t(ByteOrder byteOrder, ByteBuf buf) {
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

    @Override
    public String toString() {
        return toString(UTF_32);
    }

    public String toString(Charset charset) {
        return value != null ? value.toString() : "";
    }

}
