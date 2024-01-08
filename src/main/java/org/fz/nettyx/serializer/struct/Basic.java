package org.fz.nettyx.serializer.struct;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import lombok.Getter;

/**
 * The type Basic. The specific implementation can be enhanced
 *
 * @param <V> the type parameter, may be the java type
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:26
 */
@Getter
public abstract class Basic<V> {

    /**
     * byte size
     */
    private final int size;

    /**
     * the byte byteBuf
     */
    private final byte[] bytes;

    /**
     * the Java length
     */
    private final V value;

    /**
     * Has singed boolean.
     *
     * @return the boolean
     */
    public abstract boolean hasSinged();

    /**
     * Order byte order.
     *
     * @return the byte order
     */
    public abstract ByteOrder order();

    /**
     * change length to byteBuf
     *
     * @param value length
     * @param size the size
     * @return byteBuf byte byteBuf
     */
    protected abstract ByteBuf toByteBuf(V value, int size);

    /**
     * change byteBuf to length
     *
     * @param byteBuf bytebuf of field
     * @return length v
     */
    protected abstract V toValue(ByteBuf byteBuf);

    public ByteBuf getByteBuf() {
        return Unpooled.wrappedBuffer(this.getBytes());
    }

    public ByteBuffer getNioBuffer() {
        return ByteBuffer.wrap(this.getBytes());
    }

    /**
     * Instantiates a new Basic.
     *
     * @param value the length
     * @param size the size
     */
    protected Basic(V value, int size) {
        this.size = size;
        this.value = value;
        this.bytes = new byte[this.size];
        this.toByteBuf(this.value, this.size).readBytes(this.bytes);
    }

    /**
     * Instantiates a new Basic.
     *
     * @param byteBuf the byteBuf
     * @param size the size
     */
    protected Basic(ByteBuf byteBuf, int size) {
        this.size = size;
        this.bytes = new byte[this.size];
        byteBuf.readBytes(this.bytes);
        this.value = this.toValue(Unpooled.wrappedBuffer(this.bytes));
    }

    /**
     * Hex dump string.
     *
     * @return Returns a hex dump  of the specified buffer's readable bytes.
     */
    public String hexDump() {
        return ByteBufUtil.hexDump(this.getBytes());
    }
}
