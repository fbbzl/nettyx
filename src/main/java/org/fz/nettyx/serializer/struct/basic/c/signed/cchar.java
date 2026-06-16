package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
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

    public cchar(Integer value) {
        super(value.byteValue());
    }

    public cchar(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
    }

    @Override
    public int size() { return 1; }

    public void write(ByteBuf writingBuf, ByteOrder byteOrder) {
        writingBuf.writeByte(value);
    }

    @Override
    protected Byte read(ByteBuf readingBuf, ByteOrder byteOrder) {
        return readingBuf.readByte();
    }

    @Override
    public String toString() {
        return toString(StandardCharsets.US_ASCII);
    }

    public String toString(Charset charset) {
        return value != null ? value.toString() : "";
    }

}
