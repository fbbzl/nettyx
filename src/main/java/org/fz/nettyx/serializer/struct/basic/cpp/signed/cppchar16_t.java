package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.cpp.cppbasic;

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
        super(value, 2);
    }

    public cppchar16_t(ByteBuf buf) {
        super(buf, 2);
    }

    @Override
    protected ByteBuf toByteBuf(Character value) {
        return Unpooled.buffer(getSize()).writeChar(value);
    }

    @Override
    protected Character toValue(ByteBuf byteBuf) {
        return byteBuf.readChar();
    }

    public static cppchar16_t of(Character value) {
        return new cppchar16_t(value);
    }

    @Override
    public String toString() {
        return new String(this.getBytes(), StandardCharsets.UTF_16);
    }

    public String toString(Charset charset) {
        return new String(this.getBytes(), charset);
    }

}
