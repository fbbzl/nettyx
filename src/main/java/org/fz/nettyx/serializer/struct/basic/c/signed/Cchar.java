package org.fz.nettyx.serializer.struct.basic.c.signed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * The type Cchar.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class Cchar extends CBasic<Byte> {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cchar MIN_VALUE = new Cchar(Integer.valueOf(Byte.MIN_VALUE));

    /**
     * The constant MAX_VALUE.
     */
    public static final Cchar MAX_VALUE = new Cchar(Integer.valueOf(Byte.MAX_VALUE));

    /**
     * Instantiates a new Cchar.
     *
     * @param value the length
     */
    public Cchar(Integer value) {
        super(value.byteValue(), 1);
    }

    /**
     * Instantiates a new Cchar.
     *
     * @param buf the buf
     */
    public Cchar(ByteBuf buf) {
        super(buf, 1);
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

    public String toString(Charset charset) {
        return new String(this.getBytes(), charset);
    }

}
