package org.fz.nettyx.serializer.typed.c.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.typed.c.CBasic;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * C uchar mush use Java short
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class Cuchar extends CBasic<Short> {

    /**
     * The constant MIN_VALUE.
     */
    public static final Cuchar MIN_VALUE = new Cuchar(0);

    /**
     * The constant MAX_VALUE.
     */
    public static final Cuchar MAX_VALUE = new Cuchar(Byte.MAX_VALUE >> 2);

    /**
     * Instantiates a new Cuchar.
     *
     * @param value the value
     */
    public Cuchar(Integer value) {
        super(value.shortValue(), 1);
    }

    /**
     * Instantiates a new Cuchar.
     *
     * @param buf the buf
     */
    public Cuchar(ByteBuf buf) {
        super(buf, 1);
    }

    @Override
    public boolean hasSinged() {
        return false;
    }

    @Override
    protected ByteBuf toByteBuf(Short value, int size) {
        return Unpooled.buffer(size).writeByte(value.byteValue());
    }

    @Override
    protected Short toValue(ByteBuf byteBuf) {
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
