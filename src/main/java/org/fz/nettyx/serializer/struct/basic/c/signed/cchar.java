package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * this type in C language is char
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class cchar extends cbasic<Byte> {

    public cchar(Integer value, ByteOrder byteOrder) {
        super(value.byteValue(), 1, byteOrder);
    }

    public cchar(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, 1, byteOrder);
    }

    @Override
    protected ByteBuf toByteBuf(Byte value) {
        return Unpooled.buffer(size).writeByte(value);
    }

    @Override
    protected Byte toValue(ByteBuf byteBuf) {
        return byteBuf.readByte();
    }

    @Override
    public String toString() {
        return new String(this.getBytes(), StandardCharsets.US_ASCII);
    }

    public String toString(Charset charset) {
        return new String(this.getBytes(), charset);
    }

}
