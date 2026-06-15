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
public abstract class Cbasic<V extends Comparable<V>> extends Basic<V> {

    private static final boolean   C_DEFAULT_SIGNED = true;
    protected static final ByteOrder C_DEFAULT_ENDIAN = ByteOrder.LITTLE_ENDIAN;

    /**
     * Instantiates a new C basic.
     *
     * @param value the length
     * @param size  the size
     */
    protected Cbasic(V value, int size) {
        this(C_DEFAULT_ENDIAN, value, size);
    }

    protected Cbasic(ByteOrder byteOrder, V value, int size) {
        super(byteOrder, value, size);
    }

    protected Cbasic(ByteOrder byteOrder, ByteBuf buf, int size) {
        super(byteOrder, buf, size);
    }

    @Override
    public boolean hasSigned() {
        return C_DEFAULT_SIGNED;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
