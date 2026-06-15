package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.cpp.Cppbasic;

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
public class cppchar16_t extends Cppbasic<Character> {

    public cppchar16_t(Character value) {
        super(value, 2);
    }

    public cppchar16_t(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 2);
    }

    @Override
    public void write(ByteBuf writingBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            writingBuf.writeShortLE((short) value.charValue());
        else
            writingBuf.writeChar(value);
    }

    @Override
    protected Character read(ByteBuf byteBuf) {
        if (byteOrder == ByteOrder.LITTLE_ENDIAN)
            return (char) byteBuf.readShortLE();
        else
            return byteBuf.readChar();
    }

    @Override
    public String toString() {
        return toString(StandardCharsets.UTF_16);
    }

    public String toString(Charset charset) {
        return value != null ? value.toString() : "";
    }

}
