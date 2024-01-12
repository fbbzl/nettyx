package org.fz.nettyx.serializer.struct.basic.cpp;

import io.netty.buffer.ByteBuf;
import java.nio.ByteOrder;
import org.fz.nettyx.serializer.struct.basic.Basic;
import org.fz.nettyx.serializer.struct.basic.c.CBasic;

/**
 * The type Cpp basic.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 16:04
 */
public abstract class CppBasic<V extends Comparable<V>> extends Basic<V> implements Comparable<CppBasic<V>> {

    private static final boolean CPP_DEFAULT_SINGED = true;
    private static final ByteOrder CPP_DEFAULT_ENDIAN = ByteOrder.LITTLE_ENDIAN;


    protected CppBasic(Object value, int size) {
        super(value, size);
    }

    protected CppBasic(ByteBuf buf, int size) {
        super(buf, size);
    }


    @Override
    public boolean hasSinged() {
        return CPP_DEFAULT_SINGED;
    }

    @Override
    public ByteOrder order() {
        return CPP_DEFAULT_ENDIAN;
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
    public int compareTo(CppBasic<V> anotherCBasic) {
        return this.getValue().compareTo(anotherCBasic.getValue());
    }

}
