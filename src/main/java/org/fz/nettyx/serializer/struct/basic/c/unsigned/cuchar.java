package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
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

    public cuchar(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 1);
    }

    @Override
    public boolean hasSigned() {
        return false;
    }

    @Override
    public void write(ByteBuf writingBuf) {
        writingBuf.writeByte(value);
    }

    @Override
    protected Short read(ByteBuf byteBuf) {
        return byteBuf.readUnsignedByte();
    }

    @Override
    public String toString() {
        return toString(StandardCharsets.US_ASCII);
    }

    public String toString(Charset charset) {
        return value != null ? value.toString() : "";
    }

}
