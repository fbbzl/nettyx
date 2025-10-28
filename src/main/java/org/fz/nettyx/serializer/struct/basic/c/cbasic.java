package org.fz.nettyx.serializer.struct.basic.c;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.struct.basic.Basic;

import java.nio.ByteOrder;


/**
 * The type C basic.
 *
 * @param <V> the type parameter
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 15:50
 */
public abstract class cbasic<V extends Comparable<V>> extends Basic<V> {

    private static final boolean   C_DEFAULT_SINGED = true;
    private static final ByteOrder C_DEFAULT_ENDIAN = ByteOrder.LITTLE_ENDIAN;

    /**
     * Instantiates a new C basic.
     *
     * @param value the length
     * @param size  the size
     */
    protected cbasic(ByteOrder byteOrder, V value, int size) {
        super(byteOrder, value, size);
    }

    /**
     * Instantiates a new C basic.
     *
     * @param buf  the buf
     * @param size the size
     */
    protected cbasic(ByteOrder byteOrder, ByteBuf buf, int size) {
        super(byteOrder, buf, size);
    }

    @Override
    public boolean hasSinged() {
        return C_DEFAULT_SINGED;
    }

    @Override
    public ByteOrder order() {
        return C_DEFAULT_ENDIAN;
    }

    @Override
    public String toString() {
        return this.getValue().toString();
    }

}
