package org.fz.nettyx.serializer.struct.basic.cpp.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.Charset;
import org.fz.nettyx.serializer.struct.basic.cpp.CppBasic;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2023/12/27 13:31
 */
public class Cpp32tchar extends CppBasic<Integer> {

    private static final Charset UTF_32 = Charset.forName("UTF-32");

    public Cpp32tchar(Integer value) {
        super(value, 4);
    }

    public Cpp32tchar(ByteBuf buf) {
        super(buf, 4);
    }

    @Override
    protected ByteBuf toByteBuf(Integer value, int size) {
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
