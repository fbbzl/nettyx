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

    private static final boolean C_DEFAULT_SIGNED = true;
    
    protected cbasic(V value) {
        super(value);
    }

    protected cbasic(ByteBuf buf, ByteOrder byteOrder) {
        super(buf, byteOrder);
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
