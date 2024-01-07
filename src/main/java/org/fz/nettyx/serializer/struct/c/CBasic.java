package org.fz.nettyx.serializer.struct.c;

import io.netty.buffer.ByteBuf;
import java.nio.ByteOrder;
import org.fz.nettyx.serializer.struct.Basic;


/**
 * The type C basic.
 *
 * @param <V> the type parameter
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 15:50
 */
public abstract class CBasic<V extends Comparable<V>> extends Basic<V> implements Comparable<CBasic<V>> {

    private static final boolean C_DEFAULT_SINGED = true;
    private static final ByteOrder C_DEFAULT_ENDIAN = ByteOrder.LITTLE_ENDIAN;

    /**
     * Instantiates a new C basic.
     *
     * @param value the value
     * @param size the size
     */
    protected CBasic(V value, int size) {
        super(value, size);
    }

    /**
     * Instantiates a new C basic.
     *
     * @param buf the buf
     * @param size the size
     */
    protected CBasic(ByteBuf buf, int size) {
        super(buf, size);
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
    public int hashCode() {
        return this.getValue().hashCode();
    }

    @Override
    public boolean equals(Object anotherObj) {
        if (anotherObj == null) return false;

        if (anotherObj instanceof CBasic<?>) {
            CBasic<?> anotherCBasic = (CBasic<?>) anotherObj;
            if (   this.getSize()   != anotherCBasic.getSize()
                || this.hasSinged() != anotherCBasic.hasSinged()
                || this.order()     != anotherCBasic.order()) {
                return false;
            }

            return this.getValue().equals(anotherCBasic.getValue());
        }
        return false;
    }

    @Override
    public String toString() {
        return this.getValue().toString();
    }

    @Override
    public int compareTo(CBasic<V> anotherCBasic) {
        return this.getValue().compareTo(anotherCBasic.getValue());
    }

}
