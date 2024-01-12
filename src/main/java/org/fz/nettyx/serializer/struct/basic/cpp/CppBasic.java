package org.fz.nettyx.serializer.struct.basic.cpp;

import io.netty.buffer.ByteBuf;
import java.nio.ByteOrder;
import org.fz.nettyx.serializer.struct.basic.Basic;

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
    public int compareTo(CppBasic<V> anotherCBasic) {
        return this.getValue().compareTo(anotherCBasic.getValue());
    }

}
