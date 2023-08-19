package org.fz.nettyx.serializer.typed;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;

import java.nio.ByteOrder;

/**
 * The type Basic. The specific implementation can be enhanced
 *
 * @param <V> the type parameter, may be the java type
 * @author fengbinbin
 * @version 1.0
 * @since 2021 /10/22 13:26
 */
@SuppressWarnings("unchecked")
public abstract class Basic<V> {

    private ByteBuf buf;

    /**
     * -- GETTER --
     * Gets value.
     */
    @Getter
    private V value;

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
     * size of this basic field
     *
     * @return size int
     */
    public abstract int size();

    /**
     * change value to buf
     *
     * @param value value
     * @return buf byte buf
     */
    protected abstract ByteBuf toByteBuf(V value);

    /**
     * change buf to value
     *
     * @param byteBuf bytebuf of field
     * @return value
     */
    protected abstract V toValue(ByteBuf byteBuf);

    /**
     * Gets buf.
     *
     * @return the buf
     */
    public ByteBuf getByteBuf() {
        return buf;
    }

    protected Basic(V value) {
        this.setValue(value);
    }

    protected Basic(ByteBuf buf) {
        this.setByteBuf(buf);
    }

    public <B extends Basic<?>> B setByteBuf(ByteBuf buf) {
        this.buf = Unpooled.copiedBuffer(buf);
        this.value = toValue(buf);
        return (B) this;
    }

    public <B extends Basic<?>> B setValue(V value) {
        this.value = value;
        this.buf = toByteBuf(value);
        return (B) this;
    }

}
