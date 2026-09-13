package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.basic.c.cbasic;

import java.nio.ByteOrder;

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
        super(readValue(buf));
    }

    private static Byte readValue(ByteBuf buf) {
        try {
            return buf.readByte();
        }
        catch (IndexOutOfBoundsException error) {
            throw new TooLessBytesException(1, buf.readableBytes());
        }
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
        return value != null ? value.toString() : "";
    }

}
