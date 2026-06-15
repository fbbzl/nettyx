package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.c.Cbasic;

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
public class cchar extends Cbasic<Byte> {

    public cchar(Integer value) {
        super(value.byteValue(), 1);
    }

    public cchar(ByteOrder byteOrder, ByteBuf buf) {
        super(byteOrder, buf, 1);
    }

    @Override
    public void write(ByteBuf writingBuf) {
        writingBuf.writeByte(value);
    }

    @Override
    protected Byte read(ByteBuf byteBuf) {
        return byteBuf.readByte();
    }

    @Override
    public String toString() {
        return toString(StandardCharsets.US_ASCII);
    }

    public String toString(Charset charset) {
        return value != null ? value.toString() : "";
    }

}
