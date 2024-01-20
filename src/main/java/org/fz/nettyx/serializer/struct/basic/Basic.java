package org.fz.nettyx.serializer.struct.basic;

import static org.fz.nettyx.serializer.struct.StructUtils.filterConstructor;

import cn.hutool.core.util.ArrayUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.util.ReferenceCountUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import lombok.Getter;
import org.fz.nettyx.exception.TooLessBytesException;
import org.fz.nettyx.util.Throws;

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

    private final int size;

    private final byte[] bytes;

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
     * To byte buf byte buf.
     *
     * @param value the value
     * @param size the size
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

    /**
     * Instantiates a new Basic.
     *
     * @param value the value
     * @param size the size
     */
    protected Basic(V value, int size) {
        this.size = size;
        this.value = value;

        this.bytes = new byte[this.size];

        if (this.value != null) {
            ByteBuf buf = this.toByteBuf(this.value, this.size);
            this.fill(buf, this.size);
            buf.readBytes(this.bytes);
            ReferenceCountUtil.release(buf);
        }
    }

    /**
     * Instantiates a new Basic.
     *
     * @param byteBuf the byte buf
     * @param size the size
     */
    protected Basic(ByteBuf byteBuf, int size) {
        this.size = size;
        Throws.ifLess(byteBuf.readableBytes(), size, new TooLessBytesException(size, byteBuf.readableBytes()));

        this.bytes = new byte[this.size];
        byteBuf.readBytes(this.bytes);
        this.value = this.toValue(Unpooled.wrappedBuffer(this.bytes));
    }

    private void fill(ByteBuf buf, int requiredSize) {
        int fillLength = requiredSize - buf.readableBytes();
        if (fillLength > 0) {
            buf.writeBytes(new byte[fillLength]);
        }
    }

    public static <B extends Basic<?>> int reflectForSize(Class<B> basicClass)
        throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor<? extends Basic<?>> basicConstructor = filterConstructor(basicClass,
            c -> ArrayUtil.equals(c.getParameterTypes(), new Class[]{ByteBuf.class}));

        if (basicConstructor == null) {
            throw new IllegalArgumentException(
                "can not find basic type [" + basicClass + "] constructor with bytebuf arg, please check");
        }

        ByteBuf fillingBuf = Unpooled.wrappedBuffer(new byte[128]);
        try {
            return basicConstructor.newInstance(fillingBuf).getSize();
        } finally {
            fillingBuf.skipBytes(fillingBuf.readableBytes());
            fillingBuf.release();
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
}
