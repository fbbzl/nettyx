package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * this type in C language is unsigned char
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class cuchar extends Cbasic<Short> {

    public cuchar(Integer value) {
        super(value.shortValue(), 1);
    }

    public cuchar(ByteBuf buf) {
        super(buf, 1);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(Short value, ByteOrder byteOrder) {
        return Unpooled.buffer(size).writeByte(value);
    }

    @Override
    protected Short toValue(ByteBuf byteBuf, ByteOrder byteOrder) {
        return byteBuf.readUnsignedByte();
    }

    @Override
    public String toString() {
        return new String(this.getBytes(), StandardCharsets.US_ASCII);
    }

    public String toString(Charset charset) {
        return new String(this.getBytes(), charset);
    }

}
