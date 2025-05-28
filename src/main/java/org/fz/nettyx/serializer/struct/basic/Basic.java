package org.fz.nettyx.serializer.struct.basic;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.fz.erwin.exception.Throws;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Basic. The specific implementation can be enhanced
 *
 * @param <V> the type parameter, may be the java type
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:26
 */
@Getter
@FieldDefaults(level = PRIVATE)
public abstract class Basic<V extends Comparable<V>> implements Comparable<Basic<V>> {

    final int size;
    byte[] bytes;
    V      value;

    public V getValue() {
        // do value lazy set
        if (this.bytes != null && this.value == null) {
            this.value = this.toValue(Unpooled.wrappedBuffer(this.bytes));
        }
        return value;
    }

    protected Basic(ByteBuf byteBuf, int size) {
        this.size = size;
        Throws.ifTrue(byteBuf.readableBytes() < size, () -> new TooLessBytesException(size, byteBuf.readableBytes()));

        this.bytes = new byte[this.size];
        byteBuf.readBytes(this.bytes);
    }

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
     * To byte buf.
     *
     * @param value the value
     * @param size  the size
     * @return the byte buf
     */
    protected abstract ByteBuf toByteBuf(V value, int size);

    /**
     * To value v.
     *
     * @param byteBuf the byte buf
     * @return the v
     */
    protected abstract V toValue(ByteBuf byteBuf);

    /**
     * Gets byte buf.
     *
     * @return the byte buf
     */
    public ByteBuf getByteBuf() {
        return Unpooled.wrappedBuffer(this.getBytes());
    }

    /**
     * Gets nio buffer.
     *
     * @return the nio buffer
     */
    public ByteBuffer getNioBuffer() {
        return ByteBuffer.wrap(this.getBytes());
    }

    protected Basic(V value, int size) {
        this.size  = size;
        this.value = value;
    }

    public byte[] getBytes() {
        if (this.value != null && this.bytes == null) {
            this.bytes = new byte[this.size];
            ByteBuf buf = this.toByteBuf(this.value, this.size);
            this.fill(buf, this.size);
            buf.readBytes(this.bytes);
            ReferenceCountUtil.release(buf);
        }
        return bytes;
    }

    private void fill(ByteBuf buf, int requiredSize) {
        int fillLength = requiredSize - buf.readableBytes();
        if (fillLength > 0) {
            buf.writeBytes(new byte[fillLength]);
        }
    }

    /**
     * Hex dump string.
     *
     * @return the string
     */
    public String hexDump() {
        return ByteBufUtil.hexDump(this.getBytes());
    }

    @Override
    public int hashCode() {
        return this.getValue().hashCode();
    }

    @Override
    public boolean equals(Object anotherObj) {
        if (anotherObj == null) return false;

        if (anotherObj instanceof CBasic<?>) {
            if (this.getSize()   != ((CBasic<?>) anotherObj).getSize()
                ||
                this.hasSinged() != ((CBasic<?>) anotherObj).hasSinged()
                ||
                this.order()     != ((CBasic<?>) anotherObj).order()) {
                return false;
            }

            return this.getValue().equals(((CBasic<?>) anotherObj).getValue());
        }
        return false;
    }

    @Override
    public int compareTo(Basic<V> anotherCBasic) {
        return this.getValue().compareTo(anotherCBasic.getValue());
    }
}
