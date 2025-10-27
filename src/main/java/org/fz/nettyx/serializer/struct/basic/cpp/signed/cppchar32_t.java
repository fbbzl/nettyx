package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
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

    public cppchar32_t(Integer value, ByteOrder byteOrder) {
        super(value, 4, byteOrder);
    }

    public cppchar32_t(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, 4, byteOrder);
    }

    @Override
    protected ByteBuf toByteBuf(Integer value) {
        return Unpooled.buffer(getSize()).writeIntLE(value);
    }

    @Override
    protected Integer toValue(ByteBuf byteBuf) {
        return byteBuf.readIntLE();
    }

    @Override
    public String toString() {
        return new String(this.getBytes(), UTF_32);
    }

    public String toString(Charset charset) {
        return new String(this.getBytes(), charset);
    }

}
