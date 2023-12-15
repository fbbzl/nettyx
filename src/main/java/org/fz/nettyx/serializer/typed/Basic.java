package org.fz.nettyx.serializer.typed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
    private final ByteBuf byteBuf;

    /**
     * the Java value
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
     * change value to byteBuf
     *
     * @param value value
     * @param size the size
     * @return byteBuf byte byteBuf
     */
    protected abstract ByteBuf toByteBuf(V value, int size);

    /**
     * change byteBuf to value
     *
     * @param byteBuf bytebuf of field
     * @return value v
     */
    protected abstract V toValue(ByteBuf byteBuf);

    /**
     * Get bytes byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getBytes() {
        return ByteBufUtil.getBytes(this.getByteBuf());
    }

    public ByteBuffer getNioBuffer() {
        return this.getByteBuf().nioBuffer();
    }

    /**
     * Instantiates a new Basic.
     *
     * @param value the value
     * @param size the size
     */
    protected Basic(V value, int size) {
        this.size = size;
        this.value = value;
        this.byteBuf = this.toByteBuf(this.value, this.size);
        this.fill(this.byteBuf, this.size);
    }

    /**
     * Instantiates a new Basic.
     *
     * @param byteBuf the byteBuf
     * @param size the size
     */
    protected Basic(ByteBuf byteBuf, int size) {
        this.size = size;
        this.fill(byteBuf, this.size);
        this.byteBuf = byteBuf.readBytes(this.size);
        this.value = this.toValue(this.byteBuf.duplicate());
    }

    /**
     * fill buffer into assigned length
     */
    private void fill(ByteBuf buf, int requiredSize) {
        int fillLength = requiredSize - buf.readableBytes();
        if (fillLength > 0) {
            buf.writeBytes(new byte[fillLength]);
        }
    }

    /**
     * Hex dump string.
     *
     * @return Returns a hex dump  of the specified buffer's readable bytes.
     */
    public String hexDump() {
        return ByteBufUtil.hexDump(this.getByteBuf());
    }
}
