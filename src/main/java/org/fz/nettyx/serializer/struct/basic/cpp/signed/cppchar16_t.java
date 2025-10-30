package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

    public cppchar16_t(ByteBuf buf) {
        super(buf, 2);
    }

    @Override
    protected ByteBuf toByteBuf(Character value, ByteOrder byteOrder) {
        return Unpooled.buffer(getSize()).writeChar(value);
    }

    @Override
    protected Character toValue(ByteBuf byteBuf, ByteOrder byteOrder) {
        return byteBuf.readChar();
    }

    @Override
    public String toString() {
        return new String(this.getBytes(), StandardCharsets.UTF_16);
    }

    public String toString(Charset charset) {
        return new String(this.getBytes(), charset);
    }

}
