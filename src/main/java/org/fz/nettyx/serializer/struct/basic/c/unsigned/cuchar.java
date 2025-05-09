package org.fz.nettyx.serializer.struct.basic.c.unsigned;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * C uchar mush use Java short
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 14:38
 */
public class cuchar extends CBasic<Short> {

    public static final cuchar
            MIN_VALUE = new cuchar(0),
            MAX_VALUE = new cuchar(Byte.MAX_VALUE * 2 + 1);

    /**
     * Instantiates a new Cuchar.
     *
     * @param value the length
     */
    public cuchar(Integer value) {
        super(value.shortValue(), 1);
    }

    /**
     * Instantiates a new Cuchar.
     *
     * @param buf the buf
     */
    public cuchar(ByteBuf buf) {
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
