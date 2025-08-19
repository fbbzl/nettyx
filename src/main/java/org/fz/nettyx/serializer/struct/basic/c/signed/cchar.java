package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

import java.nio.charset.StandardCharsets;

/**
 * this type in C language is char
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class cchar extends CBasic<Byte> {

    public static final cchar
            MIN_VALUE = new cchar(Integer.valueOf(Byte.MIN_VALUE)),
            MAX_VALUE = new cchar(Integer.valueOf(Byte.MAX_VALUE));

    public cchar(Integer value) {
        super(value.byteValue(), 1);
    }

    public cchar(ByteBuf buf) {
        super(buf, 1);
    }

    public static cchar of(Integer value) {
        return new cchar(value);
    }

    @Override
    protected ByteBuf toByteBuf(Byte value, int size) {
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

}
