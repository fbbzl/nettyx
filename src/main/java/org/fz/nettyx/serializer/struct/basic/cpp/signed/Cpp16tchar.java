package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.fz.nettyx.serializer.struct.basic.cpp.CppBasic;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:31
 */
public class Cpp16tchar extends CppBasic<Character> {

    public Cpp16tchar(Character value) {
        super(value, 2);
    }

    public Cpp16tchar(ByteBuf buf) {
        super(buf, 2);
    }

    @Override
    protected ByteBuf toByteBuf(Object value, int size) {
        return Unpooled.buffer(getSize()).writeChar((Character) value);
    }

    @Override
    protected Character toValue(ByteBuf byteBuf) {
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
