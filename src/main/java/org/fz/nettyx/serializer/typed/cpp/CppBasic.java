package org.fz.nettyx.serializer.typed.cpp;

import io.netty.buffer.ByteBuf;
import org.fz.nettyx.serializer.typed.c.CBasic;

/**
 * The type Cpp basic.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2023 /12/15 16:04
 */
public abstract class CppBasic<V extends Comparable<V>> extends CBasic<V> {

    protected CppBasic(V value, int size) {
        super(value, size);
    }

    protected CppBasic(ByteBuf buf, int size) {
        super(buf, size);
    }

}
