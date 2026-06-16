package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.cpp.cppbasic;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * this type in Cpp language is char16_t
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:31
 */
public class cppchar16_t extends cppbasic<Character> {

    public cppchar16_t(Character value) {
        super(value);
    }

    public cppchar16_t(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public int size() { return 2; }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeShortLE((short) value.charValue());
        else
            writingBuf.writeChar(value);
    }

    @Override
    protected Character read(ByteBuf readingBuf, ByteOrder byteOrder) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return (char) readingBuf.readShortLE();
        else
            return readingBuf.readChar();
    }

    @Override
    public String toString() {
        return toString(StandardCharsets.UTF_16);
    }

    public String toString(Charset charset) {
        return value != null ? value.toString() : "";
    }

}
